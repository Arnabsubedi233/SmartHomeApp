package com.smarthome;

public class ContactConfig {
    private final String homeownerPhone;
    private final String homeownerEmail;
    private final String authorityPhone;

    public ContactConfig(String homeownerPhone, String homeownerEmail, String authorityPhone) {
        this.homeownerPhone = homeownerPhone;
        this.homeownerEmail = homeownerEmail;
        this.authorityPhone = authorityPhone;
    }

    public String getHomeownerPhone() { return homeownerPhone; }
    public String getHomeownerEmail() { return homeownerEmail; }
    public String getAuthorityPhone() { return authorityPhone; }

    public ContactConfig withHomeownerPhone(String v) { return new ContactConfig(v, homeownerEmail, authorityPhone); }
    public ContactConfig withHomeownerEmail(String v) { return new ContactConfig(homeownerPhone, v, authorityPhone); }
    public ContactConfig withAuthorityPhone(String v) { return new ContactConfig(homeownerPhone, homeownerEmail, v); }
}
