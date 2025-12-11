package app;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SinhVienServiceTest {
    SinhVienService service;

    @BeforeMethod
    public void setup(){ service = new SinhVienService(); }

    @Test
    public void testAddValid(){
        SinhVien s = new SinhVien("SV001","A",18,7.5f,1,"CNTT");
        Assert.assertTrue(service.add(s));
        Assert.assertEquals(service.total(),1);
    }

    @Test
    public void testAddDuplicate(){
        SinhVien s1 = new SinhVien("SV001","A",18,7.5f,1,"CNTT");
        SinhVien s2 = new SinhVien("SV001","B",19,6.0f,2,"KT");
        Assert.assertTrue(service.add(s1));
        Assert.assertFalse(service.add(s2));
    }

    @Test
    public void testAddUnderage(){
        SinhVien s = new SinhVien("SV002","C",16,8.0f,1,"CNTT");
        Assert.assertFalse(service.add(s));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddNull(){
        service.add(null);
    }

    @Test
    public void testRemove(){
        SinhVien s = new SinhVien("SV003","D",20,6.5f,1,"VH");
        service.add(s);
        Assert.assertTrue(service.remove("SV003"));
        Assert.assertNull(service.find("SV003"));
    }

    @Test
    public void testFindNotExist(){
        Assert.assertNull(service.find("NOPE"));
    }

    @Test
    public void testTotalMultiple(){
        service.add(new SinhVien("SV01","A",18,7f,1,"CNTT"));
        service.add(new SinhVien("SV02","B",19,6f,1,"KT"));
        service.add(new SinhVien("SV03","C",20,8f,1,"VH"));
        Assert.assertEquals(service.total(),3);
    }

    @Test
    public void testAddEmptyMaSV(){
        SinhVien s = new SinhVien("","E",18,7f,1,"CNTT");
        Assert.assertFalse(service.add(s));
    }

    @Test
    public void testAddExactly17(){
        SinhVien s = new SinhVien("SV17","F",17,6.5f,1,"CNTT");
        Assert.assertTrue(service.add(s));
    }

    @Test
    public void testRemoveNonExisting(){
        Assert.assertFalse(service.remove("ABC"));
    }
}
