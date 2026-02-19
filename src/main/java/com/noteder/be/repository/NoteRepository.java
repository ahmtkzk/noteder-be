package com.noteder.be.repository;

import com.noteder.be.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByUserId(UUID userId);
    List<Note> findByUserIdAndIsFavoriteTrue(UUID userId);
    List<Note> findByUserIdAndCategory(UUID userId, String category);
}
