## Genel Mimari

Bu doküman, `noteder-ui` (frontend) projesi için geliştirilecek Spring Boot backend API tasarımını ve veritabanı modelini açıklar.
Frontend şu an tüm veriyi `localStorage` üzerinde tutuyor; bu backend ile:

- **Kullanıcılar** veritabanında tutulacak (auth / profil).
- **Notlar** ve ilişkili **ekler (attachments)** veritabanında veya harici storage’da saklanacak.
- **Kullanıcı ayarları** (tema, font boyutu, varsayılan kategori/renk, varsayılan güvenli not şifresi vb.) veritabanına alınacak.
- **Oturum yönetimi** JWT access + refresh token yaklaşımıyla yapılacak.

Veritabanı scriptleri `docs/db-scripts` dosyasında tanımlanmıştır ve PostgreSQL içindir.


## Domain Modelleri (Backend Tarafı)

Frontend `src/components/types.ts` içindeki tipler:

- **Note**
  - `id: string`
  - `title?: string`
  - `content: string`
  - `createdAt: string`
  - `updatedAt: string`
  - `isFavorite: boolean`
  - `category: string`
  - `color: string`
  - `attachments: Attachment[]`
  - `isSecure?: boolean`
  - `encryptedContent?: string`
  - `hasCustomPassword?: boolean`

- **Attachment**
  - `id: string`
  - `name: string`
  - `type: string`
  - `size: number`
  - `data: string` (frontend’de base64 data URL)
  - `thumbnail?: string`

- **UserSettings**
  - `username: string`
  - `email: string`
  - `avatar: string`
  - `fontSize: 'small' | 'medium' | 'large'`
  - `defaultCategory: string`
  - `defaultNoteColor: string`
  - `colorTheme: string`
  - `defaultSecurePassword?: string`

Backend tarafında bu tipler şu tablolara map edilir:

- `users` tablosu → auth ve temel profil (username, email, password_hash, avatar).
- `user_settings` tablosu → tema, font, varsayılan kategori/renk, default secure password, show_stats.
- `notes` tablosu → Note alanları.
- `attachments` tablosu → Attachment alanları.


## Kimlik Doğrulama (Auth) Tasarımı

- **JWT Access Token**: Kısa süreli (ör. 15 dk), HTTP `Authorization: Bearer <token>`.
- **Refresh Token**: Daha uzun süreli (ör. 7–30 gün), `httpOnly`, `secure`, `sameSite=strict` cookie içinde.
- Refresh token’lar **hash’lenmiş** olarak `refresh_tokens` tablosunda tutulur.

### Auth Endpointleri

- **POST `/api/auth/register`**
  - **Amaç**: Yeni kullanıcı kaydı oluşturmak.
  - **Body**:
    ```json
    {
      "username": "string",
      "email": "user@example.com",
      "password": "plain-password"
    }
    ```
  - **Yanıt 201**:
    ```json
    {
      "id": "uuid",
      "username": "string",
      "email": "user@example.com"
    }
    ```
  - **Notlar**:
    - `password` backend’de BCrypt ile `password_hash` alanına yazılır.
    - Aynı email / username için 409 (Conflict) döndür.

- **POST `/api/auth/login`**
  - **Amaç**: Giriş yap, access + refresh token üret.
  - **Body**:
    ```json
    {
      "emailOrUsername": "string",
      "password": "plain-password"
    }
    ```
  - **Başarılı Yanıt 200**:
    ```json
    {
      "accessToken": "jwt-access-token",
      "tokenType": "Bearer",
      "user": {
        "id": "uuid",
        "username": "string",
        "email": "user@example.com",
        "avatar": "string|null"
      }
    }
    ```
    - Refresh token **Set-Cookie** ile `httpOnly` cookie’de döndürülür:
      - `Set-Cookie: refreshToken=<token>; HttpOnly; Secure; SameSite=Strict; Path=/api/auth/refresh`
  - **Hata**:
    - Yanlış şifre veya kullanıcı yoksa 401 Unauthorized.

