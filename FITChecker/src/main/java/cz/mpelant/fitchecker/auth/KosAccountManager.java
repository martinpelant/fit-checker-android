package cz.mpelant.fitchecker.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import cz.mpelant.fitchecker.App;
import cz.mpelant.fitchecker.utils.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * KosAccountManager.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.auth
 * @since 4/17/2014
 */
public class KosAccountManager {
    private static final String PREFERENCES_USERNAME = "username";
    private static final String PREFERENCES_PASSWORD = "password";
    static final int AUTH_OPTION = 3;
    private static KosAccount mAccount;


    public static void saveAccount(KosAccount account) {
        SharedPreferences.Editor ed = getSp().edit();
        ed.putString(PREFERENCES_USERNAME, account.getUsername());

        try {//try to encrypt password
            String encrypted = encrypt(generateKey(), account.getPassword());
            ed.putString(PREFERENCES_PASSWORD, encrypted);
        } catch (Exception e) {
            e.printStackTrace();//rollback to plain text storage if encryption fails
            Log.e("KosAccountManager", "WARNING: storing plaintext password");
            ed.putString(PREFERENCES_PASSWORD, account.getPassword());
        }
        ed.commit();
        mAccount = account;
    }

    public static boolean isAccount() {
        return getSp().contains(PREFERENCES_USERNAME) && getSp().contains(PREFERENCES_PASSWORD);
    }

    public static KosAccount getAccount() {
        if (mAccount == null) {

            String encryptedPassword = getSp().getString(PREFERENCES_PASSWORD, null);
            String password;
            try {//try to decrypt password
                password = decrypt(generateKey(), encryptedPassword);
            } catch (Exception e) {
                e.printStackTrace();//rollback to plain text if encryption fails
                Log.e("KosAccountManager", "WARNING: storing plaintext password");
                password = encryptedPassword;
            }

            mAccount = new KosAccount(getSp().getString(PREFERENCES_USERNAME, null), password, AUTH_OPTION);
        }

        return mAccount;
    }

    public static void deleteAccount() {
        SharedPreferences.Editor ed = getSp().edit();
        ed.remove(PREFERENCES_USERNAME);
        ed.remove(PREFERENCES_PASSWORD);
        ed.commit();
    }

    private static SharedPreferences getSp() {
        return App.getInstance().getSharedPreferences("eduxcred", Context.MODE_PRIVATE);
    }

    private static String generateKey() throws UnsupportedEncodingException, NoSuchAlgorithmException {

        String uuid = null;
        try {
            TelephonyManager tManager = (TelephonyManager) App.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
            uuid = tManager.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }


        String key = Build.FINGERPRINT + "-" + Build.SERIAL + "-" + Build.ID;
        key += uuid;


        String shaKey = convertToHex(SHA1(key));
        shaKey = shaKey.substring(0, 32);
        return shaKey;
    }

    public static byte[] SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        return md.digest();
    }

    private static String encrypt(String key, String clearText) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clearText.getBytes());
        return Base64.encodeBytes(encrypted);
    }

    private static String decrypt(String key, String encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(Base64.decode(encrypted));
        return new String(decrypted);
    }


    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }
}
