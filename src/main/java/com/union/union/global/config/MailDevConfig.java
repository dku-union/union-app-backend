package com.union.union.global.config;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 로컬 개발 환경에서 SMTP 발송 없이 콘솔 로그만 남기는 메일 sender.
 *
 * <p>활성화: {@code mail.dev.log-only=true} (application-local.yml에서 설정)
 *
 * <p>OTP 6자리는 메일 본문에서 추출해 검증·테스트용으로 별도 라인에 기록한다.
 * 운영 환경에는 이 빈이 등록되지 않으므로 동작에 영향 없음.
 */
@Slf4j
@Configuration
public class MailDevConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "mail.dev", name = "log-only", havingValue = "true")
    public JavaMailSender logOnlyMailSender(@Value("${spring.mail.host:noop}") String host) {
        log.warn("[MailDevConfig] log-only mail sender 활성. 실제 메일 발송이 비활성화됩니다.");
        return new LogOnlyMailSender();
    }

    /** SMTP 연결 없이 메일 내용을 콘솔에만 출력. */
    static class LogOnlyMailSender extends JavaMailSenderImpl {

        private static final Pattern OTP_PATTERN = Pattern.compile("(?<![0-9])([0-9]{6})(?![0-9])");

        @Override
        public MimeMessage createMimeMessage() {
            // SMTP 세션 없이 메시지 객체만 생성 (테스트용)
            Session session = Session.getInstance(new Properties());
            return new MimeMessage(session);
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) {
            return createMimeMessage();
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            try {
                String to = String.join(",", asArray(mimeMessage.getAllRecipients()));
                String subject = mimeMessage.getSubject();
                String body = extractText(mimeMessage);
                Matcher m = OTP_PATTERN.matcher(body);
                String otp = m.find() ? m.group(1) : "<not-found>";

                log.info("[Mail/log-only] to={}, subject={}, otp={}", to, subject, otp);
            } catch (MessagingException e) {
                log.warn("[Mail/log-only] 메일 메타데이터 파싱 실패", e);
            }
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            for (MimeMessage m : mimeMessages) send(m);
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            log.info("[Mail/log-only] simple to={}, body={}", simpleMessage.getTo(), simpleMessage.getText());
        }

        private String[] asArray(jakarta.mail.Address[] addresses) {
            if (addresses == null) return new String[0];
            String[] out = new String[addresses.length];
            for (int i = 0; i < addresses.length; i++) out[i] = addresses[i].toString();
            return out;
        }

        private String extractText(MimeMessage message) throws MessagingException {
            try {
                Object content = message.getContent();
                if (content instanceof String s) return s;
                return content == null ? "" : content.toString();
            } catch (Exception e) {
                return "";
            }
        }
    }
}
