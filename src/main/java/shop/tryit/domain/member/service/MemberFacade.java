package shop.tryit.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shop.tryit.domain.member.dto.EmailRequest;
import shop.tryit.domain.member.dto.MemberFormDto;
import shop.tryit.domain.member.entity.Address;
import shop.tryit.domain.member.entity.Member;
import shop.tryit.domain.member.entity.MemberRole;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacade {
    private final MemberService memberService;

    /**
     * 회원 가입
     */
    @Transactional
    public Long register(MemberFormDto memberForm) {
        MemberRole role = MemberRole.USER;
        Address address = toAddress(memberForm);
        Member member = toEntity(memberForm, address, role);
        return memberService.saveMember(member);
    }

    /**
     * 회원 수정 폼
     */
    public MemberFormDto updateForm(User user) {
        String userEmail = user.getUsername();
        Member member = memberService.findMember(userEmail);
        return toDto(member);
    }

    /**
     * 회원 수정 업데이트
     */
    @Transactional
    public String update(MemberFormDto memberForm) {
        Address address = toAddress(memberForm);
        Member member = toEntity(memberForm, address);
        return memberService.update(member.getEmail(), member);
    }

    /**
     * 이메일 인증번호 전송
     */
    public String authEmail(EmailRequest emailRequest) {
        return memberService.authEmail(emailRequest);
    }

    /**
     * 회원 탈퇴(삭제)
     */
    @Transactional
    public void delete(String email) {
        Member member = memberService.findMember(email);
        memberService.delete(member);
    }

    public Member toEntity(MemberFormDto memberFormDto, Address address, MemberRole role) {
        return Member.builder()
                .email(memberFormDto.getEmail())
                .name(memberFormDto.getName())
                .password(memberFormDto.getPassword1())
                .address(address)
                .phoneNumber(memberFormDto.getPhone())
                .role(role)
                .build();
    }

    public Member toEntity(MemberFormDto memberFormDto, Address address) {
        return Member.builder()
                .email(memberFormDto.getEmail())
                .name(memberFormDto.getName())
                .password(memberFormDto.getPassword1())
                .address(address)
                .phoneNumber(memberFormDto.getPhone())
                .build();
    }

    public Address toAddress(MemberFormDto memberForm) {
        return Address.builder()
                .zipcode(memberForm.getZipcode())
                .street_address(memberForm.getStreet_address())
                .jibeon_address(memberForm.getJibeon_address())
                .detail_address(memberForm.getDetail_address())
                .build();
    }

    public MemberFormDto toDto(Member member) {
        return MemberFormDto.builder()
                .email(member.getEmail())
                .name(member.getName())
                .phone(member.getPhoneNumber())
                .zipcode(member.getAddress().getZipcode())
                .jibeon_address(member.getAddress().getJibeon_address())
                .street_address(member.getAddress().getStreet_address())
                .detail_address(member.getAddress().getDetail_address())
                .build();
    }
}
