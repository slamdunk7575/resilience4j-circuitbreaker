package me.test.resilience4j.retry;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import me.test.resilience4j.exception.RetryException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RetryService {

    private static final String SIMPLE_RETRY_CONFIG = "simpleRetryConfig";

    @Retry(name = SIMPLE_RETRY_CONFIG, fallbackMethod = "fallback")
    public String process(String param) {
        return callAnotherServer(param);
    }

    private String fallback(String param, Exception ex) {
        // retry 에 전부 실패해야 fallback() 실행
        log.info("fallback! your request is " + param);
        return "Recovered: " + ex.toString();
    }

    private String callAnotherServer(String param) {
        // retry exception 은 retry 된다
        throw new RetryException("retry exception");
        // ignore exception 은 retry 하지않고 fallback() 실행되어 바로 예외가 클라이언트에게 전달된다
        // throw new IgnoreException("ignore exception");
    }
}
