package me.test.resilience4j.event;

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

    private String fallback(String param, Exception ex) {
        log.info("fallback! your request is " + param);
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
