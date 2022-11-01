package hello.jdbc.service;


import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜젝션 - 파라미터 연동, 풀을 고려한 종료
 */

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    // 트랜잭션은 결국 비즈니스 로직 자체를 하나의 트랜잭션으로 봐야한다.
    // 원자성을 지키려면 비즈니스 로직 단위의 함수를 트랜잭션으로 보는게 옳다.
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try{
            // 트랜젝션 시작
            con.setAutoCommit(false);
            // 비즈니스 로직 수행 시작
            bizLogic(con, fromId, toId, money);
            // 커밋
            con.commit(); //성공 시 커밋
        }catch (Exception e){
            // 실패시 롤백
            con.rollback();
            throw new IllegalStateException(e);

        }finally{
            if(con!=null){
                try{
                    release(con);
                }catch (Exception e){
                    log.info("error message = {}",e.getMessage());
                }
            }
        }

    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        // 커넥션을 여기서 만들어야 비즈니스 로직 == 트랜젝션 이 원자성을 지킨다.
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con,fromId,fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con,toId,toMember.getMoney() + money);
    }

    private static void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
    private static void release(Connection con) throws SQLException {
        // 트랜젝션을 시작할 때 오토커밋을 껐으니까
        // 커밋이 끝난 후에는 다시 오토 커밋을 켜야 안전하다.
        // 커넥션 풀 고려
        con.setAutoCommit(true);
        // 커넥션을 이 때 반환해야한다.
        con.close();
    }



}
