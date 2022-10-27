package hello.jdbc.repository;


import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j // jdbc는 데이터베이스를 연결하는 것부터 닫는 순서 모두 개입해야한다.
public class MemberRepositoryV0 {

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

            //실행 코드
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

    public Member findById(String memberId){
        String sql = "select * from member where member_id = ?";
    }

    private void close(Connection con, Statement stmt, ResultSet rs){
        // 결과 조회할 떄 사용
        if(rs != null) {
            try {
                rs.close(); // 익셉션이 터지면 밖으로 나간다.
            } catch (SQLException e) {
                log.error("error", e);
            }
        }

        if(stmt != null){
            try{
                stmt.close(); // 익셉션이 터지면 밖으로 나간다.
            }catch (SQLException e){
                log.error("error",e);
            }
        }

        if(con != null){
            try{
                con.close(); // 익셉션이 터지면 밖으로 나간다.
            }catch (SQLException e){
                log.error("error",e);
            }
        }



    }


    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
