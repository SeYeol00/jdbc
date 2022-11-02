package hello.jdbc.repository.ex;




// 체크 익셉션을 런타임익셉션으로 감싸야 종속성이 사라진다.
public class MyDbException extends RuntimeException {
    // 체크 익셉션을 감싸기 때문에 cause를 들고와야 한다.
    // 우리 예제에서는 cause가 SQLException이기 때문에 e에 SQLException이 들어가는 것이다.
    public MyDbException() {
    }

    public MyDbException(String message) {
        super(message);
    }

    public MyDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDbException(Throwable cause) {
        super(cause);
    }
}
