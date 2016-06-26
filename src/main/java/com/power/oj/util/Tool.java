package com.power.oj.util;

import com.jfinal.log.Logger;
import com.power.oj.core.OjConfig;
import jodd.mail.Email;
import jodd.mail.EmailMessage;
import jodd.mail.MailException;
import jodd.mail.SendMailSession;
import jodd.mail.SimpleAuthenticator;
import jodd.mail.SmtpServer;
import jodd.util.MimeTypes;
import jodd.util.StringTemplateParser;
import jodd.util.StringTemplateParser.MacroResolver;
import jodd.util.StringUtil;
import org.apache.shiro.SecurityUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;

/**
 * Utils for common usage.
 *
 * @author power
 */
public class Tool {
    private final static Logger log = Logger.getLogger(Tool.class);

    private static final char[] CHAR_STR =
        {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '=', '+', '<', '>', '/', '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'Q',
            'R', 'T', 'Y'};

    /**
     * @param url string of url
     * @return string of formatted base url
     */
    public static String formatBaseURL(String url) {
        while (url != null && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * Send Email with string content.
     *
     * @param from    the email address of sender.
     * @param to      the email address of receiver.
     * @param subject the title of email.
     * @param content string of email body.
     * @throws Exception
     */
    public static void sendEmail(String from, String to, String subject, String content) throws Exception {
        EmailMessage textMessage = new EmailMessage(content, MimeTypes.MIME_TEXT_PLAIN);
        sendEmail(from, to, subject, textMessage);
    }

    /**
     * Send Email with EmailMessage content.
     *
     * @param from    the email address of sender.
     * @param to      the email address of receiver.
     * @param subject the title of email.
     * @param content EmailMessage of email body.
     * @throws Exception
     */
    public static void sendEmail(String from, String to, String subject, EmailMessage content) {
        // TODO: create new thread to send mail
        Email email = new Email();

        email.setFrom(from);
        email.setTo(to);
        email.setSubject(subject);
        email.addMessage(content);

        String emailServer = OjConfig.getString("emailServer");
        String emailUser = OjConfig.getString("emailUser");
        String emailPass = OjConfig.getString("emailPass");
        SmtpServer smtpServer = new SmtpServer(emailServer, new SimpleAuthenticator(emailUser, emailPass));

        SendMailSession session = smtpServer.createSession();
        try {
            session.open();
            session.sendMail(email);
            log.info("Send mail from: " + from + " to: " + to + " subject: " + subject);
        } catch (MailException e) {
            log.error("send email failed!", e);
        } finally {
            session.close();
        }
    }

    public static String parseStringTemplate(final String template, final Map<String, String> map) {
        StringTemplateParser stp = new StringTemplateParser();
        String result = stp.parse(template, new MacroResolver() {
            public String resolve(String macroName) {
                return map.get(macroName);
            }
        });

        return result;
    }

    public static int getDayTimestamp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    public static String randomPassword(int count) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            sb.append(CHAR_STR[rand.nextInt(CHAR_STR.length)]);
        }
        return sb.toString();
    }

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return SecurityUtils.getSubject().getSession().getHost();
        }

        String ip = request.getHeader("x-forwarded-for");
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        return request.getHeader("User-Agent");
    }

    public static boolean getBoolean(Boolean value) {
        if (value == null) {
            return false;
        }
        return value;
    }

}
