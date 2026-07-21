EngineRoomLog ⚓

A production-grade Android app that replaces the paper engine-room logbook on ships — built by a former marine engineer who spent 7 years at sea keeping these logs by hand. EngineRoomLog is offline-first (a ship spends most of its life without connectivity), tamper-evident (an inspector has to trust this log), and fleet-synced (the office sees the journal before the ship reaches port). Built with Kotlin, Jetpack Compose, Room, and Firebase.

📱 App Screenshots
<!-- Screenshots i will add later -->
⚓ Why This Exists

I served 7 years at sea as a marine engineer officer, filling in engine-room logbooks by hand every watch — rows of readings, signed off by the engineer, kept for inspectors who need to trust that no page was altered after the fact.

Paper does this job, but it can't prove itself. A page can be torn out and rewritten; the only defense is the inspector's suspicion. EngineRoomLog keeps everything paper does — the watch hierarchy, the sign-off chain, the day-by-day pages — and adds the one thing paper never had: a mathematical guarantee that a closed page was never changed.

This isn't a generic CRUD app with a maritime skin. Every design decision comes from having lived the workflow: why "removing" a parameter must be deactivation and never deletion, why the person who collects a reading shouldn't be the one who posts it, why the log has to keep working with no internet for weeks.

🌟 Key Engineering Highlights
✍️ Two-stage sign-off from real ship practice — a watchkeeper collects readings; an engineer posts them. "Posting" sends nothing anywhere — it inks the record and locks it. Who collected, who posted, and both timestamps travel with every entry, surfaced read-only on a tap. Role-based access carried through navigation as route arguments: an oiler never sees the buttons an engineer does.
🔒 Write-once, tamper-evident PDFs — each day exports to an A4-landscape page drawn on a raw Canvas (group bands, measured column headers that wrap instead of truncate, ruled grid, a signatures section where unposted rows print as UNSIGNED). A journal page is a photograph: once written it is never silently rewritten. Every upload carries a SHA-256 fingerprint in its cloud metadata, and a Verify integrity action re-hashes local files against the cloud's records — "this page was never altered" becomes a provable claim, not a promise.
📡 Offline-first fleet sync — the device (not the person) signs into the fleet with one account; PDFs and entries upload themselves the moment the network returns, with no button and no scheduler. The filesystem is the queue — local files missing from the cloud are the backlog — and the sync is idempotent, so it can run on every app-start and every network-available callback without ever duplicating a file. Deterministic filenames make the diff trivial.
🗄️ Group-aware PDF pagination — wide engine rooms don't fit one page. Whole parameter groups are packed onto pages without ever splitting a group mid-way (a group larger than a page gets its own pages), mirroring how a paper log flows onto continuation sheets.
🔄 Live entry mirroring with Room as the queue — readings stream to Firestore as they're saved, tracked by a nullable syncedAt column: null (or older than postedAt) means pending. Race-safe timestamps prefer syncing twice over missing once — at-least-once, never at-most-once.
🧩 Full form management, no developer needed — engineers add parameters and groups from a dialog, rename them, move a parameter between groups, edit its unit or operational state, or retire it. Everything updates live through Room → Flow → Compose. The killer detail: "removing" is deactivation, never deletion — past records stay untouched, because an inspector has to trust this log.
⚓ Sea/Port operational modes — switch to "In Port" and the main-engine parameters disappear while generators stay. One computed property filters the whole form. Store the facts, derive the views.
🚀 First-run setup that stands on its own — no seeded test data: a fresh install walks through vessel setup → chief-engineer account (employee-number login, bcrypt-hashed) → a realistic starter template the chief can reshape. The app is born configured, not hard-coded.
🛠 Tech Stack & Libraries
Language: Kotlin
UI: Jetpack Compose (Material 3)
Architecture: MVVM with unidirectional data flow; manual dependency injection
Local storage: Room (with hand-written migrations, exportSchema = true)
Navigation: Navigation Compose (role & crew id carried as route arguments)
Cloud: Firebase Auth (device identity), Cloud Storage (PDFs + SHA-256 metadata), Cloud Firestore (live entries)
Security: bcrypt password hashing (at.favre.lib), SHA-256 integrity via MessageDigest
PDF: Android PdfDocument + Canvas (no third-party PDF library)
Async: Kotlin Coroutines + Flow
🏗 Architecture

The app follows an offline-first, layered architecture — Room is the source of truth, the cloud is a mirror:

engineroomlog/
├── MainActivity.kt              # entry point; kicks off AutoSync
├── core/
│   ├── security/                # PasswordHasher (bcrypt)
│   ├── pdf/                     # JournalPdfExporter (Canvas), PdfHasher (SHA-256)
│   └── sync/                    # FleetConnection, JournalUploader, EntrySyncer,
│                                #   AutoSync, NetworkCheck
├── data/local/
│   ├── database/                # EngineRoomDatabase, DatabaseProvider (migrations),
│   │                            #   TemplateSeeder
│   ├── dao/                     # VesselProfile, ParameterGroup, Parameter,
│   │                            #   CrewMember, LogEntry, Reading
│   ├── entity/                  # room entities
│   └── model/                   # enums: CrewRole, EntryStatus, OperationalState, Cadence
└── ui/
    ├── navigation/              # AppNavHost — start-destination decided by DB state
    ├── scaffold/                # AppDrawer (role-gated menu items)
    ├── login/  vesselsetup/  chiefsetup/
    ├── logentry/  journal/  managegroups/  managecrew/
    ├── pdflist/                 # exported journals + upload status marks
    └── fleet/                   # device-to-fleet connection, sync, verify
The data layer owns the truth. ViewModels read Room via Flow; the UI never touches the cloud directly.
The sync layer is one-way (ship → office) and idempotent. No conflict resolution, by design — the tablet produces, the cloud archives.
The filesystem is the upload queue for PDFs; Room's syncedAt column is the queue for entries.
🔒 Data Integrity & Offline Behavior
Write-once journal pages — a guard refuses to overwrite an existing PDF; the file is marked read-only after writing.
SHA-256 on every upload — fingerprint stored as cloud metadata; a verify action re-hashes and compares.
Offline-aware by construction — when there's no network the app never reaches for Firebase (no blocking timeouts, no frozen UI); the PDF list shows the last known upload status from a local cache instead of a blank stare.
Lazy catch-up — no midnight jobs. When the journal opens, any past day with entries but no PDF is exported on the spot. Nobody reads a journal at midnight; they read it in the morning.
🔮 Future Improvements
Fleet dashboard (web): an office-side view of every vessel's live entries and archived journals — the natural other end of the sync pipeline.
Company-issued accounts: crew provisioned centrally from the office instead of created on the tablet (the current local-creation flow stays as the offline fallback).
Password reset & recovery: chief-initiated resets and a recovery path so a forgotten chief password can't lock a tablet's history.
PDF size optimization: font subsetting to shrink multi-page exports.
Hardening: Firebase App Check and per-vessel Storage/Firestore rules ("a device may write only to its own folder").
Testing: unit tests for the sync queue logic, PDF pagination, and migrations.

Developed by Baran Cenk Dincsoy 📍 Charlotte, NC · GitHub · LinkedIn