- **POST `/api/auth/refresh`**
  - **Amaç**: Geçerli refresh token ile yeni access token üretmek.
  - **Girdi**:
    - Cookie’de `refreshToken` gönderilir.
  - **İşleyiş**:
    - Cookie’deki refresh token SHA-256 ile hash’lenir, `refresh_tokens.token_hash` ile eşleştirilir.
    - Süresi geçmişse 401 döndür; geçerliyse:
      - Yeni access token üret.
      - İsteğe bağlı olarak refresh token rotate et (eski kaydı sil, yenisini ekle).

- **POST `/api/auth/logout`**
  - **Amaç**: Kullanıcı oturumunu sonlandırmak.
  - **İşleyiş**:
    - İmzalayan kullanıcının ilgili refresh token kaydı silinir.
    - Cookie `refreshToken` expire olacak şekilde temizlenir.

- **GET `/api/auth/me`**
  - **Amaç**: Access token’dan gelen kullanıcı bilgisini döndürmek.
  - **Auth**: `Authorization: Bearer <accessToken>` zorunlu.
  - **Yanıt**:
    ```json
    {
      "id": "uuid",
      "username": "string",
      "email": "user@example.com",
      "avatar": "string|null"
    }
    ```


## Kullanıcı & Profil Endpointleri

- **GET `/api/users/me`**
  - **Amaç**: Aktif kullanıcının profilini döndürmek.
  - **Yanıt**:
    ```json
    {
      "id": "uuid",
      "username": "string",
      "email": "user@example.com",
      "avatar": "string|null",
      "createdAt": "ISO-8601"
    }
    ```

- **PUT `/api/users/me`**
  - **Amaç**: Profil bilgilerini güncellemek (username, email, avatar).
  - **Body (örnek)**:
    ```json
    {
      "username": "new-name",
      "email": "new@example.com",
      "avatar": "data-url-or-cdn-url"
    }
    ```
  - **Notlar**:
    - Email değiştirilirken benzersizlik kontrolü yapılmalı (409).

- **PUT `/api/users/me/password`**
  - **Amaç**: Parola güncellemek.
  - **Body**:
    ```json
    {
      "currentPassword": "string",
      "newPassword": "string"
    }
    ```
  - **İşleyiş**:
    - `currentPassword` BCrypt hash ile doğrulanmalı; yanlışsa 400/401.
    - `newPassword` tekrar hashlenerek `password_hash` alanına yazılır.


## Kullanıcı Ayarları (UserSettings) Endpointleri

Bu endpointler `ProfileSettings` ve `ThemeContext` ihtiyaçlarını karşılar.

- **GET `/api/user-settings`**
  - **Amaç**: Aktif kullanıcının ayarlarını almak.
  - **Yanıt**:
    ```json
    {
      "fontSize": "small|medium|large",
      "defaultCategory": "Genel",
      "defaultNoteColor": "default",
      "colorTheme": "default|blue|green|purple|rose|orange",
      "theme": "light|dark",
      "defaultSecurePassword": "string|null",
      "showStats": true
    }
    ```

- **PUT `/api/user-settings`**
  - **Amaç**: Kullanıcı ayarlarını güncellemek.
  - **Body (kısmi update desteklemek için tüm alanlar optional olabilir)**:
    ```json
    {
      "fontSize": "medium",
      "defaultCategory": "İş",
      "defaultNoteColor": "yellow",
      "colorTheme": "purple",
      "theme": "dark",
      "defaultSecurePassword": "1234",
      "showStats": false
    }
    ```

**Dikkat**:

- `defaultSecurePassword` kritik bir alan; DB’de **hashlemeden** tutmanız da mümkün ama bu şifre not içeriğini şifrelemek için kullanılıyor, giriş parolasından farkı var:
  - İsterseniz düz metin tutup sadece not şifrelemede kullanabilirsiniz (frontend zaten kendi encryption’ına sahip).
  - Güvenlik hassasiyetiniz yüksekse, bu şifreyi de ayrıca şifreleyip saklamayı düşünebilirsiniz.


## Notlar (Notes) Endpointleri

Tüm not endpointleri authenticated kullanıcıya göre `notes.user_id` üzerinden filtrelenmelidir.

### Not Listeleme ve Filtreleme

