package hello.jdbc.service;


import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 * 트랜젝션 - 트랜잭션 템플릿
 * try - catch 문의 반복적인 코드를 없애고 싶다.
 */

@Slf4j
public class MemberServiceV3_2 {

//    private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate transactionTemplate;

    private final MemberRepositoryV3 memberRepository;


    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.transactionTemplate = new TransactionTemplate(transactionManager); // 트랜잭션 매니저를 주입 받은 걸  생성자에 넣는다.
        this.memberRepository = memberRepository;
    }

    // 트랜잭션은 결국 비즈니스 로직 자체를 하나의 트랜잭션으로 봐야한다.
    // 원자성을 지키려면 비즈니스 로직 단위의 함수를 트랜잭션으로 보는게 옳다.
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // 람다식
        transactionTemplate.executeWithoutResult((status)->
        {
            try {
                bizLogic(fromId,toId,money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
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
