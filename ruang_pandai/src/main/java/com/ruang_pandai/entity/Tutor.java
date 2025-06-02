package com.ruang_pandai.entity;
public class Tutor extends Pengguna {
    private String mataPelajaran;
    private String pendidikan;
    private String pengalaman;
    private String ulasan;
    private Integer rating;

    // Constructor lengkap (termasuk atribut dari Pengguna)
    public Tutor(
        String idPengguna,
        String nama,
        String role,
        String email,
        String noTelp,
        String alamat,
        String mataPelajaran,
        String pendidikan,
        String pengalaman,
        String ulasan,
        Integer rating
    ) {
        super(idPengguna, nama, role, email, noTelp, alamat);
        this.mataPelajaran = mataPelajaran;
        this.pendidikan = pendidikan;
        this.pengalaman = pengalaman;
        this.ulasan = ulasan;
        this.rating = rating;
    }

    // Getter & Setter
    public String getMataPelajaran() {
        return mataPelajaran;
    }

    public void setMataPelajaran(String mataPelajaran) {
        this.mataPelajaran = mataPelajaran;
    }

    public String getPendidikan() {
        return pendidikan;
    }

    public void setPendidikan(String pendidikan) {
        this.pendidikan = pendidikan;
    }

    public String getPengalaman() {
        return pengalaman;
    }

    public void setPengalaman(String pengalaman) {
        this.pengalaman = pengalaman;
    }

    public String getUlasan() {
        return ulasan;
    }

    public void setUlasan(String ulasan) {
        this.ulasan = ulasan;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

}
