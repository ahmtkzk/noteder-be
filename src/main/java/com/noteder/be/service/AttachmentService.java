package com.noteder.be.service;

import com.noteder.be.dto.AttachmentDto;
import com.noteder.be.entity.Attachment;
import com.noteder.be.entity.Note;
import com.noteder.be.repository.AttachmentRepository;
import com.noteder.be.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final NoteRepository noteRepository;

    @Transactional
    public AttachmentDto createAttachment(UUID noteId, AttachmentDto attachmentDto) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        Attachment attachment = Attachment.builder()
                .note(note)
                .name(attachmentDto.getName())
                .type(attachmentDto.getType())
                .size(attachmentDto.getSize())
                .data(attachmentDto.getData())
                .thumbnail(attachmentDto.getThumbnail())
                .build();

        Attachment savedAttachment = attachmentRepository.save(attachment);
        return mapToDto(savedAttachment);
    }

    public List<AttachmentDto> getAttachmentsByNoteId(UUID noteId) {
        return attachmentRepository.findByNoteId(noteId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AttachmentDto getAttachmentById(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        return mapToDto(attachment);
    }

    @Transactional
    public void deleteAttachment(UUID attachmentId) {
        attachmentRepository.deleteById(attachmentId);
    }

    private AttachmentDto mapToDto(Attachment attachment) {
        return AttachmentDto.builder()
                .id(attachment.getId())
                .noteId(attachment.getNote().getId())
                .name(attachment.getName())
                .type(attachment.getType())
                .size(attachment.getSize())
                .data(attachment.getData())
                .thumbnail(attachment.getThumbnail())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
