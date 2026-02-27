-- ============================================================================
-- NOTEDER / PrivateNote - Backend Veritabanı Scriptleri
-- ============================================================================

-- 0. GEREKLİ UZANTILAR
CREATE
EXTENSION IF NOT EXISTS pgcrypto;

-- 1. KULLANICILAR
CREATE TABLE IF NOT EXISTS users
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    username VARCHAR
(
    100
) NOT NULL,
    email VARCHAR
(
    255
) NOT NULL UNIQUE,
    password_hash VARCHAR
(
    255
) NOT NULL,
    avatar TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
)
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users (LOWER (email));
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users (LOWER (username));
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users (created_at);

-- 2. KULLANICI AYARLARI
CREATE TABLE IF NOT EXISTS user_settings
(
    user_id
    UUID
    PRIMARY
    KEY
    REFERENCES
    users
(
    id
) ON DELETE CASCADE,
    theme VARCHAR
(
    10
) NOT NULL DEFAULT 'light' CHECK
(
    theme
    IN
(
    'light',
    'dark'
)),
    color_theme VARCHAR
(
    50
) NOT NULL DEFAULT 'default',
    font_size VARCHAR
(
    20
) NOT NULL DEFAULT 'medium' CHECK
(
    font_size
    IN
(
    'small',
    'medium',
    'large'
)),
    default_category VARCHAR
(
    100
) NOT NULL DEFAULT 'Genel',
    default_note_color VARCHAR
(
    50
) NOT NULL DEFAULT 'default',
    default_secure_password VARCHAR
(
    255
),
    show_stats BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
)
    );

-- 3. NOTLAR
CREATE TABLE IF NOT EXISTS notes
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    user_id UUID NOT NULL REFERENCES users
(
    id
) ON DELETE CASCADE,
    title VARCHAR
(
    500
),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
),
    is_favorite BOOLEAN NOT NULL DEFAULT false,
    category VARCHAR
(
    100
) NOT NULL DEFAULT 'Genel',
    color VARCHAR
(
    50
) NOT NULL DEFAULT 'default',
    is_secure BOOLEAN NOT NULL DEFAULT false,
    encrypted_content TEXT,
    has_custom_password BOOLEAN NOT NULL DEFAULT false
    );

CREATE INDEX IF NOT EXISTS idx_notes_user_id ON notes (user_id);
CREATE INDEX IF NOT EXISTS idx_notes_user_updated ON notes (user_id, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_notes_user_favorite ON notes (user_id, is_favorite) WHERE is_favorite = true;
CREATE INDEX IF NOT EXISTS idx_notes_user_category ON notes (user_id, category);
CREATE INDEX IF NOT EXISTS idx_notes_created_at ON notes (created_at);

-- 4. EKLER
CREATE TABLE IF NOT EXISTS attachments
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    note_id UUID NOT NULL REFERENCES notes
(
    id
) ON DELETE CASCADE,
    name VARCHAR
(
    255
) NOT NULL,
    type VARCHAR
(
    100
) NOT NULL,
    size BIGINT NOT NULL,
    data BYTEA,
    thumbnail BYTEA,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
)
    );

CREATE INDEX IF NOT EXISTS idx_attachments_note_id ON attachments (note_id);

-- 5. JWT REFRESH TOKEN TABLOSU
CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    user_id UUID NOT NULL REFERENCES users
(
    id
) ON DELETE CASCADE,
    token_hash VARCHAR
(
    255
) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
)
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

-- 6. OPSİYONEL: OTURUM GEÇMİŞİ
CREATE TABLE IF NOT EXISTS user_sessions
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    user_id UUID NOT NULL REFERENCES users
(
    id
) ON DELETE CASCADE,
    token_hash VARCHAR
(
    255
) NOT NULL,
    user_agent TEXT,
    ip_address VARCHAR
(
    45
),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
)
    );

CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions (user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions (expires_at);

-- 7. ORTAK updated_at TRIGGER FONKSİYONU
CREATE
OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at
= NOW();
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- Trigger tanımları
DO
$$
BEGIN
  IF
NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'users_updated_at'
  ) THEN
CREATE TRIGGER users_updated_at
    BEFORE UPDATE
    ON users
    FOR EACH ROW EXECUTE PROCEDURE set_updated_at();
END IF;

  IF
NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'user_settings_updated_at'
  ) THEN
CREATE TRIGGER user_settings_updated_at
    BEFORE UPDATE
    ON user_settings
    FOR EACH ROW EXECUTE PROCEDURE set_updated_at();
END IF;

  IF
NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'notes_updated_at'
  ) THEN
CREATE TRIGGER notes_updated_at
    BEFORE UPDATE
    ON notes
    FOR EACH ROW EXECUTE PROCEDURE set_updated_at();
END IF;
END;
$$;

-- 8. KULLANICI OLUŞTURULDUĞUNDA VARSAYILAN user_settings KAYDI
CREATE
OR REPLACE FUNCTION create_user_settings_on_signup()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO user_settings (user_id)
VALUES (NEW.id);
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DO
$$
BEGIN
  IF
NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'after_user_insert'
  ) THEN
CREATE TRIGGER after_user_insert
    AFTER INSERT
    ON users
    FOR EACH ROW EXECUTE PROCEDURE create_user_settings_on_signup();
END IF;
END;
$$;
