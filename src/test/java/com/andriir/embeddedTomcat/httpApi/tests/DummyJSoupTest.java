package au.com.clarity.test.httpApi.tests;

/**
 * Created by Andrew Redko on 11/30/15.
 */

import au.com.clarity.test.httpApi.framework.BaseHttpTest;
import au.com.clarity.test.httpApi.framework.data.HttpTestUser;
import au.com.clarity.test.httpApi.framework.harness.HttpClientHarness;
import au.com.clarity.test.httpApi.framework.harness.HttpRequestHarness;
import au.com.clarity.test.httpApi.framework.harness.HttpTestInfo;
import org.apache.http.message.HeaderGroup;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class DummyJSoupTest extends BaseHttpTest {
    private static final Logger LOGGER = Logger.getLogger(DummyJSoupTest.class);

    @Test
    public void dummyTest() throws Exception {
        //setup TestInfo
        HttpTestInfo testInfo = new HttpTestInfo();
        testInfo.setManageHostURL("localhost:8081");
        testInfo.setSiteHostURL("localhost:8080");
        testInfo.setManageAdminUser(new HttpTestUser("manage.admin@test.com", "123"));
//        testInfo.setManageAdminUser(new HttpUser("license.admin@test.com", "123"));
        testInfo.setSiteUser(new HttpTestUser("comp1.permissionTestUser1217201428@test.com", "123"));

        // make sure cookies is turn on
//        CookieHandler.setDefault(new CookieManager());

        // setup Manage client
        HeaderGroup headerGroup = getManageHeaderGroup(testInfo);
        HttpClientHarness http = new HttpClientHarness(testInfo, headerGroup);
//        HttpRequestHarness formHarness = new HttpRequestHarness();

        //
        String permGroup = "SALES_QUETEEEE";

        try {
            //2. Relogin with user to the Site
            //2.1 send logout request
            Jsoup.connect(testInfo.getFullUrl("logout"))
                    .method(Connection.Method.GET)
                    .execute();

            //2.2 send login request
            http.sleepDefault();
            Connection.Response res = Jsoup.connect(testInfo.getFullUrl("login"))
                    .data("username", testInfo.getSiteUser().getLogin(), "password", testInfo.getSiteUser().getPassword())
                    .method(Connection.Method.POST)
                    .execute();

            Document doc = res.parse();
            String sessionId = res.cookie("ACCLOUD_SITE_JSESSIONID"); // you will need to check what the right cooki


            //3. Try to open the page
            //3.1 send get request
            String url = "sales/salesQuoteList.html";
            http.sleepDefault();
            Connection.Response res2 = Jsoup.connect(testInfo.getFullUrl("sales/salesQuoteList.html"))
                    .cookie("ACCLOUD_SITE_JSESSIONID", sessionId)
                    .method(Connection.Method.GET)
                    .ignoreHttpErrors(true)
                    .execute();
            Document doc2 = res2.parse();

            LOGGER.debug(String.format("~~~~~~~~~~~~salesQuoteList returned:~~~~~~~~~~~~~~\n%s\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", doc2.toString()));
            String uniqueText = "PermissionTestUser1217201428";
            LOGGER.debug(String.format("Page contains \"%s\" text: %b", uniqueText, doc2.toString().contains(uniqueText)));
            uniqueText = "Access Denied";
            LOGGER.debug(String.format("Page contains \"%s\" text: %b", uniqueText, doc2.toString().contains(uniqueText)));

            //4. Verify that entity is NOT returned, but No Permission page only is returned instead
            //4.1 analyze DOM for some unique element
//        Elements infoSection =  doc.getElementsByClass("info-section-holder");
//        assertFalse(infoSection.size() == 0, String.format("Failed to verify permission [%s]. It is possible to get document [%s] by url [%s]", permGroup, infoSection.text(), url));
            //.ac-section-error
            Elements errorSection = doc2.getElementsByClass("ac-section-error");
            assertTrue(errorSection.get(0).text().contains("Access Denied"), String.format("Failed to verify permission [%s]. It is possible to get document [%s] by url [%s]", permGroup, errorSection.text(), url));
        } finally {
            http.close();
        }
    }
}
