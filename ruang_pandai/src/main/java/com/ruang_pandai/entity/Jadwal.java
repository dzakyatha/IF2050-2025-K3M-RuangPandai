package com.ruang_pandai.entity;

public class Jadwal {
    private String idJadwal;
    private String idTutor;
    private String mataPelajaran;
    private String hari;
    private String tanggal;
    private String jamMulai;
    private String jamSelesai;
    private String statusJadwal;


    public Jadwal(String idJadwal, String idTutor, String mataPelajaran, String hari, String tanggal,
                  String jamMulai, String jamSelesai, String statusJadwal) {
        this.idJadwal = idJadwal;
        this.idTutor = idTutor;
        this.mataPelajaran = mataPelajaran;
        this.hari = hari;
        this.tanggal = tanggal;
        this.jamMulai = jamMulai;
        this.jamSelesai = jamSelesai;
        this.statusJadwal = statusJadwal;
    }

    // Getter & Setter

    public String getIdJadwal() {
        return idJadwal;
    }

    public void setIdJadwal(String idJadwal) {
        this.idJadwal = idJadwal;
    }

    public String getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(String idTutor) {
        this.idTutor = idTutor;
    }

    public String getMataPelajaran() {
        return mataPelajaran;
    }

    public void setMataPelajaran(String mataPelajaran) {
        this.mataPelajaran = mataPelajaran;
    }

    public String getHari() {
        return hari;
    }

    public void setHari(String hari) {
        this.hari = hari;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getJamMulai() {
        return jamMulai;
    }

    public void setJamMulai(String jamMulai) {
        this.jamMulai = jamMulai;
    }

    public String getJamSelesai() {
        return jamSelesai;
    }

    public void setJamSelesai(String jamSelesai) {
        this.jamSelesai = jamSelesai;
    }

    public String getStatusJadwal() {
        return statusJadwal;
    }

    public void setStatusJadwal(String statusJadwal) {
        this.statusJadwal = statusJadwal;
    }
}
