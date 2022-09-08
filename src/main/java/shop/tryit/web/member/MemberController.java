package shop.tryit.web.member;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import shop.tryit.domain.member.dto.EmailRequest;
import shop.tryit.domain.member.dto.MemberFormDto;
import shop.tryit.domain.member.service.MemberFacade;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberFacade memberFacade;

    /**
     * 회원 가입
     */
    @GetMapping("/new")
    public String newMemberForm(@ModelAttribute("memberForm") MemberFormDto memberForm) {
        log.info("member controller");

        return "members/register";
    }

    @PostMapping("/new")
    public String newMember(@Valid @ModelAttribute("memberForm") MemberFormDto memberForm,
                            BindingResult bindingResult) {

        if (!memberForm.getPassword1().equals(memberForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "비밀번호가 일치하지 않습니다.");
        }

        if (!memberForm.isEmailAuth()){
            bindingResult.rejectValue("emailAuth", "emailAuthInCorrect",
                    "인증번호을 확인해주세요.");
        }

        if (bindingResult.hasErrors()) {
            log.info("member controller post");

            return "members/register";

        }

        memberFacade.register(memberForm);

        return "redirect:/";
    }

    /**
     * 이메일 전송
     */
    @PostMapping("/email")
    public ResponseEntity<String> authEmail(@RequestBody @Valid EmailRequest emailRequest) {
        log.info("member email controller");
        String authKey = memberFacade.authEmail(emailRequest);
        //성공했다면 인증번호와 함께 200 통신
        return new ResponseEntity<>(authKey, HttpStatus.OK);
    }

    /**
     * 로그인
     */
    @GetMapping("/login")
    public String login_form() {
        log.info("member login controller");

        return "members/login-form";

    }

    /**
     * 회원 프로필
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/update")
    public String editMemberForm(@AuthenticationPrincipal User user, Model model) {
        log.info("회원 수정 폼으로 이동");
        model.addAttribute("memberForm", memberFacade.updateForm(user));

        return "members/update";

    }

    @PostMapping("/update")
    public String editMember(@Valid @ModelAttribute("memberForm") MemberFormDto memberForm,
                             BindingResult bindingResult) {
        log.info("회원 수정으로 이동");
        if (!memberForm.getPassword1().equals(memberForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "비밀번호가 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            log.info("member controller post");

            return "members/update";

        }

        memberFacade.update(memberForm);

        return "members/update";

    }

    /**
     * 회원 탈퇴(삭제)
     */
    @GetMapping("/delete")
    public String deleteForm(@ModelAttribute EmailRequest emailRequest) {
        log.info("member delete form controller");

        return "members/delete";
    }

    /* TODO: view 구현 완료 후 EmailRequest -> User 로 변경하기 */
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestBody @Valid EmailRequest emailRequest) {
        log.info("member delete controller");
        memberFacade.delete(emailRequest.getEmail());

        return new ResponseEntity<>(HttpStatus.OK); //200
    }

}