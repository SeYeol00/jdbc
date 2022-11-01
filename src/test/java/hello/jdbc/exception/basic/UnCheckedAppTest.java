package hello.jdbc.exception.basic;


import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;


// 임의로 실패 메세지를 보낼 때는 런타임 익셉션을 쓰고
// 이를 컨트롤러 어드바이스(스프링 MVC 2편 익셉션 핸들러 참조)에서
// RestAPI로 변환하여 메세지를 보내는 방식이 클라이언트에게
// 실패 메세지를 보내는 방식이다.
@Slf4j
public class UnCheckedAppTest {


    @Test
    void unchecked(){
        Controller controller = new Controller();

        Assertions.assertThatThrownBy(()->controller.request())
                .isInstanceOf(Exception.class);
    }

    @Test
    void printEx(){
        Controller controller = new Controller();

        try{
            controller.request();
        }catch (Exception e){
            //e.printStackTrace() 이건 sout으로 나가니까 쓰지 말자
            // 실무는 로그를 찍는다.
            log.info("ex",e);
        }

    }

    static class Controller{

        Service service = new Service();

        public void request() {
            service.logic();
        }
    }

    static class Service{
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic()  {
//            networkClient.call();
            repository.call();
        }
    }

    static class NetworkClient{
        public void call() {
            throw new RuntimeConnectException("연결 실패");
        }
    }

    static class Repository{
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                // 체크드익셉션을 런타임익셉션으로 바꿔서 던질 수 있다.
                throw new RuntimeSQLException(e); // 기존 예외를 포함해야한다.
                // 이래야 추적이 가능함
            }
        }
        public void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RuntimeConnectException extends RuntimeException{

        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException{

        public RuntimeSQLException(String message) {
            super(message);
        }
        // 이전 예외를 포함해서 보낼 수 있다.
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }



}
