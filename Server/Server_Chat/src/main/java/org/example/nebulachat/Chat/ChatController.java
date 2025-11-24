package org.example.nebulachat.Chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    private Map<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    @SendTo("/topic/chat/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        System.out.println("📨 Mensagem recebida no servidor:");
        System.out.println("  De: " + chatMessage.getSender());
        System.out.println("  Conteúdo: " + chatMessage.getContent());
        System.out.println("  Sala: " + chatMessage.getRoomId());

        return chatMessage;
    }

    @MessageMapping("/chat.join")
    @SendTo("/topic/chat/public")
    public ChatMessage join(@Payload ChatMessage chatMessage) {
        chatMessage.setContent(chatMessage.getSender() + " entrou no chat!");
        return chatMessage;
    }


    @MessageMapping("/chat.sendPrivate")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {

        String senderId = chatMessage.getSenderId();
        String receiverId = chatMessage.getReceiverId();
        String roomId = generateRoomId(senderId, receiverId);
        chatMessage.setRoomId(roomId);

        String destination = "/topic/chat/room/" + roomId;

        System.out.println("📨 Mensagem privada recebida:");
        System.out.println("  De: " + senderId);
        System.out.println("  De: " + destination);
        System.out.println("  Para: " + receiverId);
        System.out.println("  Room: " + roomId);
        System.out.println("  Conteúdo: " + chatMessage.getContent());

        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, chatMessage);
    }

    // Mapa em memória com todas as rooms privadas existentes
    private final Map<String, Set<String>> privateRooms = new ConcurrentHashMap<>();

    @MessageMapping("/chat.joinPrivate")
    public void joinPrivateRoom(@Payload ChatMessage chatMessage) {

        String senderId = chatMessage.getSenderId();
        String receiverId = chatMessage.getReceiverId();

        if (senderId == null || receiverId == null) {
            System.out.println("❌ ERRO: senderId ou receiverId = null");
            return;
        }

        String roomId = generateRoomId(senderId, receiverId);

        privateRooms.computeIfAbsent(roomId, id -> {
            System.out.println("🆕 Criando nova room: " + id);

            ChatMessage createEvent = new ChatMessage();
            createEvent.setType(MessageType.ROOM_CREATED);
            createEvent.setRoomId(id);
            createEvent.setSenderId(senderId);
            createEvent.setReceiverId(receiverId);

            messagingTemplate.convertAndSend("/topic/chat/room/" + id, createEvent);

            return ConcurrentHashMap.newKeySet();
        });

        privateRooms.get(roomId).add(senderId);

        chatMessage.setRoomId(roomId);
        chatMessage.setType(MessageType.JOIN);
        chatMessage.setContent(chatMessage.getSender() + " entrou na conversa");

        System.out.println("👤 Usuário entrou na room:");
        System.out.println("  User: " + senderId);
        System.out.println("  Room: " + roomId);

        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, chatMessage);
    }


    @MessageMapping("/chat.leavePrivate")
    public void leavePrivateRoom(@Payload ChatMessage chatMessage) {

        String senderId = chatMessage.getSenderId();
        String receiverId = chatMessage.getReceiverId();
        String roomId = generateRoomId(senderId, receiverId);

        chatMessage.setRoomId(roomId);
        chatMessage.setContent(chatMessage.getSender() + " saiu da conversa");

        System.out.println("👋 Usuário saiu da room:");
        System.out.println("  Usuário: " + senderId);
        System.out.println("  Room: " + roomId);

        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, chatMessage);
    }

    @MessageMapping("/chat.typing")
    public void userTyping(@Payload ChatMessage chatMessage) {

        String senderId = chatMessage.getSenderId();
        String receiverId = chatMessage.getReceiverId();
        String roomId = generateRoomId(senderId, receiverId);

        chatMessage.setRoomId(roomId);
        chatMessage.setContent(chatMessage.getSender() + " está digitando...");

        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, chatMessage);
    }

    private String generateRoomId(String userId1, String userId2) {
        return Arrays.asList(userId1, userId2)
                .stream()
                .sorted()
                .collect(Collectors.joining("_"));
    }
}