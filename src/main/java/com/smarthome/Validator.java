package com.smarthome;

import java.util.regex.Pattern;

public final class Validator {
    private static final Pattern SENSOR_NAME = Pattern.compile("^[A-Za-z][A-Za-z0-9\\-\\s']{1,39}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]{1,64}@[A-Za-z0-9.-]{1,190}\\.[A-Za-z]{2,20}$");

    private Validator() {}

    public static boolean isValidSensorName(String s) {
        if (s == null) return false;
        String t = s.trim();
        if (!SENSOR_NAME.matcher(t).matches()) return false;

        // reject suspicious patterns (mirrors the unit slide examples)
        if (t.contains("--")) return false;
        if (t.contains("'") && t.indexOf('\'') != t.lastIndexOf('\'')) return false;
        return true;
    }

    public static boolean isValidPhone(String s) {
        if (s == null) return false;
        return PHONE.matcher(s.trim()).matches();
    }

    public static boolean isValidEmail(String s) {
        if (s == null) return false;
        return EMAIL.matcher(s.trim()).matches();
    }

    public static String maskPhone(String phone) {
        if (phone == null) return "(none)";
        String t = phone.trim();
        if (t.length() <= 4) return "****";
        return "****" + t.substring(t.length() - 4);
    }

    public static String maskEmail(String email) {
        if (email == null) return "(none)";
        String t = email.trim();
        int at = t.indexOf('@');
        if (at <= 1) return "***@***";
        String left = t.substring(0, at);
        String right = t.substring(at);
        return left.charAt(0) + "***" + right;
    }
}
