
package com.ljl.studyexammanagementsystem.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码加密工具类 - MD5加盐加密
 * 采用固定盐值 + 密码拼接后MD5摘要的方式
 */
public class PasswordUtil {

    private static final String SALT = "exam@2026";

    /**
     * MD5加盐加密
     */
    public static String encrypt(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest((SALT + password).getBytes());
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 校验密码：将用户输入的明文密码加盐加密后，与数据库存储的密文比对
     */
    public static boolean verify(String inputPassword, String storedPassword) {
        if (inputPassword == null || storedPassword == null) {
            return false;
        }
        return encrypt(inputPassword).equals(storedPassword);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
