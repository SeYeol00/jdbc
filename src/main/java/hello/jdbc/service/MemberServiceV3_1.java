package hello.jdbc.service;


import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜젝션 - 트랜잭션 매니저
 * 내가 직접 커넥션을 열고 닫을 필요가 없다.
 */

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    // 트랜잭션은 결국 비즈니스 로직 자체를 하나의 트랜잭션으로 봐야한다.
    // 원자성을 지키려면 비즈니스 로직 단위의 함수를 트랜잭션으로 보는게 옳다.
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        // 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try{
            // 비즈니스 로직 수행 시작
            bizLogic(fromId, toId, money);
            // 커밋
            transactionManager.commit(status); //성공 시 커밋
        }catch (Exception e){

            transactionManager.rollback(status);// 실패시 롤백
            throw new IllegalStateException(e);
        }

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
