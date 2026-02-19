package com.noteder.be.service;

import com.noteder.be.dto.NoteDto;
import com.noteder.be.entity.Note;
import com.noteder.be.entity.User;
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
        return mapToDto(updatedNote);
    }

    @Transactional
    public void deleteNote(UUID noteId) {
        noteRepository.deleteById(noteId);
    }

    private NoteDto mapToDto(Note note) {
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
                .build();
    }
}
