-- Tabel Pengguna (base class)
CREATE TABLE IF NOT EXISTS Pengguna (
    id_pengguna TEXT NOT NULL PRIMARY KEY,
    nama TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('SISWA', 'TUTOR')),
    email TEXT NOT NULL UNIQUE,
    no_telp TEXT NOT NULL,
    alamat TEXT NOT NULL
);

-- Tabel Siswa (inherit Pengguna)
CREATE TABLE IF NOT EXISTS Siswa (
    id_pengguna TEXT NOT NULL PRIMARY KEY,
    FOREIGN KEY (id_pengguna) REFERENCES Pengguna(id_pengguna)
);

-- Tabel Tutor (inherit Pengguna)
CREATE TABLE IF NOT EXISTS Tutor (
    id_pengguna TEXT NOT NULL PRIMARY KEY,
    mata_pelajaran TEXT NOT NULL,
    pendidikan TEXT NOT NULL,
    pengalaman TEXT NOT NULL,
    ulasan TEXT,
    rating INTEGER,
    FOREIGN KEY (id_pengguna) REFERENCES Pengguna(id_pengguna)
);

-- Tabel Jadwal
CREATE TABLE IF NOT EXISTS Jadwal (
    id_jadwal TEXT NOT NULL PRIMARY KEY,
    id_tutor TEXT NOT NULL,
    mata_pelajaran TEXT NOT NULL,
    hari TEXT CHECK(hari IN ('SENIN', 'SELASA', 'RABU', 'KAMIS', 'JUMAT', 'SABTU', 'MINGGU')),
    tanggal DATE NOT NULL,
    jam_mulai TIME NOT NULL,
    jam_selesai TIME NOT NULL,
    status_jadwal TEXT DEFAULT 'TERSEDIA' CHECK(status_jadwal IN ('TERSEDIA', 'DIPESAN')),
    FOREIGN KEY (id_tutor) REFERENCES Tutor(id_pengguna)
);

-- Tabel Sesi
CREATE TABLE IF NOT EXISTS Sesi (
    id_sesi TEXT NOT NULL PRIMARY KEY,
    id_siswa TEXT NOT NULL,
    id_tutor TEXT NOT NULL,
    id_jadwal TEXT NOT NULL,
    tanggal_pesan DATE NOT NULL,
    status_pembayaran TEXT CHECK(status_pembayaran IN ('SUDAH BAYAR', 'DIBATALKAN')),
    status_kehadiran TEXT CHECK(status_kehadiran IN ('HADIR', 'TIDAK HADIR', 'BELUM DIKONFIRMASI')),
    status_sesi TEXT CHECK(status_sesi IN ('AKAN DATANG', 'SELESAI', 'DIBATALKAN')),
    FOREIGN KEY (id_siswa) REFERENCES Siswa(id_pengguna),
    FOREIGN KEY (id_tutor) REFERENCES Tutor(id_pengguna),
    FOREIGN KEY (id_jadwal) REFERENCES Jadwal(id_jadwal)
);

-- Tabel Pembayaran
CREATE TABLE IF NOT EXISTS Pembayaran (
    id_pembayaran TEXT NOT NULL PRIMARY KEY,
    id_sesi TEXT NOT NULL,
    jumlah INTEGER NOT NULL,
    metode_pembayaran TEXT NOT NULL,
    bukti_pembayaran BLOB,
    waktu_pembayaran DATETIME NOT NULL,
    status_pembayaran TEXT NOT NULL CHECK(status_pembayaran IN ('BERHASIL', 'GAGAL')),
    FOREIGN KEY (id_sesi) REFERENCES Sesi(id_sesi)
);
