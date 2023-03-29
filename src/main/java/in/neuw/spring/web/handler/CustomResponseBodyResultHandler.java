package in.neuw.spring.web.handler;

import in.neuw.spring.annotation.ApiResponse;
import in.neuw.spring.web.model.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class CustomResponseBodyResultHandler extends ResponseBodyResultHandler {

    public CustomResponseBodyResultHandler(List<HttpMessageWriter<?>> writers, RequestedContentTypeResolver resolver) {
        super(writers, resolver);
    }

    @Override
    public boolean supports(HandlerResult result) {
        var className = result.getReturnTypeSource().getDeclaringClass().getName();
        var methodName = result.getReturnTypeSource().getMethod().getName();
        var classAnnotations = result.getReturnTypeSource().getDeclaringClass().getAnnotations();
        var methodAnnotations = result.getReturnTypeSource().getMethodAnnotations();
        var annotations = result.getReturnTypeSource().getDeclaringClass().getAnnotations();

        if (Arrays.stream(classAnnotations).anyMatch(a -> a.annotationType() == ApiResponse.class)) {
            log.info("{} is marked with ApiResponse annotation", className);
            return true;
        } else if (Arrays.stream(methodAnnotations).anyMatch(a -> a.annotationType() == ApiResponse.class)) {
            log.info("{} inside {} is marked with ApiResponse annotation", methodName, className);
            return true;
        }

        return false;
    }

    @Override
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
        ServiceResponse s = new ServiceResponse();
        s.setMethod(exchange.getRequest().getMethod().name());
        s.setStatus(exchange.getResponse().getStatusCode().value());
        s.setCorrelationId(exchange.getAttribute("correlation-id"));
        var adapter = getAdapter(result);
        // modify the result as you want
        if (adapter != null) { // if the response was wrapped inside Mono?
            Mono<ServiceResponse> body = ((Mono<Object>) result.getReturnValue()).map(o -> {
                s.setData(o);
                return s;
            });
            return writeBody(body, result.getReturnTypeSource().nested(), exchange);
        } else { // if the response was not wrapped inside Mono
            s.setData(result.getReturnValue());
            Mono<ServiceResponse> body = Mono.just(s);
            return writeBody(body, result.getReturnTypeSource().nested(), exchange);
        }
    }
}