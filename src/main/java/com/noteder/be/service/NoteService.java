package com.noteder.be.service;

import com.noteder.be.dto.AttachmentDto;
import com.noteder.be.dto.NoteDto;
import com.noteder.be.entity.Attachment;
import com.noteder.be.entity.Note;
import com.noteder.be.entity.User;
import com.noteder.be.repository.AttachmentRepository;
import com.noteder.be.repository.NoteRepository;
import com.noteder.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional
    public NoteDto createNote(UUID userId, NoteDto noteDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Note note = Note.builder()
                .user(user)
                .title(noteDto.getTitle())
                .content(noteDto.getContent())
                .isFavorite(noteDto.isFavorite())
                .category(noteDto.getCategory())
                .color(noteDto.getColor())
                .isSecure(noteDto.isSecure())
                .encryptedContent(noteDto.getEncryptedContent())
                .hasCustomPassword(noteDto.isHasCustomPassword())
                .build();

        Note savedNote = noteRepository.save(note);

        // Handle attachments if any
        if (noteDto.getAttachments() != null && !noteDto.getAttachments().isEmpty()) {
            List<Attachment> attachments = noteDto.getAttachments().stream()
                    .map(attDto -> Attachment.builder()
                            .note(savedNote)
                            .name(attDto.getName())
                            .type(attDto.getType())
                            .size(attDto.getSize())
                            .data(attDto.getData())
                            .thumbnail(attDto.getThumbnail())
                            .build())
                    .collect(Collectors.toList());
            attachmentRepository.saveAll(attachments);
        }

        return mapToDto(savedNote);
    }

    public List<NoteDto> getNotesByUserId(UUID userId) {
        return noteRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public NoteDto getNoteById(UUID noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        return mapToDto(note);
    }

    @Transactional
    public NoteDto updateNote(UUID noteId, NoteDto noteDto) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        note.setTitle(noteDto.getTitle());
        note.setContent(noteDto.getContent());
        note.setFavorite(noteDto.isFavorite());
        note.setCategory(noteDto.getCategory());
        note.setColor(noteDto.getColor());
        note.setSecure(noteDto.isSecure());
        note.setEncryptedContent(noteDto.getEncryptedContent());
        note.setHasCustomPassword(noteDto.isHasCustomPassword());

        Note updatedNote = noteRepository.save(note);

        // Note: Attachment update logic is simplified here. 
        // In a real scenario, you might want to diff and update attachments.
        
        return mapToDto(updatedNote);
    }

    @Transactional
    public void deleteNote(UUID noteId) {
        noteRepository.deleteById(noteId);
    }

    private NoteDto mapToDto(Note note) {
        List<Attachment> attachments = attachmentRepository.findByNoteId(note.getId());
        List<AttachmentDto> attachmentDtos = attachments.stream()
                .map(att -> AttachmentDto.builder()
                        .id(att.getId())
                        .noteId(att.getNote().getId())
                        .name(att.getName())
                        .type(att.getType())
                        .size(att.getSize())
                        // Don't return full data in list view to save bandwidth
                        .thumbnail(att.getThumbnail())
                        .createdAt(att.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return NoteDto.builder()
                .id(note.getId())
                .userId(note.getUser().getId())
                .title(note.getTitle())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .isFavorite(note.isFavorite())
                .category(note.getCategory())
                .color(note.getColor())
                .isSecure(note.isSecure())
                .encryptedContent(note.getEncryptedContent())
                .hasCustomPassword(note.isHasCustomPassword())
                .attachments(attachmentDtos)
                .attachmentsCount(attachmentDtos.size())
                .build();
    }
}
