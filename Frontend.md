# Noteder-ui — Project Summary (Backend Developer)

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Runtime** | React 18 + TypeScript |
| **Build** | Vite 6, SWC (React) |
| **UI** | Radix UI primitives, Tailwind CSS v4, shadcn/ui-style components, Chakra UI, lucide-react |
| **State** | React `useState` / `useEffect` (no global store) |
| **Forms** | react-hook-form |
| **Animation** | motion (framer-motion) |
| **Deploy** | Node 20 Alpine, static build served via `vite preview` on port 4173 |

---

## Architecture

- **SPA** with client-side routing (`notes` / `profile`).
- **No backend**: all data in `localStorage` (notes, auth).
- **Auth**: simulated via `localStorage.setItem('isLoggedIn', 'true')`; no real auth flow.
- **Data flow**: `App.tsx` owns notes state and passes handlers down; no API layer.

---

## Entry Points

| Entry | Path | Purpose |
|-------|------|---------|
| HTML | `index.html` | Root document, loads `/src/main.tsx` |
| App bootstrap | `src/main.tsx` | Mounts `App`, imports global styles |
| Root component | `src/App.tsx` | Layout, auth gate, notes CRUD, page switching |

---

## Main Modules

| Module | Role |
|--------|------|
| `App.tsx` | Root state (notes, auth, page), CRUD handlers, localStorage sync |
| `Auth.tsx` | Login/register UI (mock; no real auth) |
| `NotesList.tsx` | List view, favorites, delete |
| `NoteEditor.tsx` | Create/edit notes |
| `NoteViewDialog.tsx` | View note, share link placeholder (`https://nt.ly/...`) |
| `ProfileSettings.tsx` | Theme, font size, defaults |
| `ThemeContext.tsx` | Light/dark theme |
| `Header.tsx` | Nav, logout |
| `components/ui/*` | shadcn-style primitives (dialog, form, etc.) |
| `components/types.ts` | `Note`, `Attachment`, `UserSettings` |

---

## External Integrations

- **None**: no `fetch`, axios, or other HTTP calls.
- `docs/database-schema.sql` defines PostgreSQL schema for a future backend.
- Share link placeholder: `https://nt.ly/${note.id}` (not wired to any service).

---

## Potential Risks

1. **Auth** — Login is simulated; anyone can set `localStorage.isLoggedIn = 'true'`.
2. **Data loss** — Notes in `localStorage`; cleared on private browsing or user clearing data.
3. **Attachments** — Base64 in `localStorage`; browser limits (~5–10 MB) and performance issues.
4. **Dependency versions** — Wildcards (`*`) for `@chakra-ui/react`, `clsx`, `tailwind-merge`, `motion`; risk of breaking changes.
5. **Schema drift** — Frontend `Note` vs `docs/database-schema.sql`; backend must align with `Note` and `Attachment`.
6. **No API layer** — No env-based API base URL or client; backend integration will need new services and wiring.

---

## Backend Integration Notes

- Schema: `users`, `user_settings`, `notes`, `attachments`, `refresh_tokens`, `user_sessions`.
- Auth: JWT (access + refresh) with refresh token rotation.
- Frontend `Note` fields: `id`, `title`, `content`, `category`, `color`, `isFavorite`, `attachments`, `isSecure`, `encryptedContent`, `hasCustomPassword`.
- `attachments.data` in DB is `BYTEA`; frontend uses base64; backend must decode before storing.