- **GET `/api/notes`**
  - **Amaç**: Kullanıcının not listesini almak, filtre ve arama desteklemek.
  - **Query Parametreleri (opsiyonel)**:
    - `search`: içerik ve başlıkta arama (frontend `NotesList` ile uyumlu).
    - `category`: `Genel`, `İş`, `Kişisel`, `Eğitim`, `Alışveriş`, `Diğer` veya serbest.
    - `favorite`: `true/false`.
    - `secure`: `true/false` (şifreli notlar filtresi).
    - `dateRange`: `today|week|month|year`.
  - **Yanıt (önerilen)**:
    ```json
    [
      {
        "id": "uuid",
        "title": "string",
        "content": "string",              // Şifreli notlarda '[Şifreli Not]'
        "createdAt": "ISO-8601",
        "updatedAt": "ISO-8601",
        "isFavorite": true,
        "category": "Genel",
        "color": "default",
        "isSecure": true,
        "hasCustomPassword": false,
        "attachmentsCount": 2             // Büyük payload’tan kaçınmak için sadece sayı
      }
    ]
    ```
  - **Not**:
    - Liste endpointinde **eklerin tamamını ve büyük binary data’yı döndürmeyin**. Sadece sayısını veya küçük metadata’yı döndürmek yeterli. Detay için ayrı endpoint kullanılmalı.

### Not Detayı

- **GET `/api/notes/{id}`**
  - **Amaç**: Tek bir notun tüm detaylarını (attachment metadata dahil) almak.
  - **Yanıt örneği**:
    ```json
    {
      "id": "uuid",
      "title": "string",
      "content": "string",
      "createdAt": "ISO-8601",
      "updatedAt": "ISO-8601",
      "isFavorite": false,
      "category": "Genel",
      "color": "default",
      "isSecure": true,
      "encryptedContent": "v2:base64...",
      "hasCustomPassword": true,
      "attachments": [
        {
          "id": "uuid",
          "name": "file.png",
          "type": "image/png",
          "size": 12345,
          "hasData": false,
          "hasThumbnail": true
        }
      ]
    }
    ```
  - **Not**:
    - Eklerin gerçek binary verisini (`data`) bu endpointte döndürmeyin; bunun için ayrı download endpointleri tanımlanacak.

### Not Oluşturma

- **POST `/api/notes`**
  - **Amaç**: Yeni bir not oluşturmak.
  - **Body** (frontend `NoteEditor`’ın `onSave`’ine denk):
    ```json
    {
      "title": "optional title",
      "content": "string or '[Şifreli Not]'",
      "category": "Genel",
      "isFavorite": false,
      "color": "default",
      "attachments": [
        {
          "name": "file.png",
          "type": "image/png",
          "size": 12345,
          "data": "data:image/png;base64,...",
          "thumbnail": "data:image/jpeg;base64,..."   // opsiyonel
        }
      ],
      "isSecure": true,
      "encryptedContent": "v2:base64...",
      "hasCustomPassword": false
    }
    ```
  - **İşleyiş**:
    - `createdAt` ve `updatedAt` backend tarafından `NOW()` ile set edilir.
    - `attachments` içerisindeki `data` ve `thumbnail` alanları base64 decode edilip `attachments.data` ve `attachments.thumbnail` alanlarına BYTEA olarak yazılır.
    - Büyük dosyalar için (20MB üstü gibi) validation yapın; frontend zaten 20MB limit koyuyor.

### Not Güncelleme

- **PUT `/api/notes/{id}`**
  - **Amaç**: Var olan bir notu güncellemek.
  - **Body**: POST ile aynı payload (partial update istiyorsanız PATCH de ekleyebilirsiniz).
  - **İşleyiş**:
    - İlgili not `user_id = currentUserId` ile birlikte bulunmalı; aksi halde 404.
    - `updatedAt` güncellenmeli.
    - Attachment güncelleme stratejisi:
      - Basit yaklaşım: Var olan ekleri silip body’de gelenleri baştan ekleyin.
      - Daha ileri: Ek ID’lerine göre fark (diff) alıp ekleme/silme/güncelleme yapabilirsiniz.

### Not Silme

