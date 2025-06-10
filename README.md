CatatNada
CatatNada adalah aplikasi Android untuk menemukan, mencari, dan mengelola informasi lagu. Fitur utamanya adalah kemampuan untuk mengkurasi playlist pribadi yang disimpan secara lokal di perangkat, menjadikannya "buku catatan" musik personal Anda.

Proyek ini dikembangkan untuk memenuhi serangkaian kriteria teknis pengembangan aplikasi Android, mulai dari UI, networking, hingga penyimpanan data lokal.

Fitur-fitur:

Discover & Search: Menemukan lagu yang sedang tren (global & per genre) atau mencari lagu spesifik menggunakan data dari Last.fm API.
Local Playlist CRUD: Fungsi untuk Create, Read, Update, & Delete playlist dan lagu di dalamnya. Semua data disimpan secara lokal menggunakan SQLite.
Theme Customization: Pilihan antara tema Terang, Gelap, atau mengikuti sistem, disimpan menggunakan SharedPreferences.
Robust UI: Antarmuka yang responsif dengan RecyclerView dan Navigation Component, serta memiliki penanganan error koneksi dengan tombol refresh.
Tumpukan Teknologi (Tech Stack)
Bahasa: Java
Arsitektur: Single-Activity, Multi-Fragment
UI: Material Components, RecyclerView, BottomNavigationView
Navigasi: Android Jetpack Navigation Component
Networking: Retrofit2 & GSON
Database: SQLite (dengan SQLiteOpenHelper)
Background Processing: ExecutorService & Handler
Image Loading: Glide
