package org.example.nebulachat.Chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat.send") // <-- igual ao SEND_DESTINATION no Android
    @SendTo("/topic/chat/public") // <-- igual ao subscribe do Android
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        System.out.println("📨 Mensagem recebida no servidor:");
        System.out.println("  De: " + chatMessage.getSender());
        System.out.println("  Conteúdo: " + chatMessage.getContent());
        System.out.println("  Sala: " + chatMessage.getRoomId());

        return chatMessage; // devolve para todos os conectados
    }

    @MessageMapping("/chat.join")
    @SendTo("/topic/chat/public")
    public ChatMessage join(@Payload ChatMessage chatMessage) {
        chatMessage.setContent(chatMessage.getSender() + " entrou no chat!");
        return chatMessage;
    }
}
