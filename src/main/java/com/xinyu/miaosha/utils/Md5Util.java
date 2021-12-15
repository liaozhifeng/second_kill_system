package com.xinyu.miaosha.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class Md5Util {

    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    public static final String salt = "1a2b3c4d";

    public static String inputPassToFormPass(String inputPass) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    public static String formPassToDBPass(String inputPass, String salt) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    public static String inputPassToDbPass(String input, String saltDb) {
        String formPass = inputPassToFormPass(input);
        return formPassToDBPass(formPass, saltDb);
    }

    public static void main(String[] args) {
        System.out.println(inputPassToFormPass("123456"));
        System.out.println(inputPassToDbPass("123456", "1a2b3c4d"));
    }
}
