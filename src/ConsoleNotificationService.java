public final class ConsoleNotificationService implements NotificationService {
    private final AuditLog log;

    public ConsoleNotificationService(AuditLog log) {
        this.log = log;
    }

    public NotificationSummary notifyAll(ContactConfig contacts, String subject, String details) {
        ServiceResult sms = sendSms(contacts.getHomeownerPhone(), subject);
        ServiceResult email = sendEmail(contacts.getHomeownerEmail(), subject);
        ServiceResult auth = callAuthority(contacts.getAuthorityPhone(), subject);
        return new NotificationSummary(sms, email, auth);
    }

    private ServiceResult sendSms(String phone, String subject) {
        if (!Validator.isValidPhone(phone)) {
            log.warn("SMS not sent: invalid phone=" + Validator.maskPhone(phone));
            return new ServiceResult(10, "invalid_phone");
        }
        System.out.println("Notification: SMS to " + phone + " | " + subject);
        log.info("SMS sent to " + Validator.maskPhone(phone) + " subject=" + subject);
        return new ServiceResult(ServiceResult.OK, "ok");
    }

    private ServiceResult sendEmail(String email, String subject) {
        if (!Validator.isValidEmail(email)) {
            log.warn("Email not sent: invalid email=" + Validator.maskEmail(email));
            return new ServiceResult(11, "invalid_email");
        }
        System.out.println("Notification: Email to " + email + " | " + subject);
        log.info("Email sent to " + Validator.maskEmail(email) + " subject=" + subject);
        return new ServiceResult(ServiceResult.OK, "ok");
    }

    private ServiceResult callAuthority(String phone, String subject) {
        if (!Validator.isValidPhone(phone)) {
            log.warn("Authority notification not sent: invalid phone=" + Validator.maskPhone(phone));
            return new ServiceResult(12, "invalid_authority_phone");
        }
        System.out.println("Notification: Authority contact " + phone + " | " + subject);
        log.warn("Authority contacted: " + Validator.maskPhone(phone) + " subject=" + subject);
        return new ServiceResult(ServiceResult.OK, "ok");
    }
}
