package me.test.resilience4j.fallback;

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
public class CircuitBreakerFallbackApplication {

    public static void main(String[] args) {
        SpringApplication.run(CircuitBreakerFallbackApplication.class, args);
    }

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
