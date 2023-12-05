package me.test.resilience4j.fallback;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import me.test.resilience4j.exception.IgnoreException;
import me.test.resilience4j.exception.RecordException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CircuitBreakerService {

    private static final String SIMPLE_CIRCUIT_BREAKER_CONFIG = "simpleCircuitBreakerConfig";

    @CircuitBreaker(name = SIMPLE_CIRCUIT_BREAKER_CONFIG, fallbackMethod = "fallback")
    public String process(String param) throws InterruptedException {
        return callAnotherServer(param);
    }

    // fallback 으로 할 수 있는 일들 (클라이언트가 장애의 영향을 최소한으로 느낄 수 있는 안정적인 서비스를 위해)
    // param 으로 원래 해당 기능이 어떤 일을 수행하려고 했는지 알 수 있음
    // 추가적인 작업을 할 수 있음 (예: 실패했던 요청을 DB 같은 곳에 모아두었다가 나중에 재시도)
    // 대체 기능을 수행한 결과를 클라이언트에 반환

    // 예: 상품 목록 조회 API 요청시 리뷰 DB 에 문제가 생긴 경우
    // 리뷰 저장소가 아니라 캐시되어 있던 리뷰 데이터를 대신 조회해서 반환할 수 있음
    // 리뷰 DB 를 캐시 저장소와 동기화하는 추가 작업이 필요함
    private String fallback(String param, RecordException ex) {
        log.info("RecordException fallback! your request is " + param);
        return "Recovered: " + ex.toString();
    }

    private String fallback(String param, IgnoreException ex) {
        log.info("IgnoreException fallback! your request is " + param);
        return "Recovered: " + ex.toString();
    }

    // CallNotPermittedException 주석처리하고 테스트 해보면
    // 서킷을 OPEN 상태로 만들로 CallNotPermittedException 을 발생시키면
    // fallback 메서드에서 핸들링 되고있지 않기 때문에 클라이언트 까지 500 에러가 전달되고 stacktrace 까지 출력됨
    // 예: io.github.resilience4j.circuitbreaker.CallNotPermittedException: CircuitBreaker 'simpleCircuitBreakerConfig' is OPEN and does not permit further calls
    private String fallback(String param, CallNotPermittedException ex) {
        log.info("CallNotPermittedException fallback! your request is " + param);
        return "Recovered: " + ex.toString();
    }

    private String callAnotherServer(String param) throws InterruptedException {
        if ("a".equals(param)) {
            throw new RecordException("record exception");
        } else if ("b".equals(param)) {
            throw new IgnoreException("ignore exception");
        } else if ("c".equals(param)) {
            Thread.sleep(4000);
        }
        return param;
    }
}
