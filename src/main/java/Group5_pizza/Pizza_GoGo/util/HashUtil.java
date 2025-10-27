package Group5_pizza.Pizza_GoGo.util;

import java.security.MessageDigest;

public class HashUtil {

    public static String sha256ToMd5(String input) {
        try {
            // SHA-256
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(input.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for(byte b : hash) hex.append(String.format("%02x", b));
            String shaHex = hex.toString();

            // MD5
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5Hash = md5.digest(shaHex.getBytes("UTF-8"));
            StringBuilder md5Hex = new StringBuilder();
            for(byte b : md5Hash) md5Hex.append(String.format("%02x", b));

            return md5Hex.toString();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}