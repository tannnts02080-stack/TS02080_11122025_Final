package app;

public class SinhVien {
    private String maSV;
    private String ten;
    private int tuoi;
    private float diemTrungBinh;
    private int kyHoc;
    private String chuyenNganh;

    public SinhVien(String maSV, String ten, int tuoi, float diemTrungBinh, int kyHoc, String chuyenNganh) {
        this.maSV = maSV;
        this.ten = ten;
        this.tuoi = tuoi;
        this.diemTrungBinh = diemTrungBinh;
        this.kyHoc = kyHoc;
        this.chuyenNganh = chuyenNganh;
    }

    // getters + setters
    public String getMaSV(){ return maSV; }
    public String getTen(){ return ten; }
    public int getTuoi(){ return tuoi; }
    public float getDiemTrungBinh(){ return diemTrungBinh; }
}
