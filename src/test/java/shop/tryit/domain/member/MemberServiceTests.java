package shop.tryit.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import shop.tryit.domain.member.entity.Member;
import shop.tryit.domain.member.repository.MemberRepository;
import shop.tryit.domain.member.service.MemberService;
import shop.tryit.repository.member.MemberJpaRepository;

@Transactional
@SpringBootTest
class MemberServiceTests {

    @Autowired
    private MemberService sut;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 회원가입_테스트() {
        //given
        Member member = Member.builder()
                .email("test1234@gmail.com")
                .name("test")
                .password("11111")
                .build();

        //when
        Long savedMember = sut.saveMember(member);

        //then
        assertThat(memberJpaRepository.findById(savedMember).isPresent()).isTrue();
    }

    @Test
    void 중복_회원_성공_테스트() {
        //given
        Member member1 = Member.builder()
                .email("test1234@gmail.com")
                .name("test")
                .password("11111")
                .build();

        Member member2 = Member.builder()
                .email("test1234@gmail.com")
                .name("test")
                .password("11111")
                .build();

        //when
        sut.saveMember(member1);

        //then
        assertThatThrownBy(() -> sut.saveMember(member2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 가입된 회원입니다.");
    }

    @Test
    void 회원_조회_성공_테스트() {
        //given
        Member member1 = Member.builder()
                .email("test1234@gmail.com")
                .name("test")
                .password("11111")
                .build();

        memberJpaRepository.save(member1);

        entityManager.flush();
        entityManager.clear();

        //when
        Member findMember = sut.findMember("test1234@gmail.com");

        //then
        assertThat(findMember).usingRecursiveComparison().ignoringFields("id").isEqualTo(member1);
    }

    @Test
    void 회원_조회_실패_테스트() {
        //when
        assertThatThrownBy(() -> sut.findMember("test1234@gmail.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("해당 회원을 찾을 수 없습니다.");
    }

    @Test
    void 회원_탈퇴_테스트() {
        //given
        Member member1 = Member.builder()
                .email("test1234@gmail.com")
                .name("test")
                .password("11111")
                .build();

        Member savedmember = memberJpaRepository.save(member1);

        //when
        sut.delete(member1);

        //then
        assertThat(memberRepository.findByEmail(savedmember.getEmail()).isPresent()).isFalse();
    }

}