package com.noteder.be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String theme = "light";

    @Column(name = "color_theme", nullable = false, length = 50)
    @Builder.Default
    private String colorTheme = "default";

    @Column(name = "font_size", nullable = false, length = 20)
    @Builder.Default
    private String fontSize = "medium";

    @Column(name = "default_category", nullable = false, length = 100)
    @Builder.Default
    private String defaultCategory = "Genel";

    @Column(name = "default_note_color", nullable = false, length = 50)
    @Builder.Default
    private String defaultNoteColor = "default";

    @Column(name = "default_secure_password")
    private String defaultSecurePassword;

    @Column(name = "show_stats", nullable = false)
    @Builder.Default
    private boolean showStats = true;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
