package com.ielts.helper.Agent;

import com.ielts.helper.entity.Messages;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class IeltsAssistantAgent {

    private final ChatClient chatClientWithTools;
    private final VectorStore vectorStore;

    private static final String SYSTEM_PROMPT = """
你是一个专业的雅思(IELTS)考试咨询与课程预约助手，名叫 一航。

你的核心职责有三部分：

1. 知识解答：当用户询问关于雅思考试的任何问题（如考试结构、题型、评分标准、备考技巧等），除了回答关于雅思和雅思机构的相关内容，用户提问的其余问题均不予回答。
   你可以使用内部知识库来提供更准确的答案。

2. 信息查询：
   - 如果用户想了解有哪些雅思课程可以预约，你可以使用 listAvailableIeltsCourses 工具查询。
   - 如果用户想了解某位老师的详细信息，你可以使用 getTeacherInfo 工具查询。

3. 预约管理：
   - 查询：如果用户想查看自己的预约，你可以使用 getUserReservations 工具查询（需要传入用户ID）。
   - 预约：如果用户想预约新课程，先用 listAvailableIeltsCourses 列出可用课程让用户选择，确认后再用 reserveIeltsCourse 预约（需要传入用户ID和课程ID）。
   - 取消：如果用户想取消预约，请让用户确认要取消的预约ID，然后用 cancelReservation 取消（需要传入用户ID和预约ID）。

重要规则：
- 不准回答超出雅思范围的问题，如天气情况，旅游攻略等。
- 所有操作都必须基于用户明确的指令和确认。不要擅自为用户做出决定。
- 请始终保持友好、耐心和专业的语气。
- 使用简体中文回复。
- 当需要查询课程、教师、预约等信息时，必须使用提供的工具来获取，不要凭空编造信息。
- 当调用工具后，把工具返回的结果直接展示给用户即可，不需要额外解释。
- 用户ID从上下文中的 {userId} 变量获取，调用工具时必须传入正确的用户ID。""";

    @Autowired
    public IeltsAssistantAgent(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, IeltsAssistantTool ieltsAssistantTool) {
        this.chatClientWithTools = chatClientBuilder
                .defaultTools(ieltsAssistantTool)
                .build();
        this.vectorStore = vectorStore;
    }

    private String getSystemPromptWithUserId(Long userId) {
        return SYSTEM_PROMPT.replace("{userId}", userId.toString());
    }


    public Flux<String> streamChatWithHistory(Long userId, List<Messages> historyMessages, String newMessage, AtomicBoolean cancelled) {
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder().query(newMessage).topK(3).build()
        );

        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String enhancedMessage = context.isEmpty()
                ? newMessage
                : String.format("%s\n\n问题：%s", context, newMessage);

        List<Message> formattedHistory = historyMessages.stream()
                .map(msg -> {
                    if ("user".equals(msg.getRole())) {
                        return new UserMessage(msg.getContent());
                    } else if ("assistant".equals(msg.getRole())) {
                        return new AssistantMessage(msg.getContent());
                    }
                    return null;
                })
                .filter(m -> m != null)
                .collect(Collectors.toList());

        return chatClientWithTools.prompt()
                .messages(formattedHistory)
                .options(ChatOptions.builder()
                        .maxTokens(500)
                        .build())
                .system(getSystemPromptWithUserId(userId))
                .user(enhancedMessage)
                .stream()
                .content()
                .takeWhile(content -> !cancelled.get());
    }
}