package com.ruang_pandai.entity;
public abstract class Pengguna {
    private String idPengguna;
    private String nama;
    private String role;
    private String email;
    private String noTelp;
    private String alamat;

    public Pengguna(String idPengguna, String nama, String role, String email, String noTelp, String alamat) {
        this.idPengguna = idPengguna;
        this.nama = nama;
        this.role = role;
        this.email = email;
        this.noTelp = noTelp;
        this.alamat = alamat;
    }

    // Getter & Setter

    public String getIdPengguna() {
        return idPengguna;
    }

    public void setIdPengguna(String idPengguna) {
        this.idPengguna = idPengguna;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNoTelp() {
        return noTelp;
    }

    public void setNoTelp(String noTelp) {
        this.noTelp = noTelp;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }
}
