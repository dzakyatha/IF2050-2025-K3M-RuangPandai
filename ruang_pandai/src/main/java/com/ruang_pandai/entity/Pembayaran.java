package com.ruang_pandai.entity;
public class Pembayaran {
    private String idPembayaran;
    private String idSesi;
    private Integer jumlah;
    private String metodePembayaran;
    private String buktiPembayaran;
    private String tanggalPembayaran;
    private String statusPembayaran;

    // Constructor

    public Pembayaran(String idPembayaran, String idSesi, Integer jumlah,
                      String metodePembayaran, String buktiPembayaran,
                      String tanggalPembayaran, String statusPembayaran) {
        this.idPembayaran = idPembayaran;
        this.idSesi = idSesi;
        this.jumlah = jumlah;
        this.metodePembayaran = metodePembayaran;
        this.buktiPembayaran = buktiPembayaran;
        this.tanggalPembayaran = tanggalPembayaran;
        this.statusPembayaran = statusPembayaran;
    }

    // Getter & Setter
    public String getIdPembayaran() { return idPembayaran; }
    public void setIdPembayaran(String idPembayaran) { this.idPembayaran = idPembayaran; }

    public String getIdSesi() { return idSesi; }
    public void setIdSesi(String idSesi) { this.idSesi = idSesi; }

    public Integer getJumlah() { return jumlah; }
    public void setJumlah(Integer jumlah) { this.jumlah = jumlah; }

    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String metodePembayaran) { this.metodePembayaran = metodePembayaran; }

    public String getBuktiPembayaran() { return buktiPembayaran; }
    public void setBuktiPembayaran(String buktiPembayaran) { this.buktiPembayaran = buktiPembayaran; }

    public String getTanggalPembayaran() { return tanggalPembayaran; }
    public void setTanggalPembayaran(String tanggalPembayaran) { this.tanggalPembayaran = tanggalPembayaran; }

    public String getStatusPembayaran() { return statusPembayaran; }
    public void setStatusPembayaran(String statusPembayaran) { this.statusPembayaran = statusPembayaran; }
}
