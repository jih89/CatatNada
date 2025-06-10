# CatatNada 🎵

**CatatNada** adalah aplikasi Android yang dirancang untuk para pencinta musik. Aplikasi ini berfungsi sebagai alat bantu untuk menemukan lagu baru yang sedang tren, mencari informasi detail tentang lagu dan artis, serta memungkinkan pengguna untuk mengkurasi **playlist pribadi yang disimpan secara lokal** di perangkat.

Proyek ini dikembangkan untuk memenuhi tugas akhir saya sebagai praktikan lab mobile, dan juga untuk menerapkan serangkaian kriteria teknis pengembangan aplikasi Android, mulai dari UI, networking, hingga penyimpanan data lokal.

---

## ✨ Fitur Utama

-   **Discover & Search**: Menemukan lagu yang sedang tren (global & per genre) atau mencari lagu spesifik menggunakan data dari **Last.fm API**.
-   **Local Playlist CRUD**: Fungsionalitas penuh untuk **Create, Read, Update, & Delete** playlist dan lagu di dalamnya. Semua data disimpan secara lokal menggunakan **SQLite**.
-   **Theme Customization**: Pilihan antara tema Terang, Gelap, atau mengikuti sistem, disimpan menggunakan **SharedPreferences**.
-   **Robust UI**: Antarmuka yang responsif dengan `RecyclerView` dan `Navigation Component`, serta memiliki penanganan error koneksi dengan tombol *refresh*.

---

## 🛠️ Tumpukan Teknologi (Tech Stack)

| Kategori | Teknologi |
| :--- | :--- |
| **Bahasa** | `Java` |
| **UI** | `Material Components`, `RecyclerView`, `BottomNavigationView` |
| **Navigasi** | `Android Jetpack Navigation Component` |
| **Networking** | `Retrofit2` & `GSON` |
| **Database** | `SQLite` (dengan `SQLiteOpenHelper`) |
| **Background Processing**| `ExecutorService` & `Handler` |
| **Image Loading** | `Glide` |
