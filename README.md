# IF2050-2025-K3M-RuangPandai

<p align="center">
  <a>
    <img src="ruang_pandai\img\logo_ruang_pandai.png" height="130px">
  </a>
</p>

Ruang Pandai adalah aplikasi desktop untuk manajemen jadwal tutor privat yang menghubungkan dua jenis pengguna utama: siswa dan tutor. Beberapa fitur utama yang dimiliki:
- Siswa dapat mencari tutor berdasarkan mata pelajaran, jadwal, dan rating
- Siswa dapat memesan sesi privat dengan tutor pilihan
- Tutor dapat mengelola jadwal 

## Cara Menjalankan Aplikasi
### Prasyarat
Pastikan beberapa perangkat lunak berikut terinstall di perangkat Anda:
- Java JDK 17+
- Maven 3.8+
- Git

### Langkah-Langkah
1. Buka **terminal** dan masukkan command berikut: ```git clone https://github.com/dzakyatha/IF2050-2025-K3M-RuangPandai```
2. Ketik ```cd IF2050-2025-K3M-RuangPandai``` untuk masuk ke dalam folder **IF2050-2025-K3M-RuangPandai**
3. Ketik ```cd ruang_pandai``` untuk masuk ke direktori **ruang_pandai**
4. Ketik ```mvn install``` untuk melakukan **Instalasi dependencies**
5. **Jalankan** aplikasi dengan mengetik ```mvn javafx:run```

## Daftar Modul yang Diimplementasi
### Daftar Modul
| Nama Modul    | Jenis 
|-------------|----------|
| Pengguna.java | Entity | 
| Tutor.java       | Entity | 
| Jadwal.java        | Entity | 
| Sesi.java       | Entity | 
| Pembayaran.java     | Entity | 
| SiswaController.java     | Controller |
| TutorController.java | Controller
| SiswaBoundary.java | Boundary
| TutorBoundary.java | Boundary
| MainBoundary.java | Boundary 

### Capture Screen Tampilan Layar
- SiswaBoundary.java
<p align="center">
  <a>
    <img src="ruang_pandai\doc\Tampilan awal halaman Cari Tutor.png" height="300px">
  </a>
</p>

- TutorBoundary.java
<p align="center">
  <a>
    <img src="ruang_pandai\doc\Tampilan awal halaman kelola jadwal.png" height="300px">
  </a>
</p>

- MainBoundary.java
<p align="center">
  <a>
    <img src="ruang_pandai\doc\Tampilan awal saat menjalankan aplikasi.png" height="300px">
  </a>
</p>

### Pembagian Tugas
| Nama     | NIM | Tugas |
|-------------|------------------------------|-|
| Kenzie Raffa Ardhana | 18223127 | Membuat kelas entity: Pengguna.java, Tutor.java, Jadwal.java, Sesi.java, Pembayaran.java |
| Muhammad Dzaky Atha F. | 18223124 | Membuat kelas boundary: SiswaBoundary.java, TutorBoundary.java, MainBoundary.java |
| Andi Syaichul Mubaraq | 18223139 | Membuat kelas controller: SiswaController.java, TutorController.java


## Skema Basis Data
- Tabel Pengguna

| Atribut     | Tipe Data | Keterangan |
|-------------|------------------------------|-|
| id_pengguna | Text | ID unik pengguna (Primary Key) |
| nama        | Text | Nama lengkap |
| role        | Text | Peran pengguna (Siswa/Tutor) |
| email       | Text | Email |
| no_telp     | Text | Nomor telepon |
| alamat      | Text | Alamat

- Tabel Siswa

| Atribut     | Tipe Data | Keterangan |
|-------------|------------------------------|-|
| id_pengguna | Text | ID unik siswa (Foreign key ke Pengguna) |

- Tabel Tutor

| Atribut     | Tipe Data | Keterangan |
|-------------|------------------------------|-|
| id_pengguna | Text | ID unik tutor (Foreign key ke Pengguna) |
| mata_pelajaran | Text | Mata pelajaran yang diampu |
| pendidikan     | Text | Tingkat pendidikan tutor |
| pengalaman       | Text | Pengalaman tutor |
| ulasan     | Integer | Ulasan tutor |
| rating      | Text | Rating tutor

- Tabel Jadwal

| Atribut     | Tipe Data | Keterangan |
|-------------|------------------------------|-|
| id_jadwal | Text | ID unik jadwal (Primary key) |
| id_tutor  | Text | ID unik tutor (Foreign key ke Tutor)
| mata_pelajaran | Text | Mata pelajaran jadwal |
| hari     | Text | Hari jadwal |
| tanggal       | Date | Tanggal jadwal |
| jam_mulai     | Time | Jam mulai jadwal |
| jam_selesai      | Time | Jam selesai jadwal
| status_jadwal | Text | Status jadwal

- Tabel Sesi

| Atribut     | Tipe Data | Keterangan |
|-------------|------------------------------|-|
| id_sesi | Text | ID unik sesi (Primary key)
| id_siswa | Text | ID unik siswa (Foreign key ke Siswa)
| id_tutor  | Text | ID unik tutor (Foreign key ke Tutor)
| id_jadwal | Text | ID unik jadwal (Foreign key ke Jadwal) |
| tanggal_pesan | Date | Tanggal pemesanan sesi |
| status_pembayaran  | Text | Status pembayaran sesi |
| status_kehadiran   | Text | Status kehadiran siswa |
| status_sesi     | Text | Status sesi |

- Tabel Pembayaran

| Atribut     | Tipe Data | Keterangan |
|-------------|------------------------------|-|
| id_pembayaran | Text | ID unik pembayaran (Primary key)
| id_sesi | Text | ID unik sesi (Foreign key ke Sesi)
| jumlah  | Integer | Total pembayaran
| metode_pembayaran | Text | Metode pembayaran |
| bukti_pembayaran | Blob | Bukti pembayaran (Gambar) |
| waktu_pembayaran   | Datetime | Waktu pembayaran |
| status_pembayaran  | Text | Status pembayaran sesi |

## Kontributor
<p align="center">
  <table>
    <tbody>
      <tr>
        <td align="center" valign="top" width="14.28%"><a href="https://github.com/dzakyatha"><img src="https://avatars.githubusercontent.com/u/164707111?v=4?s=100" width="100px;" alt="Muhammad Dzaky Atha F."/><br /><sub><b>Muhammad Dzaky Atha F.</b></sub><br /><sub><b>18223124</b></sub></a><br />   </td>
        <td align="center" valign="top" width="14.28%"><a href="https://github.com/kifu"><img src="https://avatars.githubusercontent.com/u/136690241?v=4?s=100" width="100px;" alt="Andi Syaichul Mubaraq"/><br /><sub><b>Andi Syaichul Mubaraq</b></sub><br /><sub><b>18223139</b></sub></a><br /> </td>
        <td align="center" valign="top" width="14.28%"><a href="https://github.com/rinmdfa25"><img src="https://avatars.githubusercontent.com/u/135858206?v=4?s=100" width="100px;" alt="Kenzie Raffa Ardhana"/><br /><sub><b>Kenzie Raffa Ardhana</b></sub><br /><sub><b>18223127</b></sub></a><br /> </td>
      </tr>
    </tbody>
  </table>
</p>

&nbsp;

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/javafx-%23FF0000.svg?style=for-the-badge&logo=javafx&logoColor=white)
![Apache Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)