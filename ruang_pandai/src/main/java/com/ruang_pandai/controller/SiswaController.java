package com.ruang_pandai.controller;

import com.ruang_pandai.database.DatabaseInitializer;
import com.ruang_pandai.entity.Jadwal;
import com.ruang_pandai.entity.Tutor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

public class SiswaController {

    // Corrected DB_URL
    private final String DB_URL = "jdbc:sqlite:src/main/resources/com/ruang_pandai/database/ruangpandai.db"; 
    private Connection conn;

    public SiswaController() {
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Gagal koneksi ke database: " + e.getMessage());
        }
    }

    // Method helper untuk menghasilkan ID berikutnya secara sekuensial.
    private String generateNextId(String prefix, String tableName, String columnName) {
        int nextNumber = 1;
        // Query ini mengambil ID terakhir berdasarkan urutan numerik.
        // ORDER BY LENGTH(columnName), Misal: columnName DESC memastikan 'S10' diurutkan setelah 'S9'.
        String query = "SELECT " + columnName + " FROM " + tableName + " ORDER BY LENGTH(" + columnName + ") DESC, " + columnName + " DESC LIMIT 1";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                String lastId = rs.getString(columnName);
                // Menghapus prefix untuk mendapatkan bagian angka dari ID
                String numberPart = lastId.replace(prefix, "");
                int lastNumber = Integer.parseInt(numberPart);
                nextNumber = lastNumber + 1;
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Gagal membuat ID baru, akan memulai dari 1. Error: " + e.getMessage());
            // Jika terjadi error (misal tabel kosong), defaultnya adalah 1.
        }
        
        return prefix + nextNumber;
    }

    // Method untuk mencari tutor berdasarkan filter
    // Filter 'namaTutor' is added to the parameters and query
    public List<Tutor> cariTutor(String mataPelajaran, Integer rating, String namaTutor, LocalDate tanggal, String waktuMulai) { 
        // // Jika pengguna tidak memberikan tanggal, tapi memberikan waktuMulai, kembalikan daftar kosong
        // if (tanggal == null && (waktuMulai != null && !waktuMulai.isEmpty())) {
        //     return new ArrayList<>(); // Kmbalikan daftar kosong
        // }

        List<Tutor> hasilPencarian = new ArrayList<>();

        // Langkah 1: Query untuk menemukan tutor yang cocok dengan filter
        StringBuilder sql = new StringBuilder(
            "SELECT p.id_pengguna, p.nama, p.role, p.email, p.no_telp, p.alamat, " +
            "t.mata_pelajaran, t.pendidikan, t.pengalaman, t.ulasan, t.rating " +
            "FROM Pengguna p JOIN Tutor t ON p.id_pengguna = t.id_pengguna WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        // Menambahkan filter dasar
        if (mataPelajaran != null && !mataPelajaran.isEmpty()) {
            sql.append(" AND t.mata_pelajaran = ?");
            params.add(mataPelajaran);
        }
        if (rating != null && rating > 0) {
            sql.append(" AND t.rating = ?");
            params.add(rating);
        }
        // Add filter for tutor name
        if (namaTutor != null && !namaTutor.trim().isEmpty()) {
            sql.append(" AND p.nama LIKE ?");
            params.add("%" + namaTutor + "%");
        }
        // Filter dengan tanggal dan waktu
        if (tanggal != null) {
            StringBuilder subQuery = new StringBuilder(" AND EXISTS (SELECT 1 FROM Jadwal j WHERE j.id_tutor = p.id_pengguna");

            subQuery.append(" AND j.tanggal = ?");
            params.add(tanggal.toString());

            if (waktuMulai != null && !waktuMulai.isEmpty()) {
                subQuery.append(" AND j.jam_mulai = ?");
                params.add(waktuMulai);
            }

            subQuery.append(")");
            sql.append(subQuery.toString());
        }


        // // Menambahkan subquery untuk filter jadwal berdasarkan hari (Assuming 'hari' was a previous filter, now using namaTutor)
        // if (hari != null && !hari.isEmpty()) {
        //     sql.append(" AND EXISTS (SELECT 1 FROM Jadwal j WHERE j.id_tutor = t.id_pengguna AND j.hari = ?)");
        //     params.add(hari.toUpperCase());
        // }

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                hasilPencarian.add(new Tutor(
                    rs.getString("id_pengguna"),
                    rs.getString("nama"),
                    rs.getString("role"),
                    rs.getString("email"),
                    rs.getString("no_telp"),
                    rs.getString("alamat"),
                    rs.getString("mata_pelajaran"),
                    rs.getString("pendidikan"),
                    rs.getString("pengalaman"),
                    rs.getString("ulasan"),
                    rs.getInt("rating"),
                    new ArrayList<>() // Jadwal diisi dengan list kosong sementara
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return hasilPencarian; // Kembalikan list kosong jika query pertama gagal
        }

        if (hasilPencarian.isEmpty()) {
            return hasilPencarian;
        }

        // Langkah 2: Ambil semua jadwal untuk tutor yang ditemukan dalam satu query
        List<String> tutorIds = hasilPencarian.stream().map(Tutor::getIdPengguna).collect(Collectors.toList());
        
        // Check if tutorIds is empty before creating placeholders to avoid SQL error
        if (tutorIds.isEmpty()) {
            return hasilPencarian; // No tutors found, so no schedules to fetch
        }

        String placeholders = tutorIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String jadwalSql = "SELECT * FROM Jadwal WHERE id_tutor IN (" + placeholders + ")";

        Map<String, List<Jadwal>> jadwalMap = new HashMap<>();
        try (PreparedStatement pstmtJadwal = conn.prepareStatement(jadwalSql)) {
            for (int i = 0; i < tutorIds.size(); i++) {
                pstmtJadwal.setString(i + 1, tutorIds.get(i));
            }
            ResultSet rsJadwal = pstmtJadwal.executeQuery();
            while (rsJadwal.next()) {
                String idTutor = rsJadwal.getString("id_tutor");
                Jadwal jadwal = new Jadwal(
                    rsJadwal.getString("id_jadwal"),
                    idTutor,
                    rsJadwal.getString("mata_pelajaran"),
                    rsJadwal.getString("hari"),
                    rsJadwal.getString("tanggal"),
                    rsJadwal.getString("jam_mulai"),
                    rsJadwal.getString("jam_selesai")
                );
                jadwalMap.computeIfAbsent(idTutor, k -> new ArrayList<>()).add(jadwal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Langkah 3: Pasangkan jadwal ke masing-masing tutor
        for (Tutor tutor : hasilPencarian) {
            tutor.setJadwal(jadwalMap.getOrDefault(tutor.getIdPengguna(), new ArrayList<>())); // Use getOrDefault
        }

        // System.out.println(hasilPencarian.size());
        return hasilPencarian;
    }

    // Method untuk memesan sesi dengan jadwal yang tersedia
    // Memesan sesi dan menyimpannya dengan status menunggu pembayaran.
    public boolean pesanSesi(String idSiswa, String idTutor, String idJadwal, String metodePembayaran, int jumlahPembayaran) {
        String idSesi = generateNextId("S", "Sesi", "id_sesi");
        String idPembayaran = generateNextId("PB", "Pembayaran", "id_pembayaran");

        String sqlInsertSesi = "INSERT INTO Sesi (id_sesi, id_siswa, id_tutor, id_jadwal, tanggal_pesan, status_pembayaran, status_kehadiran, status_sesi) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlInsertPembayaran = "INSERT INTO Pembayaran (id_pembayaran, id_sesi, jumlah, metode_pembayaran, bukti_pembayaran, waktu_pembayaran, status_pembayaran) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            // 1. Masukkan data ke tabel Sesi
            try (PreparedStatement pstmtSesi = conn.prepareStatement(sqlInsertSesi)) {
                pstmtSesi.setString(1, idSesi);
                pstmtSesi.setString(2, idSiswa);
                pstmtSesi.setString(3, idTutor);
                pstmtSesi.setString(4, idJadwal);
                pstmtSesi.setString(5, LocalDate.now().toString());
                pstmtSesi.setString(6, "SUDAH BAYAR"); // Direct to SUDAH BAYAR
                pstmtSesi.setString(7, "BELUM DIKONFIRMASI"); // Default
                pstmtSesi.setString(8, "AKAN DATANG"); // Default
                pstmtSesi.executeUpdate();
            }

            // 2. Masukkan data ke tabel Pembayaran
            try (PreparedStatement pstmtPembayaran = conn.prepareStatement(sqlInsertPembayaran)) {
                pstmtPembayaran.setString(1, idPembayaran);
                pstmtPembayaran.setString(2, idSesi);
                pstmtPembayaran.setInt(3, jumlahPembayaran);
                pstmtPembayaran.setString(4, metodePembayaran);
                pstmtPembayaran.setString(5, "dummy_bukti_digital.jpg"); // Dummy proof
                pstmtPembayaran.setString(6, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                pstmtPembayaran.setString(7, "BERHASIL");
                pstmtPembayaran.executeUpdate();
            }

            conn.commit();
            System.out.println("Sesi " + idSesi + " berhasil dipesan dan dibayar.");
            return true;
        } catch (SQLException e) {
            System.err.println("Transaksi pemesanan dan pembayaran gagal: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback gagal: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Gagal mengembalikan autocommit: " + e.getMessage());
            }
        }
    }

    // Method untuk mendapatkan waktu mulai sesi berdasarkan ID sesi
    private LocalDateTime getSessionStartTime(String idSesi) throws SQLException {
        String sql = "SELECT j.tanggal, j.jam_mulai FROM Sesi s JOIN Jadwal j ON s.id_jadwal = j.id_jadwal WHERE s.id_sesi = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idSesi);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                LocalDate date = LocalDate.parse(rs.getString("tanggal"));
                LocalTime time = LocalTime.parse(rs.getString("jam_mulai"));
                return LocalDateTime.of(date, time);
            }
        }
        return null;
    }

    // Method untuk membatalkan sesi berdasarkan ID sesi
    // Siswa dapat membatalkan sesi yang telah dipesan sebelumnya. (Minimal 12 jam sebelum sesi dimulai)
    public boolean batalkanSesi(String idSesi) {
        try {
            LocalDateTime sessionStartTime = getSessionStartTime(idSesi);
            if (sessionStartTime == null) {
                System.out.println("Sesi " + idSesi + " tidak ditemukan.");
                return false;
            }

            long hoursUntilSession = Duration.between(LocalDateTime.now(), sessionStartTime).toHours();
            if (hoursUntilSession < 12) {
                System.out.println("Pembatalan gagal: Sesi hanya dapat dibatalkan paling lambat 12 jam sebelum dimulai.");
                return false;
            }

            String sqlUpdateSesi = "UPDATE Sesi SET status_sesi = 'DIBATALKAN', status_pembayaran = 'DIBATALKAN' WHERE id_sesi = ? AND status_sesi = 'AKAN DATANG'";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateSesi)) {
                pstmt.setString(1, idSesi);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Sesi " + idSesi + " berhasil dibatalkan.");
                    return true;
                } else {
                    System.out.println("Sesi " + idSesi + " tidak dapat dibatalkan (status sesi bukan 'AKAN DATANG').");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Pembatalan sesi gagal: " + e.getMessage());
            return false;
        }
    }

    // Method untuk menjadwal ulang sesi berdasarkan ID sesi dan ID jadwal baru
    // Siswa dapat menjadwalkan ulang sesi yang telah dipesan sebelumnya. (Minimal 12 jam sebelum sesi dimulai)
    public boolean jadwalUlangSesi(String idSesi, String idJadwalBaru) {
        try {
            // 1. Check h-12 jam sebelum sesi dimulai
            LocalDateTime sessionStartTime = getSessionStartTime(idSesi);
            if (sessionStartTime == null) {
                System.out.println("Sesi " + idSesi + " tidak ditemukan.");
                return false;
            }

            long hoursUntilSession = Duration.between(LocalDateTime.now(), sessionStartTime).toHours();
            if (hoursUntilSession < 12) {
                System.out.println("Penjadwalan ulang gagal: Sesi hanya dapat dijadwalkan ulang paling lambat 12 jam sebelum dimulai.");
                return false;
            }

            // 2. Check apakah jadwal baru milik tutor yang sama
            String originalTutorId = null;
            String newTutorId = null;

            // Buat dapetin ID tutor dari sesi yang ada
            String sqlGetOriginalTutor = "SELECT id_tutor FROM Sesi WHERE id_sesi = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlGetOriginalTutor)) {
                pstmt.setString(1, idSesi);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    originalTutorId = rs.getString("id_tutor");
                }
            }

            // Buat dapetin ID tutor dari jadwal baru
            String sqlGetNewTutor = "SELECT id_tutor FROM Jadwal WHERE id_jadwal = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlGetNewTutor)) {
                pstmt.setString(1, idJadwalBaru);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    newTutorId = rs.getString("id_tutor");
                }
            }
            
            // Validasi apakah keduanya sama
            if (originalTutorId == null || newTutorId == null || !originalTutorId.equals(newTutorId)) {
                System.out.println("Penjadwalan ulang gagal: Sesi hanya dapat dijadwalkan ulang ke jadwal milik tutor yang sama.");
                return false;
            }

            // 3. Update jika validasi berhasil
            String sqlUpdateSesi = "UPDATE Sesi SET id_jadwal = ?, tanggal_pesan = ? WHERE id_sesi = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateSesi)) {
                pstmt.setString(1, idJadwalBaru);
                pstmt.setString(2, LocalDate.now().toString());
                pstmt.setString(3, idSesi);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Sesi " + idSesi + " berhasil dijadwalkan ulang ke jadwal " + idJadwalBaru + ".");
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Penjadwalan ulang sesi gagal: " + e.getMessage());
            return false;
        }
    }

    // --- METODE HELPER UNTUK MENAMPILKAN DATA ---
    private void tampilkanSemuaTutor() {
        String sql = "SELECT p.id_pengguna, p.nama, t.mata_pelajaran, t.rating FROM Pengguna p JOIN Tutor t ON p.id_pengguna = t.id_pengguna";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            boolean found = false;
            while(rs.next()) {
                found = true;
                System.out.printf("  - ID: %-5s | Nama: %-15s | Mapel: %-15s | Rating: %d\n",
                    rs.getString("id_pengguna"),
                    rs.getString("nama"),
                    rs.getString("mata_pelajaran"),
                    rs.getInt("rating"));
            }
            if (!found) System.out.println("  -> Tidak ada data tutor.");
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data tutor: " + e.getMessage());
        }
    }

    private void tampilkanSemuaSesi() {
        String sql = "SELECT id_sesi, id_siswa, id_tutor, id_jadwal, status_pembayaran, status_sesi FROM Sesi";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            boolean found = false;
            while(rs.next()) {
                found = true;
                System.out.printf("  - ID: %-5s | Siswa: %-5s | Tutor: %-5s | Jadwal: %-5s | Status Bayar: %-20s | Status Sesi: %s\n",
                    rs.getString("id_sesi"),
                    rs.getString("id_siswa"),
                    rs.getString("id_tutor"),
                    rs.getString("id_jadwal"),
                    rs.getString("status_pembayaran"),
                    rs.getString("status_sesi"));
            }
            if (!found) System.out.println("  -> Tidak ada data sesi.");
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data sesi: " + e.getMessage());
        }
    }

    private void tampilkanSemuaPembayaran() {
        String sql = "SELECT id_pembayaran, id_sesi, jumlah, metode_pembayaran, status_pembayaran FROM Pembayaran";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            boolean found = false;
            while(rs.next()) {
                found = true;
                System.out.printf("  - ID: %-5s | Sesi: %-5s | Jumlah: %-9d | Metode: %-15s | Status: %s\n",
                    rs.getString("id_pembayaran"),
                    rs.getString("id_sesi"),
                    rs.getInt("jumlah"),
                    rs.getString("metode_pembayaran"),
                    rs.getString("status_pembayaran"));
            }
            if (!found) System.out.println("  -> Tidak ada data pembayaran.");
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data pembayaran: " + e.getMessage());
        }
    }

    /* * Cara menjalankan static void main() di SiswaController:
     * 1. Copy ini (sebelum baris </plugins>) ke dalam pom.xml:
     <plugin>
     <groupId>org.codehaus.mojo</groupId>
     <artifactId>exec-maven-plugin</artifactId>
     <version>3.2.0</version>
     <configuration>
     <mainClass>com.ruang_pandai.controller.SiswaController</mainClass>
     </configuration>
     </plugin>
     * 2. Buka terminal di direktori proyek:
     * cd ruang_pandai
     * kemudian:
     * mvn compile exec:java
     */

    /*
     * Ini untuk testing skenario pemesanan sesi
     */
    public static void main(String[] args) {
        System.setProperty("app.env", "development");
        DatabaseInitializer.initialize();

        SiswaController controller = new SiswaController();
        if (controller.conn == null) {
            System.out.println("Koneksi database gagal, tidak dapat melanjutkan tes.");
            return;
        }

        System.out.println("\n=======================================================");
        System.out.println("--- 1. KONDISI AWAL DATABASE ---");
        System.out.println("=======================================================");
        System.out.println("\n[A] Daftar Tutor Awal:");
        controller.tampilkanSemuaTutor();
        System.out.println("\n[B] Daftar Sesi Awal:");
        controller.tampilkanSemuaSesi();
        System.out.println("\n[C] Daftar Pembayaran Awal:");
        controller.tampilkanSemuaPembayaran();

        System.out.println("\n\n=======================================================");
        System.out.println("--- 2. MENJALANKAN SEMUA SKENARIO ---");
        System.out.println("=======================================================");

        System.out.println("\n--- SKENARIO UTAMA: PEMESANAN SESI ---");
        System.out.println("[+] Siswa mencari tutor 'Bahasa Inggris'...");
        List<Tutor> hasilCari = controller.cariTutor("Bahasa Inggris", null, null, null, null);

        if (hasilCari.isEmpty()) {
            System.out.println("Hasil: Tidak ada tutor yang ditemukan. Skenario berhenti.");
        } else {
            Tutor tutorPilihan = hasilCari.get(0);
            Jadwal jadwalPilihan = tutorPilihan.getJadwal().get(0);
            System.out.println("  -> Tutor ditemukan: " + tutorPilihan.getNama() + " dengan jadwal ID: " + jadwalPilihan.getIdJadwal());

            System.out.println("[+] Siswa 'P1' (Budi Santoso) memesan sesi dan langsung membayar...");
            boolean pemesananBerhasil = controller.pesanSesi("P1", tutorPilihan.getIdPengguna(), jadwalPilihan.getIdJadwal(), "Gopay", 175000);

            if (!pemesananBerhasil) {
                System.out.println("  -> Gagal memesan dan membayar sesi.");
            }
        }
        
        System.out.println("\n\n--- SKENARIO ALTERNATIF 1: MENJADWAL ULANG SESI ---");
        System.out.println("[+] Siswa mencoba menjadwal ulang Sesi 'S1' (milik Tutor P3) ke Jadwal 'J3' (milik Tutor P4). Harusnya GAGAL.");
        controller.jadwalUlangSesi("S1", "J3");

        System.out.println("\n[+] Siswa mencoba menjadwal ulang Sesi 'S1' (milik Tutor P3) ke Jadwal 'J2' (milik Tutor P3). Harusnya BERHASIL.");
        controller.jadwalUlangSesi("S1", "J2");

        System.out.println("\n\n--- SKENARIO ALTERNATIF 2: MEMBATALKAN PEMESANAN ---");
        System.out.println("[+] Siswa mencoba membatalkan Sesi 'S1' yang aktif (jadwal di masa depan). Harusnya BERHASIL.");
        controller.batalkanSesi("S1");
        
        System.out.println("\n[+] Siswa mencoba membatalkan Sesi 'S1' lagi setelah dibatalkan. Harusnya GAGAL.");
        controller.batalkanSesi("S1");
        
        System.out.println("\n\n=======================================================");
        System.out.println("--- 3. KONDISI AKHIR DATABASE ---");
        System.out.println("=======================================================");
        System.out.println("\n[A] Daftar Tutor Akhir:");
        controller.tampilkanSemuaTutor();
        System.out.println("\n[B] Daftar Sesi Akhir:");
        controller.tampilkanSemuaSesi();
        System.out.println("\n[C] Daftar Pembayaran Akhir:");
        controller.tampilkanSemuaPembayaran();

        System.out.println("\n\n--- SEMUA SKENARIO SELESAI ---");

        try {
            if (controller.conn != null && !controller.conn.isClosed()) {
                controller.conn.close();
                System.out.println("\nKoneksi ke database ditutup.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * Ini untuk testing skenario pencarian tutor
     */
    // public static void main(String[] args) {
    //     System.setProperty("app.env", "development");
    //     DatabaseInitializer.initialize();

    //     SiswaController controller = new SiswaController();
    //     if (controller.conn == null) {
    //         System.out.println("Koneksi database gagal.");
    //         return;
    //     }
        
    //     System.out.println("\n--- Skenario 1: Mencari tutor tanpa filter ---");
    //     // Mengambil semua tutor tanpa filter
    //     List<Tutor> semuaTutor = controller.cariTutor(null, null, null); // Adjusted for new parameters
    //     if (semuaTutor.isEmpty()) {
    //         System.out.println("Tidak ada tutor yang ditemukan.");
    //     } else {
    //         semuaTutor.forEach(t -> {
    //             System.out.println("Ditemukan: " + t.getNama() + " | Mata Pelajaran: " + t.getMataPelajaran());
    //             // Menampilkan jadwal yang berhasil diambil
    //             if (t.getJadwal() != null && !t.getJadwal().isEmpty()) {
    //                 System.out.println("  Jadwal yang tersedia:");
    //                 t.getJadwal().forEach(j -> {
    //                     System.out.println("   -> " + j.getHari() + ", " + j.getTanggal() + " (" + j.getJamMulai() + " - " + j.getJamSelesai() + ")");
    //                 });
    //             } else {
    //                 System.out.println("  -> Tidak ada detail jadwal yang ditemukan untuk tutor ini.");
    //             }
    //         });
    //     }

    //     System.out.println("\n--- Skenario 2: Mencari tutor yang tersedia hari RABU (Filter by name instead) ---");
    //     // Example: Search by name "Citra"
    //     List<Tutor> tutorsCitra = controller.cariTutor(null, null, "Citra"); // Adjusted for new parameters
    //     if (tutorsCitra.isEmpty()) {
    //         System.out.println("Tidak ada tutor yang sesuai dengan kriteria pencarian.");
    //     } else {
    //         tutorsCitra.forEach(t -> {
    //             System.out.println("Ditemukan: " + t.getNama() + " (" + t.getMataPelajaran() + ")");
    //             // Menampilkan jadwal yang berhasil diambil
    //              if (t.getJadwal() != null && !t.getJadwal().isEmpty()) {
    //                 System.out.println("  Jadwal: ");
    //                 t.getJadwal().forEach(j -> {
    //                         System.out.println("   -> " + j.getHari() + ", " + j.getTanggal() + " (" + j.getJamMulai() + " - " + j.getJamSelesai() + ")");
    //                     });
    //             } else {
    //                 System.out.println("  -> Tidak ada detail jadwal yang ditemukan untuk tutor ini.");
    //             }
    //         });
    //     }

    //     System.out.println("\n--- Skenario 3: Mencari tutor Bahasa Inggris dengan rating 4 ---");
    //     List<Tutor> tutorsInggris = controller.cariTutor("Bahasa Inggris", 4, null); // Adjusted for new parameters
    //     if (tutorsInggris.isEmpty()) {
    //         System.out.println("Tidak ada tutor yang sesuai dengan kriteria pencarian.");
    //     } else {
    //         tutorsInggris.forEach(t -> {
    //             System.out.println("Ditemukan: " + t.getNama() + " | Rating: " + t.getRating() + " | Pendidikan: " + t.getPendidikan());
    //             // Menampilkan jadwal yang berhasil diambil
    //             if (t.getJadwal() != null && !t.getJadwal().isEmpty()) {
    //                 System.out.println("  Jadwal yang tersedia:");
    //                 t.getJadwal().forEach(j -> {
    //                     System.out.println("   -> " + j.getHari() + ", " + j.getTanggal() + " (" + j.getJamMulai() + " - " + j.getJamSelesai() + ")");
    //                 });
    //             } else {
    //                 System.out.println("  -> Tidak ada detail jadwal yang ditemukan untuk tutor ini.");
    //             }
    //         });
    //     }


    //     System.out.println("\n--- Skenario 4: Mencari tutor dengan filter yang tidak ada ---");
    //     List<Tutor> tutorTidakAda = controller.cariTutor("Kimia", 5, "NonExistentTutor"); // Adjusted
    //     if (tutorTidakAda.isEmpty()) {
    //         System.out.println("Tidak ada tutor yang sesuai dengan kriteria pencarian.");
    //     } else {
    //         tutorTidakAda.forEach(t -> {
    //             System.out.println("Ditemukan: " + t.getNama() + " | Mata Pelajaran: " + t.getMataPelajaran());
    //             // Menampilkan jadwal yang berhasil diambil
    //             if (t.getJadwal() != null && !t.getJadwal().isEmpty()) {
    //                 System.out.println("  Jadwal yang tersedia:");
    //                 t.getJadwal().forEach(j -> {
    //                     System.out.println("   -> " + j.getHari() + ", " + j.getTanggal() + " (" + j.getJamMulai() + " - " + j.getJamSelesai() + ")");
    //                 });
    //             } else {
    //                 System.out.println("  -> Tidak ada detail jadwal yang ditemukan untuk tutor ini.");
    //             }
    //         });
    //     }
    // }
}