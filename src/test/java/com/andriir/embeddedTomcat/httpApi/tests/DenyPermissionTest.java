package au.com.clarity.test.httpApi.tests;

/**
 * Created by Andrew Redko on 11/30/15.
 */

import au.com.clarity.data.domain.security.PermissionAction;
import au.com.clarity.data.domain.security.PermissionGroup;
import au.com.clarity.test.httpApi.framework.BaseHttpPlusApiTest;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;

@Test(singleThreaded = true)
public class DenyPermissionTest extends BaseHttpPlusApiTest {

    @Test(priority = 100, enabled = true)
    @Transactional(value = "txnManagerAuth")
    @Rollback(false)
    public void dummyTest() throws Exception {
        //1. Give user all permission except one
        PermissionGroup permGroup = PermissionGroup.SALES_QUOTE;
        PermissionAction permAction = PermissionAction.READ;
        updateRoleSetAllAllowedExcept(permGroup, permAction);

        //2. Relogin with user to the Site
        //2.1 send logout request
        httpHarness.sendGet("logout");

        //2.2 send login request
        httpHarness.sleepDefault();
        httpHarness.sendPost("login", formHarness.getLoginParams(globalAccount));


        //3. Try to open the page
        //3.1 send get request
        String url = "sales/salesQuoteList.html";
        httpHarness.sleepDefault();
        Document doc = httpHarness.sendGet(url);

        //4. Verify that entity is NOT returned, but No Permission page only is returned instead
        //4.1 analyze DOM for some unique element
        Elements infoSection =  doc.getElementsByClass("info-section-holder");
//        assertFalse(infoSection.size() == 0, String.format("Failed to verify permission [%s]. It is possible to get document [%s] by url [%s]", permGroup, infoSection.text(), url));
        //.ac-section-error
        Elements errorSection =  doc.getElementsByClass(".ac-section-error");
        assertFalse(errorSection.get(0).text().contains("Access Denied"), String.format("Failed to verify permission [%s]. It is possible to get document [%s] by url [%s]", permGroup, infoSection.text(), url));

    }


//        Document doc = Jsoup.parse(html);
//
//        // Google form id
//        Element loginform = doc.getElementById("gaia_loginform");
//        Elements inputElements = loginform.getElementsByTag("input");
//
//        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
//
//        for (Element inputElement : inputElements) {
//            String key = inputElement.attr("name");
//            String value = inputElement.attr("value");
//
//            if (key.equals("Email"))
//                value = username;
//            else if (key.equals("Passwd"))
//                value = password;
//
//            paramList.add(new BasicNameValuePair(key, value));
//        }
}
