package in.neuw.spring.web.controller;

import in.neuw.spring.annotation.ApiResponse;
import in.neuw.spring.service.MockService;
import in.neuw.spring.web.model.Mock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/v1/api/mock")
public class MockController {

    private final MockService mockService;

    public MockController(MockService mockService) {
        this.mockService = mockService;
    }

    @ApiResponse
    @GetMapping
    public Mock getMock() {
        log.info("get Mock");
        return mockService.getMock();
    }

    @ApiResponse
    @GetMapping("/mono")
    public Mono<Mock> getMockMono() {
        log.info("get Mock wrapped in Mono");
        return mockService.getMockAsMono();
    }

    @GetMapping("/not-wrapped")
    public Mock getMockNotWrapped() {
        return mockService.getMock();
    }

    @GetMapping("/not-wrapped/mono")
    public Mono<Mock> getMockMonoNotWrapped() {
        return mockService.getMockAsMono();
    }

}
