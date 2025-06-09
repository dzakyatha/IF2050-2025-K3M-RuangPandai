package com.ruang_pandai.controller;

import com.ruang_pandai.database.DatabaseInitializer;
import com.ruang_pandai.entity.Jadwal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Cara menjalankan:
 * 1. Pastikan sudah berada di direktori proyek
 *    cd ruang_pandai
 * 2. Jalankan perintah:
 *    mvn test
 */
class KelolaJadwalTest {

    private TutorController tutorController;
    private final String existingTutorId = "P3"; 


    @BeforeEach
    void setUp() {
        System.setProperty("app.env", "development");
        DatabaseInitializer.initialize(); 
        tutorController = new TutorController(); 
    }

    @Test
    @DisplayName("Sukses: Melihat jadwal awal untuk seorang tutor")
    void testLihatJadwal_Success() {
        List<Jadwal> jadwalList = tutorController.lihatJadwal(existingTutorId); 
        
        assertNotNull(jadwalList, "Daftar jadwal tidak boleh null.");
        assertFalse(jadwalList.isEmpty(), "Daftar jadwal untuk tutor dummy tidak boleh kosong.");
        assertEquals(2, jadwalList.size(), "Tutor P3 seharusnya memiliki 2 jadwal awal.");
    }

    @Test
    @DisplayName("Sukses: Membuat jadwal baru yang valid")
    void testBuatJadwal_Success() {
        Jadwal newJadwal = new Jadwal( 
            "J4", existingTutorId, "Matematika", "SABTU", 
            "2025-06-14", "10:00", "12:00", "TERSEDIA"
        );

        boolean result = tutorController.buatJadwal(newJadwal); 
        
        assertTrue(result, "Seharusnya berhasil membuat jadwal baru.");

        List<Jadwal> updatedList = tutorController.lihatJadwal(existingTutorId);
        assertEquals(3, updatedList.size(), "Jumlah jadwal tutor seharusnya bertambah menjadi 3.");
    }

    @Test
    @DisplayName("Gagal: Membuat jadwal yang tumpang tindih dengan yang sudah ada")
    void testBuatJadwal_Failure_Overlap() {
        Jadwal overlappingJadwal = new Jadwal( 
            "J5", existingTutorId, "Matematika", "SELASA", 
            "2025-06-08", "09:00", "11:00", "TERSEDIA"
        );

        boolean result = tutorController.buatJadwal(overlappingJadwal); 

        assertFalse(result, "Seharusnya gagal membuat jadwal yang tumpang tindih.");
        
        List<Jadwal> currentList = tutorController.lihatJadwal(existingTutorId);
        assertEquals(2, currentList.size(), "Jumlah jadwal seharusnya tidak berubah.");
    }

    @Test
    @DisplayName("Sukses: Memperbarui jadwal tersedia yang sudah ada")
    void testUbahJadwal_Success() {
        Jadwal jadwalToUpdate = new Jadwal( 
            "J2", existingTutorId, "Matematika", "KAMIS", 
            "2025-06-12", "14:00", "16:00", "TERSEDIA"
        );

        boolean result = tutorController.ubahJadwal(jadwalToUpdate); 

        assertTrue(result, "Seharusnya berhasil memperbarui jadwal yang tersedia.");

        List<Jadwal> jadwalList = tutorController.lihatJadwal(existingTutorId);
        Jadwal updatedJadwal = jadwalList.stream()
            .filter(j -> "J2".equals(j.getIdJadwal()))
            .findFirst()
            .orElse(null);

        assertNotNull(updatedJadwal, "Jadwal yang diperbarui seharusnya ada.");
        assertEquals("Matematika", updatedJadwal.getMataPelajaran(), "Mata pelajaran seharusnya diperbarui.");
        assertEquals("14:00", updatedJadwal.getJamMulai(), "Jam mulai seharusnya diperbarui.");
    }

    @Test
    @DisplayName("Gagal: Mencoba memperbarui jadwal yang sudah dipesan")
    void testUbahJadwal_Failure_AlreadyBooked() {
        Jadwal jadwalToUpdate = new Jadwal( 
            "J1", existingTutorId, "Matematika", "SELASA", 
            "2025-06-08", "08:00", "10:00", "DIPESAN"
        );
        
        boolean result = tutorController.ubahJadwal(jadwalToUpdate);

        assertFalse(result, "Seharusnya gagal memperbarui jadwal yang sudah dipesan.");
    }

    @Test
    @DisplayName("Gagal: Memperbarui jadwal ke waktu yang tumpang tindih dengan jadwal lain")
    void testUbahJadwal_Failure_Overlap() {
        Jadwal jadwalToUpdate = new Jadwal( 
            "J2", existingTutorId, "Matematika", "SELASA",
            "2025-06-08", "09:00", "11:00", "TERSEDIA" 
        );

        boolean result = tutorController.ubahJadwal(jadwalToUpdate); 

        assertFalse(result, "Seharusnya gagal memperbarui jadwal ke slot waktu yang tumpang tindih.");
    }

    @Test
    @DisplayName("Sukses: Menghapus jadwal yang tersedia")
    void testHapusJadwal_Success() {
        boolean result = tutorController.hapusJadwal("J2"); 

        assertTrue(result, "Seharusnya berhasil menghapus jadwal yang tersedia.");

        List<Jadwal> updatedList = tutorController.lihatJadwal(existingTutorId);
        long count = updatedList.stream().filter(j -> "J2".equals(j.getIdJadwal())).count();
        assertEquals(0, count, "Jadwal yang dihapus seharusnya tidak ada lagi.");
        assertEquals(1, updatedList.size(), "Ukuran daftar jadwal seharusnya berkurang menjadi 1.");
    }

    @Test
    @DisplayName("Gagal: Mencoba menghapus jadwal yang sudah dipesan")
    void testHapusJadwal_Failure_AlreadyBooked() {
        boolean result = tutorController.hapusJadwal("J1"); 

        assertFalse(result, "Seharusnya gagal menghapus jadwal yang sudah dipesan.");
        
        List<Jadwal> currentList = tutorController.lihatJadwal(existingTutorId);
        assertEquals(2, currentList.size(), "Jumlah jadwal seharusnya tidak berubah setelah gagal menghapus.");
    }
}