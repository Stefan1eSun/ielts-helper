package com.ielts.helper.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class RagConfig {

    @Value("classpath:rag/ielts.txt")
    private Resource ieltsResource;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 创建向量存储
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();

        // 读取文档
        TikaDocumentReader reader = new TikaDocumentReader(ieltsResource);
        List<Document> documents = reader.get();

        // 分割文档为小块
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(documents);

        // 将文档块添加到向量存储中
        vectorStore.add(chunks);

        return vectorStore;
    }
}