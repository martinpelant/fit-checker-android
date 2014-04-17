package cz.mpelant.fitchecker.auth;

/**
 * KosAccount.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.auth
 * @since 4/17/2014
 */
public class KosAccount {
    private String username;
    private String password;
    private int authType;


    public KosAccount(String username, String password, int authType) {
        this.username = username;
        this.password = password;
        this.authType=authType;
    }
    public KosAccount(String username, String password) {
        this.username = username;
        this.password = password;
        this.authType=KosAccountManager.AUTH_OPTION;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getAuthType() {
        return authType;
    }
}

