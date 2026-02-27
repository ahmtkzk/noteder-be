package com.noteder.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteDto {
    private UUID id;
    private UUID userId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isFavorite;
    private String category;
    private String color;
    private boolean isSecure;
    private String encryptedContent;
    private boolean hasCustomPassword;

    // Attachments metadata
    private List<AttachmentDto> attachments;
    private int attachmentsCount;
}
