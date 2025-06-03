package com.ruang_pandai.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final String DB_FILE = "src/main/resources/database/ruangpandai.db";
    private static final String SCHEMA_FILE = "/com/ruang_pandai/database/schema.sql";
    
    public static void initialize() {
        createDatabaseIfNotExists();
        executeSchemaScript();
        insertDummyDataIfNeeded(); 
    }
    
    private static void createDatabaseIfNotExists() {
        File dbFile = new File(DB_FILE);
        if (!dbFile.exists()) {
            try {
                dbFile.getParentFile().mkdirs();
                dbFile.createNewFile();
                System.out.println("Database file created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void executeSchemaScript() {
        // Path koneksi tetap sama
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
             InputStream input = DatabaseInitializer.class.getResourceAsStream(SCHEMA_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             Statement stmt = conn.createStatement()) {
            
            if (input == null) {
                System.err.println("Schema file not found! Make sure the path is correct: " + SCHEMA_FILE);
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            
            // Eksekusi skema SQL
            String[] individualStatements = sb.toString().split(";");
            for (String statement : individualStatements) {
                if (!statement.trim().isEmpty()) {
                    stmt.executeUpdate(statement);
                }
            }
            System.out.println("Database schema initialized");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void insertDummyDataIfNeeded() {
        if (isDevelopmentMode()) {
            // Path koneksi tetap sama
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
                 Statement stmt = conn.createStatement()) {
                
                // Cek apakah data sudah ada untuk menghindari duplikasi error
                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM Pengguna");
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Dummy data already exists.");
                    return;
                }
                
                // Data Pengguna
                stmt.executeUpdate("INSERT INTO Pengguna (id_pengguna, nama, role, email, no_telp, alamat) VALUES " +
                    "('P1', 'Budi Santoso', 'SISWA', 'budi@mail.com', '08123456789', 'Jl. Sudirman No.1, Jakarta'), " +
                    "('P2', 'Ani Wijaya', 'SISWA', 'ani@mail.com', '08234567890', 'Jl. Thamrin No.2, Jakarta'), " +
                    "('P3', 'Citra Dewi', 'TUTOR', 'citra@mail.com', '08345678901', 'Jl. Gatot Subroto No.3, Jakarta'), " +
                    "('P4', 'Dedi Kurniawan', 'TUTOR', 'dedi@mail.com', '08456789012', 'Jl. MH Thamrin No.4, Jakarta')");
                
                // Data Siswa
                stmt.executeUpdate("INSERT INTO Siswa (id_pengguna) VALUES " +
                    "('P1'), ('P2')");
                
                // Data Tutor
                stmt.executeUpdate("INSERT INTO Tutor (id_pengguna, mata_pelajaran, pendidikan, pengalaman, ulasan, rating) VALUES " +
                    "('P3', 'Matematika', 'S1 Pendidikan Matematika', '5 tahun mengajar', 'Sangat membantu', 5), " +
                    "('P4', 'Bahasa Inggris', 'S2 Sastra Inggris', '3 tahun mengajar', 'Penjelasan jelas', 4)");
                
                // Data Jadwal
                stmt.executeUpdate("INSERT INTO Jadwal (id_jadwal, id_tutor, mata_pelajaran, hari, tanggal, jam_mulai, jam_selesai) VALUES " +
                    "('J1', 'P3', 'Matematika', 'SELASA', '2025-06-10', '09:00', '11:00'), " +
                    "('J2', 'P3', 'Matematika', 'KAMIS', '2025-06-12', '13:00', '15:00'), " +
                    "('J3', 'P4', 'Bahasa Inggris', 'RABU', '2025-06-11', '10:00', '12:00')");
                
                // Data Sesi
                stmt.executeUpdate("INSERT INTO Sesi (id_sesi, id_siswa, id_tutor, id_jadwal, tanggal_pesan, status_pembayaran, status_kehadiran, status_sesi) VALUES " +
                    "('S1', 'P1', 'P3', 'J1', '2025-06-09', 'SUDAH BAYAR', 'HADIR', 'SELESAI'), " +
                    "('S2', 'P2', 'P4', 'J3', '2025-06-09', 'MENUNGGU PEMBAYARAN', 'TIDAK HADIR', 'AKAN DATANG')");
                
                // Data Pembayaran
                stmt.executeUpdate("INSERT INTO Pembayaran (id_pembayaran, id_sesi, jumlah, metode_pembayaran, bukti_pembayaran, waktu_pembayaran, status_pembayaran) VALUES " +
                    "('PB1', 'S1', 150000, 'Transfer Bank', 'dummy_bukti.jpg', '2025-06-09 14:30:00', 'BERHASIL')");
                
                System.out.println("Dummy data inserted");
            } catch (SQLException e) {
                if (!e.getMessage().contains("UNIQUE constraint failed")) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static boolean isDevelopmentMode() {
        return System.getProperty("app.env", "development").equals("development");
    }

    public static void main(String[] args) {
        System.out.println(isDevelopmentMode());
    }
}