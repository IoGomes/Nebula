package org.example.nebulachat.Config;

import lombok.extern.slf4j.Slf4j;
import org.example.nebulachat.Chat.ChatMessage;
import org.example.nebulachat.Chat.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class WebSocketEventListener {

    private final AtomicInteger connectedUsers = new AtomicInteger(0);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        int count = connectedUsers.incrementAndGet();
        log.info("🟢 Novo usuário conectado. Total: {}", count);

        ChatMessage message = ChatMessage.builder()
                .sender("Servidor")
                .content("Um novo usuário entrou! Total online: " + count)
                .messageType(MessageType.JOIN)
                .build();

        messagingTemplate.convertAndSend("/topic/public", message);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        int count = connectedUsers.decrementAndGet();
        log.info("🔴 Usuário desconectado. Total: {}", count);

        ChatMessage message = ChatMessage.builder()
                .sender("Servidor")
                .content("Um usuário saiu. Total online: " + count)
                .messageType(MessageType.LEAVE)
                .build();

        messagingTemplate.convertAndSend("/topic/public", message);
    }

    public int getConnectedUsersCount() {
        return connectedUsers.get();
    }
}
