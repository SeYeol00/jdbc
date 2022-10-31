package hello.jdbc.repository;


import hello.jdbc.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 * DataSourceUtils.getConnection() -> 커넥션 열 때
 * DataSourceUtils.releaseConnection() -> 커넥션 닫을 때
 */

@Slf4j // jdbc는 데이터베이스를 연결하는 것부터 닫는 순서 모두 개입해야한다.
@RequiredArgsConstructor
public class MemberRepositoryV3 {

    private final DataSource dataSource;

    public Member save(Member member) throws SQLException {
        
        // 날릴 쿼리문          sql인젝션이 생길 수 있기 떄문에 ?를 써서 데이터를 반환하자. -> 파라미터 바인딩
        String sql = "insert into member(member_id, money) values (? , ?)";
        
        // 이거 두 개를 잘 써야 한다. 애미가 없다. jdbc 하
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            //파라미터 바인딩
            pstmt.setString(1,member.getMemberId());
            pstmt.setInt(2,member.getMoney());

            //실행 코드, insert는 아래 업데이트 함수를 쓴다.
            pstmt.executeUpdate();

            return member;

        }catch (SQLException e){
            log.error("db error",e);
            throw e;
        }finally{
            // 둘 다 닫아줘야한다. 그래야 트랜젝션이 끝난다.
            // 이걸 리소스 정리라고 한다.
            // 리소스 정리할 떄는 항상 역순이다.ㅇ
            // 안 하면 리소스 누수가 생겨 커넥션 장애가 생길 수 있다.
            close(con,pstmt,null);
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";


        // try, catch 때문에 밖에다 선언해야한다.
        // 이거 세 개를 잘 써야 한다. 커넥션 -> 프리페어드 스테이트먼트 순서
        Connection con = null;
        PreparedStatement pstmt = null;
        // select한 결과를 담고있는 통
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,memberId);

            // select문은 executeQuery를 쓴다.
            rs = pstmt.executeQuery();
            if(rs.next()) { // rs.next -> 커서, 넥스트를 호출하면 다음 인스턴스 데이터가 있는지 확인한다.
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else{// 데이터가 없다. 예외가 터질 때 어떤 멤버가 에러가 나는지 알아야하기 때문에 아이디 리턴
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }

        } catch (SQLException e) {
            log.error("db error",e);
            throw e;
        }finally{
            // 둘 다 닫아줘야한다. 그래야 트랜젝션이 끝난다.
            // 이걸 리소스 정리라고 한다.
            // 리소스 정리할 떄는 항상 역순이다.ㅇ
            // 안 하면 리소스 누수가 생겨 커넥션 장애가 생길 수 있다.
            close(con,pstmt,rs);
        }

    }


    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            //파라미터 바인딩
            pstmt.setInt(1,money);
            pstmt.setString(2,memberId);

            //실행 코드, update는 아래 업데이트 함수를 쓴다.
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}",resultSize);

        }catch (SQLException e){
            log.error("db error",e);
            throw e;

        }finally{
            // 둘 다 닫아줘야한다. 그래야 트랜젝션이 끝난다.
            // 이걸 리소스 정리라고 한다.
            // 리소스 정리할 떄는 항상 역순이다.ㅇ
            // 안 하면 리소스 누수가 생겨 커넥션 장애가 생길 수 있다.
            close(con,pstmt,null);
        }
    }


    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            //파라미터 바인딩, sql문에 있는 파라미터들을 연결시켜준다.
            pstmt.setString(1,memberId);
            //실행 코드, delete는 아래 업데이트 함수를 쓴다.
            pstmt.executeUpdate();

        }catch (SQLException e){
            log.error("db error",e);
            throw e;

        }finally{
            // 둘 다 닫아줘야한다. 그래야 트랜젝션이 끝난다.
            // 이걸 리소스 정리라고 한다.
            // 리소스 정리할 떄는 항상 역순이다.ㅇ
            // 안 하면 리소스 누수가 생겨 커넥션 장애가 생길 수 있다.
            close(con,pstmt,null);
        }
    }


    private void close(Connection con, Statement stmt, ResultSet rs){
        // 닫을 때는 반대 순서로 닫는다.
        // 결과 조회할 떄 사용
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        DataSourceUtils.releaseConnection(con,dataSource);
    }


    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}",con,con.getClass());
        return con;
    }
}
