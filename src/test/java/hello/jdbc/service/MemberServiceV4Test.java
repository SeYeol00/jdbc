package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV4_1;
import hello.jdbc.repository.MemberRepositoryV4_2;
import hello.jdbc.repository.MemberRepositoryV5;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 예외 누수 문제 해결
 * SQLException 제거
 *
 * MemberRepository 인터페이스 의존
 */

// 스프링 관련 어노테이션을 쓰고 싶으면 스프링 컨테이너가 있어야한다.
@Slf4j
@SpringBootTest // 테스트를 돌릴 때 이 어노테이션으로 스프링을 하나 띄운다. 그럼 의존관계 주입도 다 된다.
class MemberServiceV4Test {

    public static final String MEMBER_A = "member_A";
    public static final String MEMBER_B = "member_B";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberServiceV4 memberService;

    @TestConfiguration // 테스트에서 AppConfig와 같은 역할을 하는 클래스, 테스트 시 빈 등록용 클래스다.
    static class TestConfig{

        private final DataSource dataSource;


        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }


        @Bean // 빈 주입하기, 인터페이스 구현체 등록
        MemberRepository memberRepository(){
            return new MemberRepositoryV5(dataSource);
        }

        @Bean
        MemberServiceV4 memberService(){
            return new MemberServiceV4(memberRepository());
        }

    }

   @AfterEach
    void afterEach() {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }



    @Test
    @DisplayName("정상 이체")
    void accountTransfer() {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
        log.info("START TX");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(),2000);
        log.info("END TX");


        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);

    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEX = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEX);

        //when
        assertThatThrownBy(()-> memberService.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(),2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEX.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
        // 예외가 터지면서 con.rollback()으로 롤백시키는 코드를 넣어놨기 떄문에
        // 둘 다 결국 만원, 만원 그대로 남게된 것

    }
}