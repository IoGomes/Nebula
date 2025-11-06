package org.example.nebulachat.Chat;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ChatMessage {
    private String content;
    private String sender;
    private MessageType messageType;
    private String roomId;
    private long timestamp;
}
