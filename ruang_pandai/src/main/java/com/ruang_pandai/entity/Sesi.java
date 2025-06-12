package com.ruang_pandai.entity;
public class Sesi {
    private String idSesi;
    private String idSiswa;
    private String idTutor;
    private String idJadwal;
    private String tanggalPesan;
    private String statusPembayaran;
    private String statusKehadiran;
    private String statusSesi;

    // Constructor

    public Sesi(String idSesi, String idSiswa, String idTutor, String idJadwal,
                String tanggalPesan, String statusPembayaran, String statusKehadiran, String statusSesi) {
        this.idSesi = idSesi;
        this.idSiswa = idSiswa;
        this.idTutor = idTutor;
        this.idJadwal = idJadwal;
        this.tanggalPesan = tanggalPesan;
        this.statusPembayaran = statusPembayaran;
        this.statusKehadiran = statusKehadiran;
        this.statusSesi = statusSesi;
    }

    // Getter & Setter
    public String getIdSesi() { return idSesi; }
    public void setIdSesi(String idSesi) { this.idSesi = idSesi; }

    public String getIdSiswa() { return idSiswa; }
    public void setIdSiswa(String idSiswa) { this.idSiswa = idSiswa; }

    public String getIdTutor() { return idTutor; }
    public void setIdTutor(String idTutor) { this.idTutor = idTutor; }

    public String getIdJadwal() { return idJadwal; }
    public void setIdJadwal(String idJadwal) { this.idJadwal = idJadwal; }

    public String getTanggalPesan() { return tanggalPesan; }
    public void setTanggalPesan(String tanggalPesan) { this.tanggalPesan = tanggalPesan; }

    public String getStatusPembayaran() { return statusPembayaran; }
    public void setStatusPembayaran(String statusPembayaran) { this.statusPembayaran = statusPembayaran; }

    public String getStatusKehadiran() { return statusKehadiran; }
    public void setStatusKehadiran(String statusKehadiran) { this.statusKehadiran = statusKehadiran; }

    public String getStatusSesi() { return statusSesi; }
    public void setStatusSesi(String statusSesi) { this.statusSesi = statusSesi; }
}