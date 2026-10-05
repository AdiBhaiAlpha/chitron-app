# Chitron's Archive (Android)

Native Android application for **Chitron's Archive** — the personal digital archive, writing blog, photo gallery, live visitor messenger, and CMS of **Chitron Bhattacharjee** (চিত্রণ ভট্টাচার্য), AI developer, programmer, designer, and writer from Bangladesh.

## Features

- **Bilingual Editorial Interface (`EN / বাং`)**: Instant switching between English and Bengali (`বাংলা`) with bundled **Kalpurush** and **Noto Serif Bengali** typography and Bengali numeral formatting.
- **Warm Editorial Theme (Light & Dark)**: Crafted with the signature warm ivory (`#FAF8F5`), deep charcoal (`#1A1917`), and burnished bronze (`#8B5E3C` / `#C49A6C`) color palette.
- **Home & Author Profile**: Hero banner, author profile card, topic filter chips, and latest articles feed.
- **Writing Archive & Reader**: Real-time title/content search, category & tag filtering, newest/oldest sorting, automatic reading time calculation, view counters, and previous/next article navigation.
- **Photo Gallery & Full-Screen Lightbox**: Category filter chips with photo counts, location & date metadata, and full-screen interactive lightbox with next/previous navigation.
- **About Page**: Authoritative biography, roles, selected projects (*ShiPu AI*, *Chitron's Archive*), technical stack, philosophy, and social links.
- **Direct Visitor Messenger**: Floating Action Button (`Message Me` / `বার্তা পাঠান`) with quick conversation starters and local Room persistence.
- **PIN-Protected Admin CMS**:
  - **Dashboard**: Total, Published, Draft, Scheduled, and Unread Message statistics.
  - **Posts Manager & Editor**: Create, edit, duplicate, publish/unpublish, trash/delete articles, and restore previous **Revision Snapshots**.
  - **Live Messenger**: Filter visitor threads (`All`, `Unread`, `Active`, `Archived`, `Blocked`) and reply as Chitron.
  - **Gallery & Site Content Manager**: Add/remove gallery photos and customize homepage/about copy live.

## Tech Stack

- **UI**: Kotlin + Jetpack Compose (Material 3)
- **Architecture**: MVVM (`ArchiveViewModel` + `StateFlow` + `ArchiveRepository`)
- **Persistence**: Room Database (`ArchiveDatabase`, KSP)
- **Images**: Coil Compose (`AsyncImage`)
- **Secrets**: Secrets Gradle Plugin reading `.env` / `.env.example` (`BuildConfig.ADMIN_PIN`)
