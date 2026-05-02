package com.ielts.helper.controller;

import com.ielts.helper.Agent.IeltsAssistantAgent;
import com.ielts.helper.common.JwtUtil;
import com.ielts.helper.common.Result;
import com.ielts.helper.entity.Conversation;
import com.ielts.helper.entity.Messages;
import com.ielts.helper.entity.dto.ChatRequestDTO;
import com.ielts.helper.service.ConversationService;
import com.ielts.helper.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final long SSE_TIMEOUT = 300_000L;
    private final ConcurrentHashMap<Long, SseEmitter> activeEmitters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AtomicBoolean> cancellationFlags = new ConcurrentHashMap<>();

    @Autowired
    private IeltsAssistantAgent ieltsAssistantAgent;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> chat(@RequestBody ChatRequestDTO dto, HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = jwtUtil.getUserId(token);

        Conversation conversation = conversationService.getConversationById(dto.getConversationId());
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        SseEmitter existingEmitter = activeEmitters.get(dto.getConversationId());
        if (existingEmitter != null) {
            existingEmitter.complete();
            activeEmitters.remove(dto.getConversationId());
        }

        cancellationFlags.remove(dto.getConversationId());
        AtomicBoolean cancelled = new AtomicBoolean(false);
        cancellationFlags.put(dto.getConversationId(), cancelled);

        messageService.saveMessage(dto.getConversationId(), "user", dto.getMessage());

        List<Messages> historyMessages = messageService.getConversationMessages(dto.getConversationId());

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        activeEmitters.put(dto.getConversationId(), emitter);

        emitter.onCompletion(() -> {
            activeEmitters.remove(dto.getConversationId());
            cancellationFlags.remove(dto.getConversationId());
        });
        emitter.onTimeout(() -> {
            activeEmitters.remove(dto.getConversationId());
            cancellationFlags.remove(dto.getConversationId());
        });
        emitter.onError(e -> {
            activeEmitters.remove(dto.getConversationId());
            cancellationFlags.remove(dto.getConversationId());
        });

        final boolean[] wasStopped = {false};

        new Thread(() -> {
            try {
                StringBuilder fullResponse = new StringBuilder();
                Flux<String> streamFlux = ieltsAssistantAgent.streamChatWithHistory(userId, historyMessages, dto.getMessage(), cancelled);

                streamFlux.subscribe(
                    content -> {
                        fullResponse.append(content);
                        try {
                            emitter.send(SseEmitter.event()
                                .name("message")
                                .data(content));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    error -> {
                        emitter.completeWithError(error);
                    },
                    () -> {
                        String responseContent = fullResponse.toString();
                        if (responseContent != null && !responseContent.isEmpty()) {
                            messageService.saveMessage(dto.getConversationId(), "assistant", responseContent);
                        }

                        if (cancelled.get()) {
                            wasStopped[0] = true;
                            try {
                                emitter.send(SseEmitter.event()
                                    .name("stopped")
                                    .data("[STOPPED]"));
                            } catch (IOException e) {
                            }
                        } else {
                            try {
                                emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data("[DONE]"));
                            } catch (IOException e) {
                            }
                        }
                        emitter.complete();
                    }
                );
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return ResponseEntity.ok(emitter);
    }

    @DeleteMapping("/{conversationId}/stop")
    public ResponseEntity<Result<String>> stopGeneration(@PathVariable Long conversationId, HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, "未登录或token已过期"));
        }
        Long userId = jwtUtil.getUserId(token);

        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error(403, "无权限停止该对话"));
        }

        AtomicBoolean cancelled = cancellationFlags.get(conversationId);
        if (cancelled != null) {
            cancelled.set(true);
        }

        SseEmitter emitter = activeEmitters.remove(conversationId);
        if (emitter != null) {
            emitter.complete();
            return ResponseEntity.ok(Result.success("已停止生成"));
        }
        return ResponseEntity.ok(Result.success("没有正在进行的生成"));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}