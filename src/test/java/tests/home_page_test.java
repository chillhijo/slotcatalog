package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import page.HomePage;

public class home_page_test extends BaseTest {

    public home_page_test() {
        super();
    }

    @Test
    public void verify_home_page_test_001()  {
        long startTime = System.currentTimeMillis();
        HomePage homePage = new HomePage(driver);
        homePage.verifyHomePage();
        homePage.loadJsonAndIterate();
        long endTime = System.currentTimeMillis();
        System.out.println("Test take: " + ((endTime - startTime) / 1000 ) + " seconds!");
    }
}
