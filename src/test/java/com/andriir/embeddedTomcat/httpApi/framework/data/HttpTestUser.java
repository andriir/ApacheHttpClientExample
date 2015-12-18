package au.com.clarity.test.httpApi.framework.data;

/**
 * Created by Andrew Redko on 11/30/15.
 */
public class HttpTestUser {
    private String Login;
    private String Password;

    public HttpTestUser(String login, String password) {
        Login = login;
        Password = password;
    }

    public String getLogin() {
        return Login;
    }

    public void setLogin(String login) {
        Login = login;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}