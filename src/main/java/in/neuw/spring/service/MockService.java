package in.neuw.spring.service;

import in.neuw.spring.web.model.Mock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class MockService {

    public Mono<Mock> getMockAsMono() {
        return Mono.just(new Mock("Hello World", true));
    }

    public Mock getMock() {
        return new Mock("Hello World", true);
    }

}
