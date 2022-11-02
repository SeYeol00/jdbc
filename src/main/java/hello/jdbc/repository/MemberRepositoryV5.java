package hello.jdbc.repository;


import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

/**
 * JdbcTemplate 사용, 여기까지 오기 위해 많은 변환을 거침
 * jdbc를 쓸 때 생기는 모든 코드들을 해결함
 * 우리는 sql만 집중하면 된다.
 * 기본적인 틀은 다 템플릿화 되어있는 것이다.
 */

@Slf4j // jdbc는 데이터베이스를 연결하는 것부터 닫는 순서 모두 개입해야한다.
public class MemberRepositoryV5 implements MemberRepository{

    // 핵심, 우리가 했던 트라이 캐치, 익셉션 처리 등을 이 템플릿이 알아서 다 해준다.
    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Member save(Member member) {
        
        // 날릴 쿼리문          sql인젝션이 생길 수 있기 떄문에 ?를 써서 데이터를 반환하자. -> 파라미터 바인딩
        String sql = "insert into member(member_id, money) values (? , ?)";
        template.update(sql,member.getMemberId(),member.getMoney());
        return member;
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        // 한 객체 뽑을 때 사용
        return template.queryForObject(sql,memberRowMapper(),memberId);
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";
        template.update(sql,money,memberId);
    }

    @Override
    public void delete(String memberId)  {
        String sql = "delete from member where member_id=?";
        template.update(sql,memberId);
    }
    private RowMapper<Member> memberRowMapper() {
        // 람다, 결과를 반환한다.
        return (rs,rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }
}
