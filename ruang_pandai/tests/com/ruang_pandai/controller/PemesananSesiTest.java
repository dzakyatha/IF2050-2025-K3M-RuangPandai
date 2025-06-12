package com.ruang_pandai.controller;

import com.ruang_pandai.controller.SiswaController;
import com.ruang_pandai.database.DatabaseInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Cara menjalankan:
 * 1. Pastikan sudah berada di direktori proyek
 *    cd ruang_pandai
 * 2. Jalankan perintah:
 *    mvn test
 */
class PemesananSesiTest {

    private SiswaController siswaController;
    private Connection conn;
    private final String DB_URL = "jdbc:sqlite:src/main/resources/com/ruang_pandai/database/ruangpandai.db";


    @BeforeEach
    void setUp() throws SQLException {
        System.setProperty("app.env", "development");
        DatabaseInitializer.initialize();
        siswaController = new SiswaController();
        conn = DriverManager.getConnection(DB_URL);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }


    @Test
    @DisplayName("Pesan Sesi Sukses: Jadwal tersedia dan data valid")
    void testPesanSesi_Success() throws SQLException {
        String idSiswa = "P1";
        String idTutor = "P3";
        String idJadwal = "J2";

        boolean result = siswaController.pesanSesi(idSiswa, idTutor, idJadwal, "Transfer Bank", 150000);

        assertTrue(result, "Pemesanan sesi seharusnya berhasil.");
        assertEquals("DIPESAN", getJadwalStatus(idJadwal), "Status jadwal seharusnya berubah menjadi 'DIPESAN'.");
        String idSesiBaru = getSesiIdByJadwalId(idJadwal);
        assertNotNull(idSesiBaru, "Sesi baru seharusnya dibuat di database.");
        assertTrue(isPembayaranExist(idSesiBaru), "Data pembayaran untuk sesi baru seharusnya ada.");
    }

    @Test
    @DisplayName("Pesan Sesi Gagal: Jadwal sudah dipesan")
    void testPesanSesi_Failure_JadwalAlreadyBooked() throws SQLException {
        String idSiswa = "P2";
        String idTutor = "P3";
        String idJadwal = "J1";

        boolean result = siswaController.pesanSesi(idSiswa, idTutor, idJadwal, "OVO", 150000);

        assertFalse(result, "Pemesanan sesi seharusnya gagal karena jadwal sudah dipesan.");
        assertEquals("DIPESAN", getJadwalStatus(idJadwal), "Status jadwal seharusnya tidak berubah.");
    }

    @Test
    @DisplayName("Batalkan Sesi Sukses: Sesi 'AKAN DATANG' dan waktu mencukupi")
    void testBatalkanSesi_Success() throws SQLException {
        String idSesi = "S4";
        String idJadwalTerkait = "J4";

        boolean result = siswaController.batalkanSesi(idSesi);

        assertTrue(result, "Pembatalan sesi seharusnya berhasil.");

        assertEquals("DIBATALKAN", getSesiStatus(idSesi), "Status sesi seharusnya berubah menjadi 'DIBATALKAN'.");
        assertEquals("TERSEDIA", getJadwalStatus(idJadwalTerkait), "Status jadwal terkait seharusnya kembali menjadi 'TERSEDIA'.");
    }

    @Test
    @DisplayName("Batalkan Sesi Gagal: Sesi sudah dibatalkan sebelumnya")
    void testBatalkanSesi_Failure_AlreadyCancelled() {
        String idSesi = "S2";

        boolean result = siswaController.batalkanSesi(idSesi);

        assertFalse(result, "Seharusnya tidak bisa membatalkan sesi yang sudah berstatus 'DIBATALKAN'.");
    }

    @Test
    @DisplayName("Jadwal Ulang Sesi Sukses: Jadwal baru tersedia")
    void testJadwalUlangSesi_Success() throws SQLException {
        String idSesi = "S5";
        String idJadwalLama = "J5";
        String idJadwalBaru = "J6";

        boolean result = siswaController.jadwalUlangSesi(idSesi, idJadwalBaru);

        assertTrue(result, "Penjadwalan ulang sesi seharusnya berhasil.");

        assertEquals("TERSEDIA", getJadwalStatus(idJadwalLama), "Status jadwal lama seharusnya kembali menjadi 'TERSEDIA'.");
        assertEquals("DIPESAN", getJadwalStatus(idJadwalBaru), "Status jadwal baru seharusnya menjadi 'DIPESAN'.");
        assertEquals(idJadwalBaru, getJadwalIdBySesiId(idSesi), "Sesi seharusnya sekarang terikat pada jadwal baru.");
    }

    @Test
    @DisplayName("Jadwal Ulang Sesi Gagal: Jadwal baru tidak tersedia")
    void testJadwalUlangSesi_Failure_NewJadwalNotAvailable() throws SQLException {
        String idSesi = "S1";
        String idJadwalLama = "J1";
        String idJadwalBaru = "J3";

        boolean result = siswaController.jadwalUlangSesi(idSesi, idJadwalBaru);

        assertFalse(result, "Penjadwalan ulang seharusnya gagal karena jadwal baru sudah dipesan.");

        assertEquals("DIPESAN", getJadwalStatus(idJadwalLama), "Status jadwal lama seharusnya tidak berubah.");
        assertEquals("DIPESAN", getJadwalStatus(idJadwalBaru), "Status jadwal baru seharusnya tidak berubah.");
    }

    // Metode helper
    private String getJadwalStatus(String idJadwal) throws SQLException {
        String sql = "SELECT status_jadwal FROM Jadwal WHERE id_jadwal = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idJadwal);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("status_jadwal");
            }
        }
        return null;
    }
    
    private String getSesiIdByJadwalId(String idJadwal) throws SQLException {
        String sql = "SELECT id_sesi FROM Sesi WHERE id_jadwal = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idJadwal);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id_sesi");
            }
        }
        return null;
    }

    private String getSesiStatus(String idSesi) throws SQLException {
        String sql = "SELECT status_sesi FROM Sesi WHERE id_sesi = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idSesi);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("status_sesi");
            }
        }
        return null;
    }

    private String getJadwalIdBySesiId(String idSesi) throws SQLException {
        String sql = "SELECT id_jadwal FROM Sesi WHERE id_sesi = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idSesi);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id_jadwal");
            }
        }
        return null;
    }

    private boolean isPembayaranExist(String idSesi) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Pembayaran WHERE id_sesi = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idSesi);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}