package com.ielts.helper.controller;

import com.ielts.helper.common.JwtUtil;
import com.ielts.helper.common.Result;
import com.ielts.helper.entity.Conversation;
import com.ielts.helper.entity.Messages;
import com.ielts.helper.service.ConversationService;
import com.ielts.helper.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<Result<List<Conversation>>> getUserConversations(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, "未登录或token已过期"));
        }
        Long userId = jwtUtil.getUserId(token);
        List<Conversation> conversations = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(Result.success(conversations));
    }

    @PostMapping
    public ResponseEntity<Result<Conversation>> createConversation(
            @RequestBody(required = false) String title,
            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, "未登录或token已过期"));
        }
        Long userId = jwtUtil.getUserId(token);
        if (title == null || title.trim().isEmpty()) {
            title = "新对话";
        }
        Conversation conversation = conversationService.createConversation(userId, title);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(conversation));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Result<List<Messages>>> getConversationMessages(
            @PathVariable Long conversationId,
            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, "未登录或token已过期"));
        }
        Long userId = jwtUtil.getUserId(token);
        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error(403, "无权限访问该对话"));
        }
        List<Messages> messages = messageService.getConversationMessages(conversationId);
        return ResponseEntity.ok(Result.success(messages));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Result<Void>> deleteConversation(
            @PathVariable Long conversationId,
            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, "未登录或token已过期"));
        }
        Long userId = jwtUtil.getUserId(token);
        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error(403, "无权限删除该对话"));
        }
        messageService.deleteConversationMessages(conversationId);
        conversationService.deleteConversation(conversationId, userId);
        return ResponseEntity.ok(Result.success("删除成功"));
    }

    @PutMapping("/{conversationId}/title")
    public ResponseEntity<Result<Void>> updateConversationTitle(
            @PathVariable Long conversationId,
            @RequestBody String newTitle,
            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, "未登录或token已过期"));
        }
        Long userId = jwtUtil.getUserId(token);
        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error(403, "无权限修改该对话"));
        }
        conversationService.updateConversationTitle(conversationId, userId, newTitle);
        return ResponseEntity.ok(Result.success("更新成功"));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}