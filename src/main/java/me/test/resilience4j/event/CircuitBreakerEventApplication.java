package me.test.resilience4j.event;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class CircuitBreakerEventApplication {

    public static void main(String[] args) {
        SpringApplication.run(CircuitBreakerEventApplication.class, args);
    }

    // CircuitBreaker 활용
    // 1. 로그 기록: CircuitBreaker 에서 발생하는 event 로그들을 수집해서 서비스를 모니터링
    // 예: 동일한 web 어플리케이션이 여러 서버에 떠있는 경우 로그를 수집하여 한곳에서 확인

    // 2. 어플리케이션이 여러 서버에 떠있는 경우 StateTransition 에서 발생한 event 를 다른 서버에 전달하여 상태 동기화
    // 예: 3번 서버에서 DB 에 보내는 요청이 CircuitBreaker 설정한 실패율 임계치를 초과한 경우 상태가 OPEN 으로 바뀐 경우
    // 이 경우 보통 DB 자체 문제인 경우가 많기 때문에 다른 1번, 2번 서버에도 CircuitBreaker 의 OPEN 상태를 전달
    // 만약 특정 서버만 OPEN 을 유지하는 경우 사용자들이 서로 다른 화면을 볼 수 있기 때문임
    // 트래픽이 너무 급격하게 올라갈 수 있기 때문에 HALF -> CLOSE 로 상태로의 변경은 전파하지 않는 것이 좋음 (DB 가 회복되면 서서히 자동으로 CLOSE 상태로 변경됨)
    @Bean
    public RegistryEventConsumer<CircuitBreaker> myRegistryEventConsumer() {
        return new RegistryEventConsumer<CircuitBreaker>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                log.info("RegistryEventConsumer.onEntryAddedEvent");

                CircuitBreaker.EventPublisher eventPublisher = entryAddedEvent.getAddedEntry().getEventPublisher();

                // 아래의 모든 event 에 대해 핸들링 되는 이벤트 핸들러
                // 이것만 등록하면 모든 이벤트 핸들링 되지만 특정 이벤트만 핸들링 하기 위해 다른 핸들러 사용
                eventPublisher.onEvent(event -> log.info("onEvent {}", event));

                eventPublisher.onSuccess(event -> log.info("onSuccess {}", event));
                // CircuitBreaker 상태가 OPEN 되어서 요청이 차단되었을때 실행되는 이벤트 핸들러
                eventPublisher.onCallNotPermitted(event -> log.info("onCallNotPermitted {}", event));
                // CircuitBreaker 에서 RecordException 이 발생했을때 실행되는 이벤트 핸들러
                eventPublisher.onError(event -> log.info("onError {}", event));
                // CircuitBreaker 에서 IgnoredException 이 발생했을때 실행되는 이벤트 핸들러
                eventPublisher.onIgnoredError(event -> log.info("onIgnoredError {}", event));

                // CircuitBreaker 상태가 변경되었을때 실행되는 이벤트 핸들러
                eventPublisher.onStateTransition(event -> log.info("onStateTransition {}", event));

                // SlowCall 이 임계치에 도달했을때 실행되는 이벤트 핸들러
                eventPublisher.onSlowCallRateExceeded(event -> log.info("onSlowCallRateExceeded {}", event));
                // RecodeException 이 임계치에 도달했을때 실행되는 이벤트 핸들러
                eventPublisher.onFailureRateExceeded(event -> log.info("onFailureRateExceeded {}", event));
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
                log.info("RegistryEventConsumer.onEntryRemovedEvent");
            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
                log.info("RegistryEventConsumer.onEntryReplacedEvent");
            }
        };
    }
}
