package com.ruang_pandai.controller;

import com.ruang_pandai.database.DatabaseInitializer;
import com.ruang_pandai.entity.Jadwal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TutorController {

    private static final String DB_URL = "jdbc:sqlite:src/main/resources/com/ruang_pandai/database/ruangpandai.db";

    // Method buatJadwal untuk menambahkan jadwal baru ke database
    // Operasi ini akan memasukkan data jadwal ke dalam tabel Jadwal.
    public void buatJadwal(Jadwal jadwal) {
        String sql = "INSERT INTO Jadwal(id_jadwal, id_tutor, mata_pelajaran, hari, tanggal, jam_mulai, jam_selesai, status_jadwal) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, jadwal.getIdJadwal());
            pstmt.setString(2, jadwal.getIdTutor());
            pstmt.setString(3, jadwal.getMataPelajaran());
            pstmt.setString(4, jadwal.getHari());
            pstmt.setString(5, jadwal.getTanggal());
            pstmt.setString(6, jadwal.getJamMulai());
            pstmt.setString(7, jadwal.getJamSelesai());
            pstmt.setString(8, jadwal.getStatusJadwal());
            pstmt.executeUpdate();
            
            System.out.println("SUCCESS: Jadwal ketersediaan berhasil ditambahkan.");
        } catch (SQLException e) {
            System.err.println("ERROR: Gagal menambahkan jadwal: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method lihatJadwal untuk mengambil daftar jadwal berdasarkan ID tutor
    // Operasi ini akan mengambil semua jadwal yang terkait dengan tutor tertentu dari tabel Jadwal.
    public List<Jadwal> lihatJadwal(String idTutor) {
        List<Jadwal> daftarJadwal = new ArrayList<>();
        String sql = "SELECT * FROM Jadwal WHERE id_tutor = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idTutor);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                daftarJadwal.add(new Jadwal(
                        rs.getString("id_jadwal"),
                        rs.getString("id_tutor"),
                        rs.getString("mata_pelajaran"),
                        rs.getString("hari"),
                        rs.getString("tanggal"),
                        rs.getString("jam_mulai"),
                        rs.getString("jam_selesai"),
                        rs.getString("status_jadwal")
                ));
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Gagal mengambil data jadwal: " + e.getMessage());
            e.printStackTrace();
        }
        return daftarJadwal;
    }

    // Method lihatSemuaJadwal untuk mengambil semua jadwal dari database
    // Operasi ini akan mengambil semua jadwal yang ada di tabel Jadwal tanpa filter.
    public List<Jadwal> lihatSemuaJadwal() {
        List<Jadwal> daftarJadwal = new ArrayList<>();
        String sql = "SELECT * FROM Jadwal"; 

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                daftarJadwal.add(new Jadwal(
                        rs.getString("id_jadwal"),
                        rs.getString("id_tutor"),
                        rs.getString("mata_pelajaran"),
                        rs.getString("hari"),
                        rs.getString("tanggal"),
                        rs.getString("jam_mulai"),
                        rs.getString("jam_selesai"),
                        rs.getString("status_jadwal")
                ));
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Gagal mengambil semua data jadwal: " + e.getMessage());
            e.printStackTrace();
        }
        return daftarJadwal;
    }
    
    // Method ubahJadwal untuk memperbarui jadwal yang sudah ada
    // Operasi ini akan memperbarui data jadwal berdasarkan ID jadwal yang diberikan.
    public void ubahJadwal(Jadwal jadwal) {
        String checkSql = "SELECT status_jadwal FROM Jadwal WHERE id_jadwal = ?";
        String updateSql = "UPDATE Jadwal SET mata_pelajaran = ?, hari = ?, tanggal = ?, jam_mulai = ?, jam_selesai = ? WHERE id_jadwal = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, jadwal.getIdJadwal());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status_jadwal");
                if ("DIPESAN".equals(status)) {
                    System.err.println("ERROR: Tidak dapat mengubah jadwal yang sudah dipesan.");
                    return;
                }
            } else {
                System.err.println("ERROR: Jadwal tidak ditemukan.");
                return;
            }
            
            // Jika jadwal 'TERSEDIA'
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, jadwal.getMataPelajaran());
                updateStmt.setString(2, jadwal.getHari());
                updateStmt.setString(3, jadwal.getTanggal());
                updateStmt.setString(4, jadwal.getJamMulai());
                updateStmt.setString(5, jadwal.getJamSelesai());
                updateStmt.setString(6, jadwal.getIdJadwal());

                int affectedRows = updateStmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("SUCCESS: Jadwal berhasil diperbarui.");
                } else {
                    System.err.println("ERROR: Gagal memperbarui jadwal, data tidak ditemukan.");
                }
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Database error saat mengubah jadwal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method hapusJadwal untuk menghapus jadwal berdasarkan ID jadwal
    // Operasi ini akan menghapus data jadwal dari tabel Jadwal berdasarkan ID jadwal yang diberikan.
    public void hapusJadwal(String idJadwal) {
        String checkSql = "SELECT status_jadwal FROM Jadwal WHERE id_jadwal = ?";
        String deleteSql = "DELETE FROM Jadwal WHERE id_jadwal = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, idJadwal);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status_jadwal");
                if ("DIPESAN".equals(status)) {
                    System.err.println("ERROR: Tidak dapat menghapus jadwal yang sudah dipesan.");
                    return;
                }
            } else {
                 System.err.println("ERROR: Jadwal yang akan dihapus tidak ditemukan.");
                 return;
            }
            
            // Jika jadwal 'TERSEDIA'
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, idJadwal);
                int affectedRows = deleteStmt.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("SUCCESS: Jadwal berhasil dihapus.");
                } else {
                    System.err.println("ERROR: Gagal menghapus jadwal.");
                }
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Database error saat menghapus jadwal: " + e.getMessage());
            e.printStackTrace();
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