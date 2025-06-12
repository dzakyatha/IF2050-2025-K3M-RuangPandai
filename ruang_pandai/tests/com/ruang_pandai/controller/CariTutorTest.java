package com.ruang_pandai.controller;

import com.ruang_pandai.controller.SiswaController;
import com.ruang_pandai.database.DatabaseInitializer;
import com.ruang_pandai.entity.Tutor;
import com.ruang_pandai.entity.Jadwal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



/*
 * Cara menjalankan:
 * 1. Pastikan sudah berada di direktori proyek
 *    cd ruang_pandai
 * 2. Jalankan perintah:
 *    mvn test
 */
class CariTutorTest {

    private static SiswaController siswaController;

    @BeforeAll
    static void setUp() {
        System.setProperty("app.env", "development");
        DatabaseInitializer.initialize();
        siswaController = new SiswaController();
    }

    @Test
    @DisplayName("Skenario 1: Cari Tutor Tanpa Filter")
    void testCariTutor_tanpaFilter() {
        List<Tutor> hasil = siswaController.cariTutor(null, 0, null, null, null);
        assertNotNull(hasil, "Hasil tidak boleh null");
        assertEquals(2, hasil.size(), "Harusnya menemukan 2 tutor");
    }

    @Test
    @DisplayName("Skenario 2: Cari Tutor Berdasarkan Nama")
    void testCariTutor_berdasarkanNama() {
        List<Tutor> hasil = siswaController.cariTutor(null, 0, "Citra", null, null);
        assertNotNull(hasil);
        assertEquals(1, hasil.size(), "Harusnya menemukan 1 tutor bernama Citra");
        assertEquals("Citra Dewi", hasil.get(0).getNama(), "Nama tutor harusnya Citra Dewi");
    }

    @Test
    @DisplayName("Skenario 3: Cari Tutor Berdasarkan Mata Pelajaran")
    void testCariTutor_berdasarkanMataPelajaran() {
        List<Tutor> hasil = siswaController.cariTutor("Matematika", 0, null, null, null);
        assertNotNull(hasil);
        assertEquals(1, hasil.size(), "Harusnya menemukan 1 tutor Matematika");
        assertEquals("Citra Dewi", hasil.get(0).getNama(), "Tutor Matematika harusnya Citra Dewi");
    }
    
    @Test
    @DisplayName("Skenario 4: Cari Tutor Berdasarkan Rating")
    void testCariTutor_berdasarkanRating() {
        List<Tutor> hasil = siswaController.cariTutor(null, 4, null, null, null);
        assertNotNull(hasil);
        assertEquals(1, hasil.size(), "Harusnya menemukan 1 tutor dengan rating 4");
        assertEquals("Dedi Kurniawan", hasil.get(0).getNama(), "Tutor dengan rating 4 harusnya Dedi Kurniawan");
    }
    
    @Test
    @DisplayName("Skenario 5: Cari Tutor Berdasarkan Tanggal Tersedia")
    void testCariTutor_berdasarkanTanggal() {
        LocalDate tanggal = LocalDate.of(2025, 6, 12);
        List<Tutor> hasil = siswaController.cariTutor(null, 0, null, tanggal, null);
        assertNotNull(hasil);
        assertEquals(1, hasil.size(), "Harusnya ada 1 tutor tersedia pada tanggal tersebut");
        assertEquals("Citra Dewi", hasil.get(0).getNama(), "Tutor yang tersedia harusnya Citra Dewi");
        
        assertFalse(hasil.get(0).getJadwal().isEmpty(), "Jadwal tutor seharusnya tidak kosong");
    }
    
    @Test
    @DisplayName("Skenario 6: Cari Tutor Berdasarkan Tanggal dan Waktu")
    void testCariTutor_berdasarkanTanggalDanWaktu() {
        LocalDate tanggal = LocalDate.of(2025, 6, 12);
        String waktuMulai = "13:00";
        List<Tutor> hasil = siswaController.cariTutor(null, 0, null, tanggal, waktuMulai);
        assertNotNull(hasil);
        assertEquals(1, hasil.size(), "Harusnya menemukan 1 tutor pada waktu spesifik");
        assertEquals("Citra Dewi", hasil.get(0).getNama());
    }

    @Test
    @DisplayName("Skenario 7: Kombinasi Filter (Mapel dan Rating)")
    void testCariTutor_kombinasiFilter() {
        List<Tutor> hasil = siswaController.cariTutor("Bahasa Inggris", 4, null, null, null);
        assertNotNull(hasil);
        assertEquals(1, hasil.size(), "Harusnya menemukan 1 tutor dengan kriteria tersebut");
        assertEquals("Dedi Kurniawan", hasil.get(0).getNama());
    }
    
    @Test
    @DisplayName("Skenario 8: Hasil Pencarian Kosong")
    void testCariTutor_hasilKosong() {
        List<Tutor> hasil = siswaController.cariTutor("Kimia", 5, null, null, null);
        assertNotNull(hasil);
        assertTrue(hasil.isEmpty(), "Harusnya tidak menemukan tutor Kimia");
    }
    
    @Test
    @DisplayName("Skenario 9: Filter Tanggal Tidak Cocok")
    void testCariTutor_tanggalTidakCocok() {
        LocalDate tanggal = LocalDate.of(2025, 12, 25);
        List<Tutor> hasil = siswaController.cariTutor(null, 0, null, tanggal, null);
        assertNotNull(hasil);
        assertTrue(hasil.isEmpty(), "Harusnya tidak ada tutor pada tanggal ini");
    }
}