- **DELETE `/api/notes/{id}`**
  - **Amaç**: Not ve tüm eklerini silmek.
  - **İşleyiş**:
    - `ON DELETE CASCADE` ile `attachments` otomatik silinir.

### Favori İşaretleme

- **POST `/api/notes/{id}/favorite`**
  - **Amaç**: Notu favoriye alma / çıkarma.
  - **Body** (basit yaklaşım):
    ```json
    { "favorite": true }
    ```
  - veya toggle:
    - Mevcut `is_favorite` alanını tersine çevir.


## Ekler (Attachments) Endpointleri

Frontend şu an ekleri base64 data URL olarak not içinde tutuyor. Backend’e geçtiğinizde:

- Liste / detay endpointleri sadece **metadata** döndürmeli.
- Asıl binary veri için ayrı **download** endpointi tanımlanmalı.

### Ek Oluşturma (Alternatif)

İki tasarım opsiyonu var:

1. **Not ile birlikte gönderme** (POST `/api/notes`) – şu anki frontend’e daha yakın.
2. **Ayrı upload endpointi** (daha esnek, özellikle büyük dosyalarda).

Önerilen ek upload endpointi:

- **POST `/api/notes/{noteId}/attachments`**
  - **Content-Type**: `multipart/form-data`
  - **Alanlar**:
    - `file`: gerçek dosya.
  - **İşleyiş**:
    - `file` direkt binary olarak alınır, `attachments.data`’ya yazılır.
    - Görseller için thumbnail üretilip `thumbnail` alanına yazılır.
  - **Yanıt**:
    ```json
    {
      "id": "uuid",
      "name": "file.png",
      "type": "image/png",
      "size": 12345
    }
    ```

### Ek İndirme

- **GET `/api/notes/{noteId}/attachments/{attachmentId}/download`**
  - **Amaç**: Ek dosyasını binary olarak indirmek.
  - **İşleyiş**:
    - `Content-Type` = `attachments.type`
    - `Content-Disposition: attachment; filename="<name>"`.
    - `attachments.data` BYTEA alanı stream olarak döndürülür.

### Thumbnail Alma

- **GET `/api/notes/{noteId}/attachments/{attachmentId}/thumbnail`**
  - **Amaç**: Sadece küçük önizleme (image thumbnail) döndürmek.
  - `Content-Type` = `image/jpeg` veya thumbnail üretim formatınız.


## Güvenli Notlar (Şifreli İçerik) – Dikkat Noktaları

Frontend’te şifreleme tamamen **client-side** yapılıyor:

- `NoteEditor` içinde `encryptContent` ve `encryptContentV2` fonksiyonları var.
- Backend’in görevi:
  - `encryptedContent` alanını **aynen gelen haliyle saklamak**.
  - **Şifreyi bilmemek** ve yeniden şifreleme yapmamak.

**Dikkat etmeniz gerekenler**:

- `content` alanı, güvenli notlarda sadece placeholder (`"[Şifreli Not]"`) olarak gelir; gerçek içerik `encryptedContent`’tadır.
- Backend `encryptedContent`’i **log’lamamalı**, debug’da bile mümkün olduğunca maskeleme yapın.
- `defaultSecurePassword`:
  - Bu değer kullanıcı tarafında not şifrelemek için kullanılıyor.
  - Çok hassas sistemler için bu şifreyi de ayrıca şifreleyerek saklamayı düşünebilirsiniz (örn. başka bir KMS ile).


## Media / Attachment Saklama Stratejisi

Bu proje için en kritik mimari kararlardan biri **media’ların nasıl saklanacağı**:

- Şu an frontend, dosyaları **base64 data URL** olarak bellek ve localStorage’da tutuyor.
- Backend’e geçtiğinizde aşağıdaki seçenekler var:

- **1) Veritabanında binary (BYTEA / BLOB) saklamak** (şu anki `attachments` tablosu bu şekilde)
  - **Artıları**:
    - Backup, restore, transaction yönetimi tek yerde.
    - Küçük dosyalar için pratik.
  - **Eksileri**:
    - Veritabanı boyutu hızla büyür.
    - Büyük dosyalarda performans ve maliyet sorunları.
  - **Öneri**:
    - Küçük ekler (ör: < 5MB) için uygun.
    - Daha büyük dosyalarda object storage’a geçmeyi planlayın.

