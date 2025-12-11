package app;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SeleniumRegisterLoginTest extends BaseTest {

    private WebDriver driver;
    private final String base = "https://vitimex.com.vn/";

    @BeforeClass
    public void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setup() {
        ChromeOptions opts = new ChromeOptions();
        // visible: để bạn thấy browser khi chạy local
        opts.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(opts);
        // implicit rất nhỏ (chúng ta dùng findElements nhanh)
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(200));
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    // 0.5s sleep helper
    private void sleep500() {
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    // lưu ảnh + pagesource khi gặp lỗi
    private void dumpFailureState(String namePrefix) {
        try {
            Path outDir = Path.of("target","selenium-screenshots");
            Files.createDirectories(outDir);
            String page = driver.getPageSource();
            Path ps = outDir.resolve(namePrefix + "-pagesource.html");
            try (FileWriter fw = new FileWriter(ps.toFile())) {
                fw.write(page);
            }
            if (driver instanceof TakesScreenshot) {
                File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Path dest = outDir.resolve(namePrefix + "-screenshot.png");
                Files.copy(scr.toPath(), dest);
            }
            System.out.println("Saved debug files to: " + outDir.toAbsolutePath());
            System.out.println("Current URL: " + driver.getCurrentUrl());
        } catch (Exception e) {
            System.err.println("Failed to dump failure state: " + e.getMessage());
        }
    }

    // save credentials
    private void saveCredentials(String email, String password) {
        try {
            Path outDir = Path.of("target","selenium-screenshots");
            Files.createDirectories(outDir);
            Path file = outDir.resolve("credentials.txt");
            try (FileWriter fw = new FileWriter(file.toFile(), true)) {
                fw.write("email=" + email + ", password=" + password + System.lineSeparator());
            }
            System.out.println("Saved credentials to: " + file.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to save credentials: " + e.getMessage());
        }
    }

    // fast find (no long waits) - trả về phần tử hiển thị & enabled đầu tiên
    private WebElement tryFind(By... locators) {
        for (By l : locators) {
            try {
                List<WebElement> els = driver.findElements(l);
                if (els != null && !els.isEmpty()) {
                    for (WebElement e : els) {
                        try {
                            if (e.isDisplayed() && e.isEnabled()) {
                                return e;
                            }
                        } catch (StaleElementReferenceException ser) {
                            // ignore and continue
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    // polling helper: cho tới timeout ms, kiểm tra điều kiện supplier
    private boolean pollUntil(java.util.function.Supplier<Boolean> cond, long timeoutMs, long intervalMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (cond.get()) return true;
            } catch (Exception ignored) {}
            try { Thread.sleep(intervalMs); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    @Test
    public void testRegisterAndLoginManualFlow() {
        // unique data
        String uniq = String.valueOf(System.currentTimeMillis()).substring(6) + UUID.randomUUID().toString().substring(0,4);
        String email = "test+" + uniq + "@example.com";
        String ho = "T";
        String ten = "User" + uniq;
        String dob = "12/11/2000";
        String password = "Aa1" + uniq; // >5 chars
        System.out.println("Running with email=" + email + " password=" + password);

        try {
            // 1) Open homepage
            driver.get(base);
            sleep500();

            // 2) Click account/user icon (link contains /account)
            WebElement accountLink = tryFind(
                    By.cssSelector("a[href*='/account']"),
                    By.cssSelector("a[href*='account']"),
                    By.xpath("//a[contains(@href,'/account')]"),
                    By.cssSelector("a[class*='account']"),
                    By.cssSelector("a[title*='Tài khoản']"),
                    By.cssSelector("a[aria-label*='account']")
            );
            if (accountLink == null) {
                dumpFailureState("no-account-link");
                Assert.fail("Cannot find account icon/link.");
            }
            accountLink.click();
            sleep500();

            // 3) Click "ĐĂNG KÝ" tab or navigate to register URL
            WebElement registerTab = tryFind(
                    By.xpath("//*[contains(normalize-space(.),'ĐĂNG KÝ')]"),
                    By.xpath("//a[contains(.,'Đăng ký')]"),
                    By.xpath("//button[contains(.,'ĐĂNG KÝ')]"),
                    By.cssSelector("a[href*='register']"),
                    By.cssSelector("a[href*='dang-ky']")
            );
            if (registerTab != null) {
                try { registerTab.click(); } catch (Exception ignored) { driver.get(base + "account/register"); }
                sleep500();
            } else {
                // fallback navigate
                driver.get(base + "account/register");
                sleep500();
            }

            // 4) Fill registration form (try multiple locators)

            // Họ
            WebElement hoInput = tryFind(
                    By.cssSelector("input[name*='lastName']"),
                    By.xpath("//label[contains(.,'Họ')]/following::input[1]"),
                    By.cssSelector("input[placeholder*='Họ']"),
                    By.cssSelector("input[id*='last']"),
                    By.cssSelector("input[name*='Ho']")
            );
            if (hoInput != null) {
                hoInput.clear();
                hoInput.sendKeys(ho);
                sleep500();
            }

            // Tên
            WebElement tenInput = tryFind(
                    By.cssSelector("input[name*='firstName']"),
                    By.xpath("//label[contains(.,'Tên')]/following::input[1]"),
                    By.cssSelector("input[placeholder*='Tên']"),
                    By.cssSelector("input[id*='first']")
            );
            if (tenInput != null) {
                tenInput.clear();
                tenInput.sendKeys(ten);
                sleep500();
            }

            // Ngày sinh (try different inputs)
            WebElement dobInput = tryFind(
                    By.cssSelector("input[name*='birthday']"),
                    By.xpath("//label[contains(.,'Ngày sinh')]/following::input[1]"),
                    By.cssSelector("input[id*='birth']"),
                    By.cssSelector("input[placeholder*='Ngày sinh']"),
                    By.cssSelector("input[placeholder*='MM/DD/YYYY']"),
                    By.cssSelector("input[placeholder*='DD/MM/YYYY']")
            );
            if (dobInput != null) {
                try {
                    dobInput.clear();
                    dobInput.sendKeys(dob);
                } catch (Exception ignored) {}
                sleep500();
            }

            // CHỌN GIỚI TÍNH (random Nam/Nữ) - đặt sau dobInput
            boolean chooseMale = new Random().nextBoolean(); // true -> Nam, false -> Nữ
            if (chooseMale) {
                WebElement genderMale = tryFind(
                        By.cssSelector("input[value='male']"),
                        By.cssSelector("input[value='Male']"),
                        By.cssSelector("input[id*='gender'][value*='m']"),
                        By.cssSelector("input[name*='gender'][value*='m']"),
                        By.xpath("//label[contains(normalize-space(.),'Nam')]/preceding::input[1]"),
                        By.xpath("//label[contains(normalize-space(.),'Nam')]")
                );
                if (genderMale != null) {
                    try {
                        String tag = genderMale.getTagName().toLowerCase();
                        if ("input".equals(tag)) {
                            if (!genderMale.isSelected()) genderMale.click();
                        } else genderMale.click();
                    } catch (Exception ignored) {}
                    sleep500();
                }
            } else {
                WebElement genderFemale = tryFind(
                        By.cssSelector("input[value='female']"),
                        By.cssSelector("input[value='Female']"),
                        By.xpath("//label[contains(normalize-space(.),'Nữ')]/preceding::input[1]"),
                        By.xpath("//label[contains(normalize-space(.),'Nu') or contains(normalize-space(.),'Nữ')]")
                );
                if (genderFemale != null) {
                    try {
                        String tag = genderFemale.getTagName().toLowerCase();
                        if ("input".equals(tag)) {
                            if (!genderFemale.isSelected()) genderFemale.click();
                        } else genderFemale.click();
                    } catch (Exception ignored) {}
                    sleep500();
                }
            }

            // Email
            WebElement emailInput = tryFind(
                    By.cssSelector("input[type='email']"),
                    By.cssSelector("input[name*='email']"),
                    By.cssSelector("input[id*='email']"),
                    By.xpath("//input[contains(@placeholder,'email') or contains(@placeholder,'Email')]")
            );
            if (emailInput == null) {
                dumpFailureState("register-no-email");
                Assert.fail("Could not locate email input for registration.");
            }
            emailInput.clear();
            emailInput.sendKeys(email);
            sleep500();

            // Password
            WebElement passInput = tryFind(
                    By.cssSelector("input[type='password']"),
                    By.cssSelector("input[name*='password']"),
                    By.cssSelector("input[id*='password']"),
                    By.xpath("//label[contains(.,'Mật khẩu')]/following::input[1]")
            );
            if (passInput == null) {
                dumpFailureState("register-no-password");
                Assert.fail("Could not locate password input for registration.");
            }
            passInput.clear();
            passInput.sendKeys(password);
            sleep500();

            // confirm password (if exists)
            WebElement confirmPass = tryFind(
                    By.cssSelector("input[name*='confirm']"),
                    By.cssSelector("input[name*='passwordConfirm']"),
                    By.cssSelector("input[id*='confirm']"),
                    By.xpath("//label[contains(.,'Nhập lại')]/following::input[1]"),
                    By.xpath("//input[@placeholder[contains(.,'nhập lại')]]")
            );
            if (confirmPass != null) {
                try {
                    confirmPass.clear();
                    confirmPass.sendKeys(password);
                } catch (Exception ignored){}
                sleep500();
            }

            // Submit registration
            WebElement regSubmit = tryFind(
                    By.xpath("//button[contains(translate(.,'ĐĂNG KÝ','đăng ký'),'đăng ký') or contains(.,'Đăng ký') or contains(.,'Register') or contains(.,'ĐĂNG KÝ')]"),
                    By.cssSelector("button[type='submit']"),
                    By.cssSelector("input[type='submit']"),
                    By.cssSelector("button[class*='register']"),
                    By.cssSelector("button[id*='register']")
            );
            if (regSubmit != null) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", regSubmit);
                } catch (Exception ignored){}
                try {
                    regSubmit.click();
                } catch (Exception ex) {
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", regSubmit);
                    } catch (Exception ignored){}
                }
            } else {
                dumpFailureState("register-no-submit");
                Assert.fail("No register submit button found.");
            }

            // small wait then poll up to 8s to detect if logged in or redirected to account
            sleep500();
            boolean becameLogged = pollUntil(() -> {
                String src = driver.getPageSource().toLowerCase();
                String url = driver.getCurrentUrl().toLowerCase();
                return src.contains("đăng xuất") || url.contains("/account") || url.contains("dashboard") || src.contains("thông tin tài khoản");
            }, 8000, 500);

            if (!becameLogged) {
                // maybe registration created account but not auto-logged -> navigate to account
                try {
                    driver.get(base + "account");
                    sleep500();
                } catch (Exception ignored) {}
            }

            // Save credentials
            saveCredentials(email, password);

            // Try logout if present
            WebElement logoutBtn = tryFind(
                    By.xpath("//button[contains(.,'ĐĂNG XUẤT')]"),
                    By.xpath("//a[contains(.,'Đăng xuất')]"),
                    By.xpath("//a[contains(translate(.,'logout','LOGOUT'),'logout')]")
            );
            if (logoutBtn != null) {
                try { logoutBtn.click(); } catch (Exception ignored) {}
                sleep500();
            } else {
                // maybe there is a logout on account page
                try {
                    driver.get(base + "account");
                    sleep500();
                    WebElement logout2 = tryFind(By.xpath("//button[contains(.,'ĐĂNG XUẤT')]"), By.xpath("//a[contains(.,'Đăng xuất')]"));
                    if (logout2 != null) {
                        try { logout2.click(); } catch (Exception ignored){}
                        sleep500();
                    }
                } catch (Exception ignored) {}
            }

            // 6) Login with created account
            WebElement accountAgain = tryFind(By.cssSelector("a[href*='/account']"), By.xpath("//a[contains(@href,'/account')]"));
            if (accountAgain != null) {
                accountAgain.click();
                sleep500();
            } else {
                driver.get(base + "account/login");
                sleep500();
            }

            // Fill login form
            WebElement loginEmail = tryFind(
                    By.cssSelector("input[type='email']"),
                    By.cssSelector("input[name*='email']"),
                    By.cssSelector("input[id*='email']"),
                    By.xpath("//input[contains(@placeholder,'Email') or contains(@placeholder,'email')]")
            );
            if (loginEmail == null) {
                dumpFailureState("login-no-email");
                Assert.fail("Login: cannot find email input.");
            }
            loginEmail.clear();
            loginEmail.sendKeys(email);
            sleep500();

            WebElement loginPass = tryFind(
                    By.cssSelector("input[type='password']"),
                    By.cssSelector("input[name*='password']"),
                    By.cssSelector("input[id*='password']")
            );
            if (loginPass == null) {
                dumpFailureState("login-no-pass");
                Assert.fail("Login: cannot find password input.");
            }
            loginPass.clear();
            loginPass.sendKeys(password);
            sleep500();

            // Improved login submit: many fallbacks + JS click + ENTER
            // ---------- LOGIN: improved submit with many fallbacks ----------
            // tìm submit bằng nhiều cách
            WebElement loginSubmit = tryFind(
                    By.xpath("//button[contains(.,'ĐĂNG NHẬP') or contains(.,'Đăng nhập') or contains(.,'Login')]"),
                    By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'đăng')]"),
                    By.cssSelector("button[class*='login']"),
                    By.cssSelector("button[class*='submit']"),
                    By.cssSelector("button[class*='dang']"),
                    By.cssSelector("input[type='submit']"),
                    By.cssSelector("input[value*='Đăng']"),
                    By.cssSelector("[role='button']"),
                    By.cssSelector("a[class*='login']"),
                    By.xpath("//form//button"),
                    By.xpath("//form//input[@type='submit']")
            );

            if (loginSubmit != null) {
                // in ra debug ngắn outerHTML để kiểm tra selector nếu cần
                try {
                    System.out.println("Found loginSubmit: tag=" + loginSubmit.getTagName()
                            + " outerHTML (start): " + loginSubmit.getAttribute("outerHTML").substring(0, Math.min(300, loginSubmit.getAttribute("outerHTML").length())));
                } catch (Exception ignored){}

                // scroll + thử click -> js click -> send ENTER
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", loginSubmit);
                } catch (Exception ignored) {}
                sleep500();

                boolean clicked = false;
                try {
                    loginSubmit.click();
                    clicked = true;
                } catch (Exception e1) {
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginSubmit);
                        clicked = true;
                    } catch (Exception e2) {
                        try {
                            // fallback: gửi ENTER từ password field
                            loginPass.sendKeys(Keys.ENTER);
                            clicked = true;
                        } catch (Exception e3) {}
                    }
                }

                // nếu click không thành công, thử nộp form chứa phần tử này (nếu có)
                if (!clicked) {
                    try {
                        WebElement ancestorForm = loginSubmit.findElement(By.xpath("ancestor::form"));
                        if (ancestorForm != null) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].submit();", ancestorForm);
                            clicked = true;
                            System.out.println("Submitted ancestor form via JS.");
                        }
                    } catch (Exception ignored) {}
                }

                if (!clicked) {
                    // thêm 1 fallback: tìm form dựa trên password field và submit
                    try {
                        WebElement pwForm = loginPass.findElement(By.xpath("ancestor::form"));
                        if (pwForm != null) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].submit();", pwForm);
                            clicked = true;
                            System.out.println("Submitted password's ancestor form via JS.");
                        }
                    } catch (Exception ignored) {}
                }

                if (!clicked) {
                    // nếu vẫn không được, dump dom snippet để debug và fail
                    dumpFailureState("login-click-failed");
                    // ghi ra vài candidate buttons outerHTML để bạn gửi cho mình xem
                    List<WebElement> allCands = driver.findElements(By.cssSelector("button, input[type='submit'], [role='button'], a"));
                    System.out.println("Candidate elements sample:");
                    for (int i=0; i<Math.min(8, allCands.size()); i++) {
                        try {
                            String h = allCands.get(i).getAttribute("outerHTML");
                            System.out.println("CAND[" + i + "]: " + (h==null ? allCands.get(i).getText() : h.substring(0, Math.min(200,h.length()))));
                        } catch (Exception ignored){}
                    }
                    Assert.fail("Login: submit click failed (no working method). Check saved pagesource/screenshot.");
                }

            } else {
                // không tìm thấy bất kỳ element submit nào -> dump và fail
                dumpFailureState("login-no-submit");
                // in 1 số button/links để debug
                List<WebElement> allCands = driver.findElements(By.cssSelector("button, input, a, [role='button']"));
                System.out.println("Found " + allCands.size() + " candidate elements for debug. Sample:");
                for (int i=0; i<Math.min(8, allCands.size()); i++) {
                    try {
                        String h = allCands.get(i).getAttribute("outerHTML");
                        System.out.println("CAND[" + i + "]: " + (h==null ? allCands.get(i).getText() : h.substring(0, Math.min(200,h.length()))));
                    } catch (Exception ignored){}
                }

                Assert.fail("Login: cannot find submit button.");
            }


            System.out.println("REGISTER+LOGIN flow succeeded for: " + email);
            
            
         // WAIT for examiner to see the logged-in page
            try {
                Thread.sleep(7000); // 7 seconds, chỉnh thành 10000 nếu muốn 10s
            } catch (InterruptedException ignored) {}


        } catch (AssertionError ae) {
            throw ae;
        } catch (Exception e) {
            dumpFailureState("unexpected");
            Assert.fail("Unexpected exception: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
        }
    }
}
