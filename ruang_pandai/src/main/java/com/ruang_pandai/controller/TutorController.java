package com.ruang_pandai.controller;

import com.ruang_pandai.entity.Jadwal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TutorController {

    private static final String DB_URL = "jdbc:sqlite:src/main/resources/com/ruang_pandai/database/ruangpandai.db";
    private Connection conn;

    public TutorController() {
        try {
            this.conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Koneksi ke database GAGAL di TutorController: " + e.getMessage());
            throw new RuntimeException("Tidak dapat terhubung ke database.", e);
        }
    }
    
    private String generateNextId() {
        int nextNumber = 1;
        String query = "SELECT id_jadwal FROM Jadwal ORDER BY LENGTH(id_jadwal) DESC, id_jadwal DESC LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String lastId = rs.getString("id_jadwal");
                nextNumber = Integer.parseInt(lastId.replace("J", "")) + 1;
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Gagal membuat ID Jadwal baru, akan memulai dari 1. Error: " + e.getMessage());
        }
        return "J" + nextNumber;
    }

    /**
     * [BARU] Method internal untuk memeriksa jadwal yang tumpang tindih.
     */
    private boolean isJadwalOverlap(Jadwal jadwal, String idToExclude) {
        String sql = "SELECT COUNT(*) FROM Jadwal WHERE id_tutor = ? AND tanggal = ? AND " +
                     "jam_mulai < ? AND jam_selesai > ?";
        
        if (idToExclude != null) {
            sql += " AND id_jadwal != ?";
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            pstmt.setString(paramIndex++, jadwal.getIdTutor());
            pstmt.setString(paramIndex++, jadwal.getTanggal());
            pstmt.setString(paramIndex++, jadwal.getJamSelesai());
            pstmt.setString(paramIndex++, jadwal.getJamMulai());
            if (idToExclude != null) {
                pstmt.setString(paramIndex, idToExclude);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // Ditemukan jadwal yang tumpang tindih
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Gagal memeriksa jadwal tumpang tindih: " + e.getMessage());
        }
        return false;
    }

    /**
     * DIPERBARUI: Menambah validasi sebelum menyimpan.
     */
    public boolean buatJadwal(Jadwal jadwal) {
        // // Validasi backend
        if (!isJadwalValid(jadwal, null)) return false;

        String newId = generateNextId();
        String sql = "INSERT INTO Jadwal(id_jadwal, id_tutor, mata_pelajaran, hari, tanggal, jam_mulai, jam_selesai, status_jadwal) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newId);
            pstmt.setString(2, jadwal.getIdTutor());
            pstmt.setString(3, jadwal.getMataPelajaran());
            pstmt.setString(4, jadwal.getHari());
            pstmt.setString(5, jadwal.getTanggal());
            pstmt.setString(6, jadwal.getJamMulai());
            pstmt.setString(7, jadwal.getJamSelesai());
            pstmt.setString(8, "TERSEDIA");
            pstmt.executeUpdate();
            System.out.println("buat jadwal berhasil");
            return true;
        } catch (SQLException e) {
            System.err.println("ERROR: Gagal menambahkan jadwal: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * DIPERBARUI: Menambah validasi sebelum mengubah.
     */
    public boolean ubahJadwal(Jadwal jadwal) {
        // // Validasi backend
        if (!isJadwalValid(jadwal, jadwal.getIdJadwal())) return false;

        String sql = "UPDATE Jadwal SET mata_pelajaran = ?, hari = ?, tanggal = ?, jam_mulai = ?, jam_selesai = ? WHERE id_jadwal = ? AND status_jadwal = 'TERSEDIA'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, jadwal.getMataPelajaran());
            pstmt.setString(2, jadwal.getHari());
            pstmt.setString(3, jadwal.getTanggal());
            pstmt.setString(4, jadwal.getJamMulai());
            pstmt.setString(5, jadwal.getJamSelesai());
            pstmt.setString(6, jadwal.getIdJadwal());
            int affectedRows = pstmt.executeUpdate();
            System.out.println("edit berhasil");
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("ERROR: Database error saat mengubah jadwal: " + e.getMessage());
            return false;
        }
    }
    
    private boolean isJadwalValid(Jadwal jadwal, String idToExclude) {
        if (isJadwalOverlap(jadwal, idToExclude)) {
            System.err.println("VALIDATION FAILED: Jadwal bertabrakan dengan jadwal yang sudah ada.");
            return false;
        }
        return true;
    }

    public List<Jadwal> lihatJadwal(String idTutor) {
        List<Jadwal> daftarJadwal = new ArrayList<>();
        String sql = "SELECT * FROM Jadwal WHERE id_tutor = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idTutor);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                daftarJadwal.add(new Jadwal(
                    rs.getString("id_jadwal"), rs.getString("id_tutor"),
                    rs.getString("mata_pelajaran"), rs.getString("hari"),
                    rs.getString("tanggal"), rs.getString("jam_mulai"),
                    rs.getString("jam_selesai"), rs.getString("status_jadwal")
                ));
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Gagal mengambil data jadwal: " + e.getMessage());
        }
        return daftarJadwal;
    }

    public boolean hapusJadwal(String idJadwal) {
        String sql = "DELETE FROM Jadwal WHERE id_jadwal = ? AND status_jadwal = 'TERSEDIA'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idJadwal);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("hapus berhasil");
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("ERROR: Database error saat menghapus jadwal: " + e.getMessage());
            return false;
        }
    }

    /* * Cara menjalankan static void main() di TutorController:
     * 1. Copy ini (sebelum baris </plugins>) ke dalam pom.xml:
     <plugin>
     <groupId>org.codehaus.mojo</groupId>
     <artifactId>exec-maven-plugin</artifactId>
     <version>3.2.0</version>
     <configuration>
     <mainClass>com.ruang_pandai.controller.TutorController</mainClass>
     </configuration>
     </plugin>
     * 2. Buka terminal di direktori proyek:
     * cd ruang_pandai
     * kemudian:
     * mvn compile exec:java
     */
    // public static void main(String[] args) {
    //     // Inisialisasi database dan dummy data
    //     DatabaseInitializer.initialize();

    //     TutorController tutorController = new TutorController();
    //     String idTutorP3 = "P3"; 

    //     System.out.println("--- DEMO PENGELOLAAN JADWAL TUTOR ---");

    //     // 1. LIHAT JADWAL (AWAL)
    //     System.out.println("\n[1] Menampilkan jadwal awal untuk Tutor " + idTutorP3 + ":");
    //     List<Jadwal> jadwalAwal = tutorController.lihatJadwal(idTutorP3);
    //     if (jadwalAwal.isEmpty()) {
    //         System.out.println("Tidak ada jadwal ditemukan.");
    //     } else {
    //         jadwalAwal.forEach(j -> 
    //             System.out.println("  - ID: " + j.getIdJadwal() + ", Mapel: " + j.getMataPelajaran() + ", Status: " + j.getStatusJadwal())
    //         );
    //     }

    //     // 2. BUAT JADWAL (SKENARIO NORMAL)
    //     System.out.println("\n[2] Menambahkan jadwal baru ");
    //     Jadwal jadwalBaru = new Jadwal("J4", idTutorP3, "Fisika", "JUMAT", "2025-06-13", "10:00", "12:00", "TERSEDIA");
    //     tutorController.buatJadwal(jadwalBaru);
        
    //     System.out.println("\n   Menampilkan jadwal setelah penambahan:");
    //     tutorController.lihatJadwal(idTutorP3).forEach(j -> 
    //         System.out.println("  - ID: " + j.getIdJadwal() + ", Mapel: " + j.getMataPelajaran() + ", Status: " + j.getStatusJadwal())
    //     );

    //     // 3. UBAH JADWAL (SKENARIO ALTERNATIF 1)
    //     System.out.println("\n[3] Mengubah jadwal J2 (TERSEDIA) menjadi Kimia");
    //     Jadwal jadwalUpdate = new Jadwal("J2", idTutorP3, "Kimia", "KAMIS", "2025-06-12", "14:00", "16:00", "TERSEDIA");
    //     tutorController.ubahJadwal(jadwalUpdate);

    //     System.out.println("\n   Mencoba mengubah jadwal J1 (DIPESAN)");
    //     Jadwal jadwalUpdateGagal = new Jadwal("J1", idTutorP3, "Biologi", "SELASA", "2025-06-08", "08:00", "10:00", "DIPESAN");
    //     tutorController.ubahJadwal(jadwalUpdateGagal); // Ini akan menampilkan pesan error

    //     System.out.println("\n   Menampilkan jadwal setelah percobaan perubahan:");
    //     tutorController.lihatJadwal(idTutorP3).forEach(j -> 
    //         System.out.println("  - ID: " + j.getIdJadwal() + ", Mapel: " + j.getMataPelajaran() + ", Status: " + j.getStatusJadwal())
    //     );

    //     // 4. HAPUS JADWAL (SKENARIO ALTERNATIF 2)
    //     System.out.println("\n[4] Menghapus jadwal J4 (TERSEDIA)");
    //     tutorController.hapusJadwal("J4");

    //     System.out.println("\n   Mencoba menghapus jadwal J1 (DIPESAN)");
    //     tutorController.hapusJadwal("J1"); // Ini akan menampilkan pesan error
        
    //     System.out.println("\n   Menampilkan jadwal akhir untuk Tutor " + idTutorP3 + ":");
    //     tutorController.lihatJadwal(idTutorP3).forEach(j -> 
    //         System.out.println("  - ID: " + j.getIdJadwal() + ", Mapel: " + j.getMataPelajaran() + ", Status: " + j.getStatusJadwal())
    //     );
        
    //     // 5. LIHAT SEMUA JADWAL DARI SEMUA TUTOR
    //     System.out.println("\n[5] Menampilkan SEMUA jadwal dari semua tutor yang ada di database:");
    //     List<Jadwal> semuaJadwal = tutorController.lihatSemuaJadwal();
    //     if (semuaJadwal.isEmpty()) {
    //         System.out.println("Tidak ada jadwal sama sekali di database.");
    //     } else {
    //         semuaJadwal.forEach(j -> 
    //             System.out.println("  - ID: " + j.getIdJadwal() + ", Tutor: " + j.getIdTutor() + ", Mapel: " + j.getMataPelajaran() + ", Status: " + j.getStatusJadwal())
    //         );
    //     }
        
    //     System.out.println("\n--- DEMO SELESAI ---");
    // }
}