- **2) Harici Object Storage (S3, MinIO, Azure Blob vb.) kullanmak**
  - **Artıları**:
    - Büyük dosyalarda daha ucuz ve ölçeklenebilir.
    - CDN entegrasyonu kolay.
  - **Eksileri**:
    - Ek servis konfigürasyonu ve deployment karmaşıklığı.
  - **Öneri**:
    - Uzun vadede media için en sağlam yaklaşım bu.
    - `attachments` tablosuna ek kolonlar ekleyebilirsiniz:
      - `storage_type` (`DB`, `S3` gibi)
      - `external_url` (S3 path vb.).

**Ana prensipler**:

- Liste endpointlerinde **asla base64 data** döndürmeyin (JSON boyutunu uçurur).
- Sadece metadata veya thumbnail (küçük) döndürün.
- Gerçek dosyalar için ayrı, stream tabanlı download endpointi kullanın.


## Spring Boot Katmanları ve Güvenlik

Spring Boot tarafında aşağıdaki katman yapısı önerilir:

- **Controller**: Sadece HTTP katmanı, DTO <-> domain mapping.
- **Service**: İş kuralları (örn. not kullanıcıya ait mi?, ek boyutu limiti, şifreli not yönetimi).
- **Repository**: Spring Data JPA ile tablo erişimi (`User`, `UserSettings`, `Note`, `Attachment`, `RefreshToken`, `UserSession`).

Spring Security için:

- JWT filter:
  - `Authorization: Bearer <token>` header’ını okuyup, geçerli ise `SecurityContext` içine kullanıcıyı yerleştirir.
- Endpointleri `@PreAuthorize("isAuthenticated()")` veya basit `http.authorizeHttpRequests` konfigürasyonu ile koruyun.
- `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh` serbest (permitAll), diğerleri authenticated olmalı.


## Frontend Geçişinde Dikkat Etmeniz Gerekenler

- **LocalStorage’dan backend’e geçiş**:
  - Şu an `App.tsx` dosyasında:
    - Auth durumu: `localStorage.isLoggedIn`.
    - Notlar: `localStorage.notes`.
  - Backend’e taşıdığınızda:
    - Uygulama açılışında:
      - `/api/auth/me` ile user bilgisi,
      - `/api/notes` ile notlar,
      - `/api/user-settings` ile ayarlar çekilecek.
    - Not CRUD işlemleri `handleSaveNote`, `handleDeleteNote`, `handleToggleFavorite` fonksiyonlarında HTTP çağrısına dönüştürülecek.

- **Şifreli notlar**:
  - Frontend şifreleme algoritmasını koruyun; sadece storage yeri (localStorage → DB) değişecek.
  - Backend `encryptedContent` alanını olduğu gibi saklayacak, çözmeyecek.

- **Ekler (media)**:
  - İlk aşamada hızlı ilerlemek için:
    - Not oluştururken/ güncellerken ekleri base64 olarak API’ye gönderip DB’de saklayabilirsiniz.
    - Daha sonra, ölçek ihtiyacı artarsa object storage’a refactor edebilirsiniz.


## Özet

- **Veritabanı şeması** `docs/db-scripts` içinde, PostgreSQL’e hazır ve frontend ile bire bir uyumlu.
- **Auth**: JWT access + refresh token, `users`, `refresh_tokens`, `user_sessions` tabloları üzerinden.
- **Notlar & ekler**: `notes` ve `attachments` tabloları; liste ve detay endpointleri ayrıştırılmış.
- **Güvenli notlar**: Şifreleme tamamen frontend’de, backend sadece encrypted payload’ı saklıyor.
- **Media yönetimi**: Kısa vadede DB, uzun vadede object storage’a evrilebilecek esnek bir yapı önerildi.

Bu tasarım ile Spring Boot backend’ini hayata geçirdikten sonra, frontend tarafında sadece localStorage erişimlerini API çağrılarına dönüştürerek entegre olabilirsiniz.
