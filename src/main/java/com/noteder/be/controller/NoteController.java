package com.noteder.be.controller;

import com.noteder.be.dto.NoteDto;
import com.noteder.be.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping("/{userId}")
    public ResponseEntity<NoteDto> createNote(@PathVariable UUID userId, @RequestBody NoteDto noteDto) {
        return ResponseEntity.ok(noteService.createNote(userId, noteDto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NoteDto>> getNotesByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(noteService.getNotesByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDto> getNoteById(@PathVariable UUID id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteDto> updateNote(@PathVariable UUID id, @RequestBody NoteDto noteDto) {
        return ResponseEntity.ok(noteService.updateNote(id, noteDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable UUID id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
