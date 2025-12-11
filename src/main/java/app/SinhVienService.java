package app;

import java.util.*;

public class SinhVienService {
    private final Map<String, SinhVien> db = new HashMap<>();

    public boolean add(SinhVien sv){
        if (sv == null) throw new IllegalArgumentException("Null student");
        if (sv.getMaSV() == null || sv.getMaSV().isBlank()) return false;
        if (db.containsKey(sv.getMaSV())) return false;
        if (sv.getTuoi() < 17) return false;
        db.put(sv.getMaSV(), sv);
        return true;
    }

    public boolean remove(String maSV){
        return db.remove(maSV) != null;
    }

    public SinhVien find(String maSV){ return db.get(maSV); }

    public int total(){ return db.size(); }
}
