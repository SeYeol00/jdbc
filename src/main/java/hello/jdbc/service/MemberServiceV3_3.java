package hello.jdbc.service;


import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 * 트랜젝션 - @Transactional AOP
 */

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    // 트랜잭션은 결국 비즈니스 로직 자체를 하나의 트랜잭션으로 봐야한다.
    // 원자성을 지키려면 비즈니스 로직 단위의 함수를 트랜잭션으로 보는게 옳다.


    // 모든 트랜잭션 서비스 버전의 핵심을 프록시 객체로 해결하는 어노테이션
    // 프록시 객체를 통해 비즈니스 로직을 제외한 모든 트랜젝션 관련 코드를 이 어노테이션으로 해결한다.
    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId,toId,money);
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        // 커넥션을 여기서 만들어야 비즈니스 로직 == 트랜젝션 이 원자성을 지킨다.
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId,fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId,toMember.getMoney() + money);
    }

    private static void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
