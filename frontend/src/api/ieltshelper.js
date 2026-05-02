import request from '@/utils/request';

// 获取对话列表
export function getConversations() {
    return request.get('/api/conversations');
}

// 创建新对话
export function createConversation(title) {
    return request.post('/api/conversations', title);
}

// 获取对话消息
export function getConversationMessages(conversationId) {
    return request.get(`/api/conversations/${conversationId}/messages`);
}

// 删除对话
export function deleteConversation(conversationId) {
    return request.delete(`/api/conversations/${conversationId}`);
}

export function updateConversationTitle(conversationId, title) {
    return request.put(`/api/conversations/${conversationId}/title`, title, { rawBody: true });
}

// 发送消息（流式响应）
export async function sendMessageStream(conversationId, message) {
    const token = localStorage.getItem('token');
    const response = await fetch('http://localhost:8080/api/chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            conversationId: conversationId,
            message: message
        })
    });

    if (!response.ok) {
        throw new Error('发送消息失败');
    }

    return response;
}

// 停止生成消息
export async function stopMessageStream(conversationId) {
    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/chat/${conversationId}/stop`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        throw new Error('停止生成失败');
    }

    return response.json();
}
