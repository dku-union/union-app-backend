package com.union.union.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String code) {
        String title = "[Union] 회원가입 이메일 인증 안내";
        String content = "<h3>안녕하세요, Union입니다.</h3>" +
                "<p>아래의 인증번호를 입력하여 이메일 인증을 완료해 주세요.</p>" +
                "<div style='background-color:#f4f4f4; padding:20px; text-align:center; font-size:24px; font-weight:bold; letter-spacing:5px;'>" + code + "</div>" +
                "<p>본 인증번호는 5분간 유효합니다.</p>" +
                "<p>감사합니다.</p>";

        sendEmail(to, title, content);
    }

    private void sendEmail(String to, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("Union 인증 <union.auth@gmail.com>");
            helper.setTo(to);
            helper.setSubject(title);
            helper.setText(content, true); // true indicates HTML

            mailSender.send(message);
            log.info("이메일 발송 성공: {}", to);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", to, e);
            throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.");
        }
    }
}
