package app;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CalculatorTest {
    Calculator c = new Calculator();

    @Test
    public void testAdd() { Assert.assertEquals(c.add(2,3), 5); }

    @Test
    public void testSub() { Assert.assertEquals(c.sub(5,3), 2); }

    @Test
    public void testMul() { Assert.assertEquals(c.mul(4,3), 12); }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDivByZero() { c.div(5,0); }

    @Test(dataProvider = "divData")
    public void testDiv(int a, int b, int expected) { Assert.assertEquals(c.div(a,b), expected); }

    @DataProvider(name = "divData")
    public Object[][] divData() {
        return new Object[][]{
            {10,2,5},
            {9,3,3},
            {7,1,7}
        };
    }
}
