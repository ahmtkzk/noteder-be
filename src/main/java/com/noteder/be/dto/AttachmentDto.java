package com.noteder.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {
    private UUID id;
    private UUID noteId;
    private String name;
    private String type;
    private Long size;
    private byte[] data;
    private byte[] thumbnail;
    private LocalDateTime createdAt;
}
