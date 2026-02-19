package com.noteder.be.controller;

import com.noteder.be.dto.AttachmentDto;
import com.noteder.be.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/{noteId}")
    public ResponseEntity<AttachmentDto> createAttachment(@PathVariable UUID noteId, @RequestBody AttachmentDto attachmentDto) {
        return ResponseEntity.ok(attachmentService.createAttachment(noteId, attachmentDto));
    }

    @GetMapping("/note/{noteId}")
    public ResponseEntity<List<AttachmentDto>> getAttachmentsByNoteId(@PathVariable UUID noteId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByNoteId(noteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttachmentDto> getAttachmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(attachmentService.getAttachmentById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }
}
