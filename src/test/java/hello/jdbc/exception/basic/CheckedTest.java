package hello.jdbc.exception.basic;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
public class CheckedTest {

    /**
     * Exception을 상속받은 예외는 체크 예외가 된다.
     */
    static class MyCheckedException extends  Exception{

        public MyCheckedException(String message) {

        }
    }

    static class Service{
        Repository repository = new Repository();
        /**
         * 예외를 잡아서 처리하는 코드
         */
        public voi
    }
    static class Repository{ // 무조건 선언해야한다. 선언 안하면 컴파일러에서 못 읽는다,
        public void call() throws MyCheckedException {// 안 잡았으니 밖으로 던질게
            throw new MyCheckedException("ex");
        }
    }
}
