package com.ruang_pandai.controller;

import com.ruang_pandai.entity.Jadwal;
import com.ruang_pandai.entity.Sesi;
import com.ruang_pandai.entity.Tutor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class SiswaController {

    private final String DB_URL = "jdbc:sqlite:src/main/resources/com/ruang_pandai/database/ruangpandai.db";
    private Connection conn;

    public SiswaController() {
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Gagal koneksi ke database: " + e.getMessage());
        }
    }

    // Method helper untuk menghasilkan ID berikutnya secara sekuensial
    private String generateNextId(String prefix, String tableName, String columnName) {
        int nextNumber = 1;
        String query = "SELECT " + columnName + " FROM " + tableName + " ORDER BY LENGTH(" + columnName + ") DESC, " + columnName + " DESC LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String lastId = rs.getString(columnName);
                String numberPart = lastId.replace(prefix, "");
                nextNumber = Integer.parseInt(numberPart) + 1;
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Gagal membuat ID baru untuk " + tableName + ", akan memulai dari 1. Error: " + e.getMessage());
        }
        return prefix + nextNumber;
    }

    public List<Tutor> cariTutor(String mataPelajaran, Integer rating, String namaTutor, LocalDate tanggal, String waktuMulai) {
        List<Tutor> hasilPencarian = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.id_pengguna, p.nama, p.role, p.email, p.no_telp, p.alamat, " +
            "t.mata_pelajaran, t.pendidikan, t.pengalaman, t.ulasan, t.rating " +
            "FROM Pengguna p JOIN Tutor t ON p.id_pengguna = t.id_pengguna WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        // Filter utama (Mata pelajaran/Rating/Nama)
        if (mataPelajaran != null && !mataPelajaran.isEmpty()) {
            sql.append(" AND t.mata_pelajaran = ?");
            params.add(mataPelajaran);
        }
        if (rating != null && rating > 0) {
            sql.append(" AND t.rating = ?");
            params.add(rating);
        }
        if (namaTutor != null && !namaTutor.trim().isEmpty()) {
            sql.append(" AND p.nama LIKE ?");
            params.add("%" + namaTutor + "%");
        }

        // Filter Jadwal
        // Subquery untuk jadwal hanya ditambahkan jika pengguna mengisi filter tanggal atau waktu.
        if (tanggal != null || (waktuMulai != null && !waktuMulai.isEmpty())) {
            StringBuilder subQuery = new StringBuilder(" AND EXISTS (SELECT 1 FROM Jadwal j WHERE j.id_tutor = p.id_pengguna AND j.status_jadwal = 'TERSEDIA'");
            if (tanggal != null) {
                subQuery.append(" AND j.tanggal = ?");
                params.add(tanggal.toString());
            }
            if (waktuMulai != null && !waktuMulai.isEmpty()) {
                subQuery.append(" AND j.jam_mulai = ?");
                params.add(waktuMulai);
            }
            subQuery.append(")");
            sql.append(subQuery.toString());
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                hasilPencarian.add(new Tutor(
                    rs.getString("id_pengguna"), rs.getString("nama"), rs.getString("role"),
                    rs.getString("email"), rs.getString("no_telp"), rs.getString("alamat"),
                    rs.getString("mata_pelajaran"), rs.getString("pendidikan"), rs.getString("pengalaman"),
                    rs.getString("ulasan"), rs.getInt("rating"), new ArrayList<>() // Jadwal diisi list kosong sementara
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return hasilPencarian;
        }

        if (hasilPencarian.isEmpty()) {
            return hasilPencarian;
        }

        List<String> tutorIds = hasilPencarian.stream().map(Tutor::getIdPengguna).collect(Collectors.toList());
        String placeholders = tutorIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        
        String jadwalSql = "SELECT * FROM Jadwal WHERE id_tutor IN (" + placeholders + ") AND status_jadwal = 'TERSEDIA'";
        
        Map<String, List<Jadwal>> jadwalMap = new HashMap<>();
        try (PreparedStatement pstmtJadwal = conn.prepareStatement(jadwalSql)) {
            for (int i = 0; i < tutorIds.size(); i++) {
                pstmtJadwal.setString(i + 1, tutorIds.get(i));
            }
            ResultSet rsJadwal = pstmtJadwal.executeQuery();
            while (rsJadwal.next()) {
                Jadwal jadwal = new Jadwal(
                    rsJadwal.getString("id_jadwal"), rsJadwal.getString("id_tutor"), rsJadwal.getString("mata_pelajaran"),
                    rsJadwal.getString("hari"), rsJadwal.getString("tanggal"), rsJadwal.getString("jam_mulai"),
                    rsJadwal.getString("jam_selesai"), rsJadwal.getString("status_jadwal")
                );
                jadwalMap.computeIfAbsent(jadwal.getIdTutor(), k -> new ArrayList<>()).add(jadwal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Tutor tutor : hasilPencarian) {
            tutor.setJadwal(jadwalMap.getOrDefault(tutor.getIdPengguna(), new ArrayList<>()));
        }
        
        return hasilPencarian;
}

    public boolean pesanSesi(String idSiswa, String idTutor, String idJadwal, String metodePembayaran, int jumlahPembayaran) {
        String checkJadwalSql = "SELECT status_jadwal FROM Jadwal WHERE id_jadwal = ?";
        String updateJadwalSql = "UPDATE Jadwal SET status_jadwal = 'DIPESAN' WHERE id_jadwal = ? AND status_jadwal = 'TERSEDIA'";
        String insertSesiSql = "INSERT INTO Sesi (id_sesi, id_siswa, id_tutor, id_jadwal, tanggal_pesan, status_pembayaran, status_kehadiran, status_sesi) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertPembayaranSql = "INSERT INTO Pembayaran (id_pembayaran, id_sesi, jumlah, metode_pembayaran, bukti_pembayaran, waktu_pembayaran, status_pembayaran) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try {
            // 1. Periksa ketersediaan jadwal SEBELUM transaksi
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkJadwalSql)) {
                pstmtCheck.setString(1, idJadwal);
                ResultSet rs = pstmtCheck.executeQuery();
                if (rs.next()) {
                    if (!"TERSEDIA".equals(rs.getString("status_jadwal"))) {
                        System.err.println("Pemesanan gagal: Jadwal " + idJadwal + " sudah tidak tersedia.");
                        return false;
                    }
                } else {
                    System.err.println("Pemesanan gagal: Jadwal " + idJadwal + " tidak ditemukan.");
                    return false;
                }
            }

            // 2. Memulai transaksi
            conn.setAutoCommit(false);

            // 3. Update status jadwal menjadi 'DIPESAN'
            try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateJadwalSql)) {
                pstmtUpdate.setString(1, idJadwal);
                int affectedRows = pstmtUpdate.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Gagal mengunci jadwal, mungkin sudah dipesan oleh orang lain saat proses berlangsung.");
                }
            }

            // 4. Masukkan data Sesi dan Pembayaran
            String idSesi = generateNextId("S", "Sesi", "id_sesi");
            try (PreparedStatement pstmtSesi = conn.prepareStatement(insertSesiSql)) {
                pstmtSesi.setString(1, idSesi);
                pstmtSesi.setString(2, idSiswa);
                pstmtSesi.setString(3, idTutor);
                pstmtSesi.setString(4, idJadwal);
                pstmtSesi.setString(5, LocalDate.now().toString());
                pstmtSesi.setString(6, "SUDAH BAYAR");
                pstmtSesi.setString(7, "BELUM DIKONFIRMASI");
                pstmtSesi.setString(8, "AKAN DATANG");
                pstmtSesi.executeUpdate();
            }
            
            String idPembayaran = generateNextId("PB", "Pembayaran", "id_pembayaran");
            try (PreparedStatement pstmtPembayaran = conn.prepareStatement(insertPembayaranSql)) {
                pstmtPembayaran.setString(1, idPembayaran);
                pstmtPembayaran.setString(2, idSesi);
                pstmtPembayaran.setInt(3, jumlahPembayaran);
                pstmtPembayaran.setString(4, metodePembayaran);
                pstmtPembayaran.setString(5, "dummy_bukti_digital.jpg");
                pstmtPembayaran.setString(6, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                pstmtPembayaran.setString(7, "BERHASIL");
                pstmtPembayaran.executeUpdate();
            }

            // 5. Commit transaksi
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Transaksi pemesanan gagal: " + e.getMessage());
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

    public boolean batalkanSesi(String idSesi) {
        String idJadwalTerkait = null;
        String getJadwalSql = "SELECT id_jadwal FROM Sesi WHERE id_sesi = ? AND status_sesi = 'AKAN DATANG'";
        
        try (PreparedStatement pstmt = conn.prepareStatement(getJadwalSql)) {
            pstmt.setString(1, idSesi);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                idJadwalTerkait = rs.getString("id_jadwal");
            }
        } catch (SQLException e) {
             System.err.println("Gagal mendapatkan id_jadwal terkait: " + e.getMessage());
             return false;
        }

        if (idJadwalTerkait == null) {
            System.err.println("Pembatalan gagal: Sesi tidak ditemukan atau statusnya bukan 'AKAN DATANG'.");
            return false;
        }

        if (!isJadwalUlangSesiAvailable(idSesi)) {
            System.err.println("Pembatalan gagal: Sesi hanya dapat dibatalkan paling lambat 12 jam sebelum dimulai.");
            return false;
        }
        
        try {
            conn.setAutoCommit(false);
            
            // Update status Sesi
            String sqlUpdateSesi = "UPDATE Sesi SET status_sesi = 'DIBATALKAN', status_pembayaran = 'DIBATALKAN' WHERE id_sesi = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateSesi)) {
                pstmt.setString(1, idSesi);
                pstmt.executeUpdate();
            }
            
            // Update status Jadwal kembali menjadi TERSEDIA
            updateJadwalStatus(idJadwalTerkait, "TERSEDIA");
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Transaksi pembatalan sesi gagal: " + e.getMessage());
            try { conn.rollback(); } catch (SQLException ex) { System.err.println("Rollback gagal: " + ex.getMessage()); }
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { System.err.println("Gagal mengembalikan autocommit: " + e.getMessage()); }
        }
    }

    public boolean jadwalUlangSesi(String idSesi, String idJadwalBaru) {
        String idJadwalLama = null;
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT id_jadwal FROM Sesi WHERE id_sesi = ?")) {
            pstmt.setString(1, idSesi);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                idJadwalLama = rs.getString("id_jadwal");
            }
        } catch (SQLException e) {
             System.err.println("Gagal mendapatkan id_jadwal lama: " + e.getMessage());
             return false;
        }
        
        if (idJadwalLama == null || !isJadwalUlangSesiAvailable(idSesi)) {
            return false;
        }

        // Validasi tambahan: pastikan jadwal baru tersedia
        if (!isJadwalAvailable(idJadwalBaru)) {
             System.err.println("Penjadwalan ulang gagal: Jadwal baru sudah tidak tersedia.");
             return false;
        }
        
        try {
            conn.setAutoCommit(false);
            
            // Bebaskan jadwal lama
            updateJadwalStatus(idJadwalLama, "TERSEDIA");
            
            // Pesan jadwal baru
            updateJadwalStatus(idJadwalBaru, "DIPESAN");
            
            // Update Sesi untuk menunjuk ke jadwal baru
            String sqlUpdateSesi = "UPDATE Sesi SET id_jadwal = ?, tanggal_pesan = ? WHERE id_sesi = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateSesi)) {
                pstmt.setString(1, idJadwalBaru);
                pstmt.setString(2, LocalDate.now().toString());
                pstmt.setString(3, idSesi);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
             System.err.println("Transaksi penjadwalan ulang gagal: " + e.getMessage());
            try { conn.rollback(); } catch (SQLException ex) { System.err.println("Rollback gagal: " + ex.getMessage()); }
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { System.err.println("Gagal mengembalikan autocommit: " + e.getMessage()); }
        }
    }

    // Metode helper
    private void updateJadwalStatus(String idJadwal, String status) throws SQLException {
        String sql = "UPDATE Jadwal SET status_jadwal = ? WHERE id_jadwal = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, idJadwal);
            pstmt.executeUpdate();
        }
    }

    private boolean isJadwalAvailable(String idJadwal) {
        String sql = "SELECT status_jadwal FROM Jadwal WHERE id_jadwal = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idJadwal);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "TERSEDIA".equals(rs.getString("status_jadwal"));
            }
        } catch (SQLException e) {
            System.err.println("Error saat cek ketersediaan jadwal: " + e.getMessage());
        }
        return false;
    }

    public boolean isJadwalUlangSesiAvailable(String idSesi) {
        try {
            LocalDateTime sessionStartTime = getJamMulaiSesi(idSesi);
            if (sessionStartTime == null) return false;
            return Duration.between(LocalDateTime.now(), sessionStartTime).toHours() >= 12;
        } catch (SQLException e) {
            System.err.println("Error saat mengecek kelayakan sesi: " + e.getMessage());
            return false;
        }
    }

    private LocalDateTime getJamMulaiSesi(String idSesi) throws SQLException {
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

    // Method untuk retrieve data sesi
    public List<Sesi> getSesiByIdSiswaAndStatus(String idSiswa, String statusSesi) {
        List<Sesi> daftarSesi = new ArrayList<>();
        
        String sql = "SELECT id_sesi, id_siswa, id_tutor, id_jadwal, tanggal_pesan, " +
                     "status_pembayaran, status_kehadiran, status_sesi " +
                     "FROM Sesi WHERE id_siswa = ? AND status_sesi = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, idSiswa);
            pstmt.setString(2, statusSesi);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Sesi sesi = new Sesi(
                    rs.getString("id_sesi"),
                    rs.getString("id_siswa"),
                    rs.getString("id_tutor"),
                    rs.getString("id_jadwal"),
                    rs.getString("tanggal_pesan"),
                    rs.getString("status_pembayaran"),
                    rs.getString("status_kehadiran"),
                    rs.getString("status_sesi")
                );
                
                daftarSesi.add(sesi);
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data sesi: " + e.getMessage());
        }

        return daftarSesi;
    }

    public Tutor getTutorById(String idTutor) {
        String sql = "SELECT p.id_pengguna, p.nama, p.role, p.email, p.no_telp, p.alamat, " +
                     "t.mata_pelajaran, t.pendidikan, t.pengalaman, t.ulasan, t.rating " +
                     "FROM Pengguna p JOIN Tutor t ON p.id_pengguna = t.id_pengguna WHERE p.id_pengguna = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idTutor);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Tutor(
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
                    new ArrayList<>() // Jadwal diisi list kosong
                );
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data tutor by ID: " + e.getMessage());
        }
        return null;
    }

    public Jadwal getJadwalById(String idJadwal) {
        String sql = "SELECT * FROM Jadwal WHERE id_jadwal = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idJadwal);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Jadwal(
                    rs.getString("id_jadwal"), rs.getString("id_tutor"), rs.getString("mata_pelajaran"),
                    rs.getString("hari"), rs.getString("tanggal"), rs.getString("jam_mulai"),
                    rs.getString("jam_selesai"), rs.getString("status_jadwal") // Pastikan ini terisi
                );
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data jadwal by ID: " + e.getMessage());
        }
        return null;
    }
    

    public String getHariTanggalJadwal(String idJadwal) {
        Jadwal jadwal = getJadwalById(idJadwal);

        if (jadwal != null) {
            try {
                // Parse tanggal dari format YYYY-MM-DD
                LocalDate date = LocalDate.parse(jadwal.getTanggal());

                // Dapatkan nama hari dan bulan dalam Bahasa Indonesia
                String namaHari = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
                String namaBulan = date.getMonth().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));

                String tanggalFormatted = String.format("%02d", date.getDayOfMonth());

                return String.format("%s, %s %s %d", namaHari, tanggalFormatted, namaBulan, date.getYear());
            } catch (Exception e) {
                System.err.println("Gagal memformat tanggal untuk jadwal ID " + idJadwal + ": " + e.getMessage());
                return "Tanggal tidak valid";
            }
        }
        return "Jadwal tidak ditemukan";
    }

    public String getJamJadwal(String idJadwal) {
        Jadwal jadwal = getJadwalById(idJadwal);

        if (jadwal != null) {
            String jamMulai = jadwal.getJamMulai().replace(":", ".");
            String jamSelesai = jadwal.getJamSelesai().replace(":", ".");
            
            return String.format("%s - %s WIB", jamMulai, jamSelesai);
        }
        return "Jam tidak ditemukan";
    }

    public List<Jadwal> getJadwalByTutorId(String idTutor) {
        List<Jadwal> daftarJadwal = new ArrayList<>();
        String sql = "SELECT id_jadwal, id_tutor, mata_pelajaran, hari, tanggal, jam_mulai, jam_selesai, status_jadwal FROM Jadwal WHERE id_tutor = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, idTutor);
            
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Jadwal jadwal = new Jadwal(
                    rs.getString("id_jadwal"),
                    rs.getString("id_tutor"),
                    rs.getString("mata_pelajaran"),
                    rs.getString("hari"),
                    rs.getString("tanggal"),
                    rs.getString("jam_mulai"),
                    rs.getString("jam_selesai"),
                    rs.getString("status_jadwal")
                );
                daftarJadwal.add(jadwal);
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data jadwal berdasarkan ID Tutor: " + e.getMessage());
        }
        
        return daftarJadwal;
    }

    // Metode untuk menampilkan data
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
    // public static void main(String[] args) {
    //     System.setProperty("app.env", "development");
    //     DatabaseInitializer.initialize();

    //     SiswaController controller = new SiswaController();
    //     if (controller.conn == null) {
    //         System.out.println("Koneksi database gagal, tidak dapat melanjutkan tes.");
    //         return;
    //     }

    //     System.out.println("\n=======================================================");
    //     System.out.println("--- 1. KONDISI AWAL DATABASE ---");
    //     System.out.println("=======================================================");
    //     System.out.println("\n[A] Daftar Tutor Awal:");
    //     controller.tampilkanSemuaTutor();
    //     System.out.println("\n[B] Daftar Sesi Awal:");
    //     controller.tampilkanSemuaSesi();
    //     System.out.println("\n[C] Daftar Pembayaran Awal:");
    //     controller.tampilkanSemuaPembayaran();

    //     System.out.println("\n\n=======================================================");
    //     System.out.println("--- 2. MENJALANKAN SEMUA SKENARIO ---");
    //     System.out.println("=======================================================");

    //     System.out.println("\n--- SKENARIO UTAMA: PEMESANAN SESI ---");
    //     System.out.println("[+] Siswa mencari tutor 'Bahasa Inggris'...");
    //     List<Tutor> hasilCari = controller.cariTutor("Bahasa Inggris", null, null, null, null);

    //     if (hasilCari.isEmpty()) {
    //         System.out.println("Hasil: Tidak ada tutor yang ditemukan. Skenario berhenti.");
    //     } else {
    //         Tutor tutorPilihan = hasilCari.get(0);
    //         Jadwal jadwalPilihan = tutorPilihan.getJadwal().get(0);
    //         System.out.println("  -> Tutor ditemukan: " + tutorPilihan.getNama() + " dengan jadwal ID: " + jadwalPilihan.getIdJadwal());

    //         System.out.println("[+] Siswa 'P1' (Budi Santoso) memesan sesi dan langsung membayar...");
    //         boolean pemesananBerhasil = controller.pesanSesi("P1", tutorPilihan.getIdPengguna(), jadwalPilihan.getIdJadwal(), "Gopay", 175000);

    //         if (!pemesananBerhasil) {
    //             System.out.println("  -> Gagal memesan dan membayar sesi.");
    //         }
    //     }
        
    //     System.out.println("\n\n--- SKENARIO ALTERNATIF 1: MENJADWAL ULANG SESI ---");
    //     System.out.println("[+] Siswa mencoba menjadwal ulang Sesi 'S1' (milik Tutor P3) ke Jadwal 'J3' (milik Tutor P4). Harusnya GAGAL.");
    //     controller.jadwalUlangSesi("S1", "J3");

    //     System.out.println("\n[+] Siswa mencoba menjadwal ulang Sesi 'S1' (milik Tutor P3) ke Jadwal 'J2' (milik Tutor P3). Harusnya BERHASIL.");
    //     controller.jadwalUlangSesi("S1", "J2");

    //     System.out.println("\n\n--- SKENARIO ALTERNATIF 2: MEMBATALKAN PEMESANAN ---");
    //     System.out.println("[+] Siswa mencoba membatalkan Sesi 'S1' yang aktif (jadwal di masa depan). Harusnya BERHASIL.");
    //     controller.batalkanSesi("S1");
        
    //     System.out.println("\n[+] Siswa mencoba membatalkan Sesi 'S1' lagi setelah dibatalkan. Harusnya GAGAL.");
    //     controller.batalkanSesi("S1");
        
    //     System.out.println("\n\n=======================================================");
    //     System.out.println("--- 3. KONDISI AKHIR DATABASE ---");
    //     System.out.println("=======================================================");
    //     System.out.println("\n[A] Daftar Tutor Akhir:");
    //     controller.tampilkanSemuaTutor();
    //     System.out.println("\n[B] Daftar Sesi Akhir:");
    //     controller.tampilkanSemuaSesi();
    //     System.out.println("\n[C] Daftar Pembayaran Akhir:");
    //     controller.tampilkanSemuaPembayaran();

    //     System.out.println("\n\n--- SEMUA SKENARIO SELESAI ---");

    //     try {
    //         if (controller.conn != null && !controller.conn.isClosed()) {
    //             controller.conn.close();
    //             System.out.println("\nKoneksi ke database ditutup.");
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

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