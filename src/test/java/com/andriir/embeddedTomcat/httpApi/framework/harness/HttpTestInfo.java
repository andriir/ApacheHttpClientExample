package au.com.clarity.test.httpApi.framework.harness;

import au.com.clarity.test.httpApi.framework.data.HttpTestUser;

/**
 * Created by Andrew Redko on 11/30/15.
 */
public class HttpTestInfo {
    private String manageHostURL;
    private String siteHostURL;
    private HttpTestUser manageAdminUser;
    private HttpTestUser siteUser;
    private String testGuid;

    public String getManageHostURL() {
        return manageHostURL;
    }

    public void setManageHostURL(String manageHostURL) {
        manageHostURL = removeLeading(manageHostURL, "http://");
        manageHostURL = removeLasting(manageHostURL, "/");
        this.manageHostURL = manageHostURL;
    }

    public String getSiteHostURL() {
        return siteHostURL;
    }

    public void setSiteHostURL(String siteHostURL) {
        this.siteHostURL = siteHostURL;
    }

    public HttpTestUser getManageAdminUser() {
        return manageAdminUser;
    }

    public void setManageAdminUser(HttpTestUser manageAdminUser) {
        this.manageAdminUser = manageAdminUser;
    }

    public HttpTestUser getSiteUser() {
        return siteUser;
    }

    public void setSiteUser(HttpTestUser siteUser) {
        this.siteUser = siteUser;
    }

    public String getFullUrl(String url) {
        url = removeLeading(url, "/");
        return "http://" + getSiteHostURL() + "/" + url;
    }

    private String removeLeading(String url, String value) {
        if (url != null && value != null && url.startsWith(value)) {
            url = url.substring(value.length());
        }
        return url;
    }

    private String removeLasting(String manageHostURL, String value) {
        if (manageHostURL != null && value != null && manageHostURL.endsWith("/")) {
            manageHostURL = manageHostURL.substring(0, manageHostURL.length() - value.length());
        }
        return manageHostURL;
    }

    public void setTestGuid(String testGuid) {
        this.testGuid = testGuid;
    }

    public String getTestGuid() {
        return testGuid;
    }
}