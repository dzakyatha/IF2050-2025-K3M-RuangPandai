package com.ruang_pandai.controller;

import com.ruang_pandai.database.DatabaseInitializer;
import com.ruang_pandai.entity.Jadwal;
import com.ruang_pandai.entity.Tutor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SiswaController {

    private final String DB_URL = "jdbc:sqlite:src/main/resources/database/ruangpandai.db";
    private Connection conn;

    public SiswaController() {
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Gagal koneksi ke database: " + e.getMessage());
        }
    }

    // Method untuk mencari tutor berdasarkan filter
    public List<Tutor> cariTutor(String mataPelajaran, String pendidikan, Integer rating, String hari) {
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
        if (pendidikan != null && !pendidikan.isEmpty()) {
            sql.append(" AND t.pendidikan LIKE ?");
            params.add("%" + pendidikan + "%");
        }
        if (rating != null && rating > 0) {
            sql.append(" AND t.rating >= ?");
            params.add(rating);
        }

        // Menambahkan subquery untuk filter jadwal berdasarkan hari
        if (hari != null && !hari.isEmpty()) {
            sql.append(" AND EXISTS (SELECT 1 FROM Jadwal j WHERE j.id_tutor = t.id_pengguna AND j.hari = ?)");
            params.add(hari.toUpperCase());
        }

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
            tutor.setJadwal(jadwalMap.get(tutor.getIdPengguna()));
        }

        return hasilPencarian;
    }

    // Demo penggunaan controller
    /*  
     * Cara menjalankan main ini:
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
    public static void main(String[] args) {
        System.setProperty("app.env", "development");
        DatabaseInitializer.initialize();

        SiswaController controller = new SiswaController();
        if (controller.conn == null) {
            System.out.println("Koneksi database gagal.");
            return;
        }
        
        System.out.println("\n--- Skenario 1: Mencari tutor tanpa filter ---");
        // Mengambil semua tutor tanpa filter
        List<Tutor> semuaTutor = controller.cariTutor(null, null, null, null);
        if (semuaTutor.isEmpty()) {
            System.out.println("Tidak ada tutor yang ditemukan.");
        } else {
            semuaTutor.forEach(t -> {
                System.out.println("Ditemukan: " + t.getNama() + " | Mata Pelajaran: " + t.getMataPelajaran());
                // Menampilkan jadwal yang berhasil diambil
                if (t.getJadwal() != null && !t.getJadwal().isEmpty()) {
                    System.out.println("  Jadwal yang tersedia:");
                    t.getJadwal().forEach(j -> {
                        System.out.println("   -> " + j.getHari() + ", " + j.getTanggal() + " (" + j.getJamMulai() + " - " + j.getJamSelesai() + ")");
                    });
                } else {
                    System.out.println("  -> Tidak ada detail jadwal yang ditemukan untuk tutor ini.");
                }
            });
        }

        System.out.println("\n--- Skenario 2: Mencari tutor yang tersedia hari RABU ---");
        // Data dummy memiliki jadwal Matematika pada hari RABU
        List<Tutor> tutorsRabu = controller.cariTutor(null, null, null, "RABU");
        if (tutorsRabu.isEmpty()) {
            System.out.println("Tidak ada tutor yang sesuai dengan kriteria pencarian.");
        } else {
            tutorsRabu.forEach(t -> {
                System.out.println("Ditemukan: " + t.getNama() + " (" + t.getMataPelajaran() + ")");
                // Menampilkan jadwal yang berhasil diambil
                 if (t.getJadwal() != null && !t.getJadwal().isEmpty()) {
                    System.out.println("  Jadwal hari rabu: ");
                    t.getJadwal().stream()
                        .filter(j -> "RABU".equalsIgnoreCase(j.getHari()))
                        .forEach(j -> {
                            System.out.println("   -> " + j.getHari() + ", " + j.getTanggal() + " (" + j.getJamMulai() + " - " + j.getJamSelesai() + ")");
                        });
                } else {
                    System.out.println("  -> Tidak ada detail jadwal yang ditemukan untuk tutor ini.");
                }
            });
        }

        System.out.println("\n--- Skenario 3: Mencari tutor Bahasa Inggris dengan rating 4 ---");
        // Data dummy memiliki tutor Bahasa Inggris dengan rating 4
        List<Tutor> tutorsInggris = controller.cariTutor("Bahasa Inggris", null, 4, null);
        if (tutorsInggris.isEmpty()) {
            System.out.println("Tidak ada tutor yang sesuai dengan kriteria pencarian.");
        } else {
            tutorsInggris.forEach(t -> {
                System.out.println("Ditemukan: " + t.getNama() + " | Rating: " + t.getRating() + " | Pendidikan: " + t.getPendidikan());
                // Menampilkan jadwal yang berhasil diambil
                if (t.getJadwal() != null && !t.getJadwal().isEmpty()) {
                    System.out.println("  Jadwal yang tersedia:");
                    t.getJadwal().forEach(j -> {
                        System.out.println("   -> " + j.getHari() + ", " + j.getTanggal() + " (" + j.getJamMulai() + " - " + j.getJamSelesai() + ")");
                    });
                } else {
                    System.out.println("  -> Tidak ada detail jadwal yang ditemukan untuk tutor ini.");
                }
            });
        }


        System.out.println("\n--- Skenario 4: Mencari tutor dengan filter yang tidak ada ---");
        // Mencoba mencari tutor dengan filter yang tidak ada
        List<Tutor> tutorTidakAda = controller.cariTutor("Kimia", "S3", 5, "SABTU");
        if (tutorTidakAda.isEmpty()) {
            System.out.println("Tidak ada tutor yang sesuai dengan kriteria pencarian.");
        } else {
            tutorTidakAda.forEach(t -> {
                System.out.println("Ditemukan: " + t.getNama() + " | Mata Pelajaran: " + t.getMataPelajaran());
                // Menampilkan jadwal yang berhasil diambil
                if (t.getJadwal() != null && !t.getJadwal().isEmpty()) {
                    System.out.println("  Jadwal yang tersedia:");
                    t.getJadwal().forEach(j -> {
                        System.out.println("   -> " + j.getHari() + ", " + j.getTanggal() + " (" + j.getJamMulai() + " - " + j.getJamSelesai() + ")");
                    });
                } else {
                    System.out.println("  -> Tidak ada detail jadwal yang ditemukan untuk tutor ini.");
                }
            });
        }
    }
}