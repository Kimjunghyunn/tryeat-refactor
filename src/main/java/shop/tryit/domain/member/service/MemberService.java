package shop.tryit.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.tryit.domain.member.dto.EmailRequest;
import shop.tryit.domain.member.entity.Member;
import shop.tryit.domain.member.repository.MemberRepository;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    @Transactional
    public Long saveMember(Member member) {
        validateDuplicateMember(member);
        String encodedPassword = passwordEncoder.encode(member.getPassword());
        member.Password(encodedPassword);
        return repository.save(member);
    }

    public Member findMember(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("해당 회원을 찾을 수 없습니다."));
    }

    private void validateDuplicateMember(Member member) {
        if (repository.existsByEmail(member.getEmail())) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Transactional
    public String update(String email, Member newMember) {
        Member findMember = findMember(email);
        String newEncodedPassword = passwordEncoder.encode(newMember.getPassword());
        newMember.Password(newEncodedPassword);
        findMember.update(newMember.getName(), newMember.getAddress(),
                newMember.getPhoneNumber(), newMember.getPassword());
        return findMember.getEmail();
    }

    /**
     * 이메일 인증번호 전송
     */
    @Transactional
    public String authEmail(EmailRequest emailRequest) {
        Random random = new Random();
        String authKey = String.valueOf(random.nextInt(888888) + 111111); // 범위 : 111111 ~ 999999

        sendAuthEmail(emailRequest.getEmail(), authKey);
        return authKey;
    }

    /**
     * 이메일 전송 시 내용에 해당
     */
    private void sendAuthEmail(String email, String authKey) {
        String subject = "츄라잇 인증번호 입니다.";
        String text = "회원 가입을 위한 인증번호는 " + authKey + "입니다.<br/>";

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text, true); // 포함된 텍스트가 HTML 이라는 의미로 true
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}