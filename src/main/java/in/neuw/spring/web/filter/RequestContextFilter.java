package in.neuw.spring.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Component
public class RequestContextFilter implements WebFilter {

    final List<PathPattern> pathPatternList;

    public RequestContextFilter() {
        PathPattern pathPatternActuator = new PathPatternParser().parse("/actuator/**");
        pathPatternList = new ArrayList();
        pathPatternList.add(pathPatternActuator);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var startTime = System.currentTimeMillis();
        String correlationId;
        if (!StringUtils.hasText(exchange.getRequest().getHeaders().getFirst("x-correlation-id"))) {
            correlationId = UUID.randomUUID().toString();
        } else {
            correlationId = exchange.getRequest().getHeaders().getFirst("x-correlation-id");
        }
        RequestPath path = exchange.getRequest().getPath();
        return chain.filter(exchange).contextWrite((context) -> {
            if (pathPatternList.stream().anyMatch(pathPattern -> pathPattern.matches(path.pathWithinApplication()))) {
                return context;
            }
            // MDC won't work as expected in the reactive world, doing a customization is dirty
            MDC.put("correlation-id", correlationId);
            log.info("{}: url = {} path, from client IP {}, setting the correlation-id to the request, correlation-id={}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getPath(),
                    exchange.getRequest().getHeaders().containsKey("X-Forwarded-For") ? exchange.getRequest().getHeaders().getFirst("X-Forwarded-For") : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress(),
                    correlationId);
            Map<String, String> map = new HashMap();
            map.put("correlation-id", correlationId);
            context = context.put("request-context", map);
            exchange.getAttributes().put("correlation-id", correlationId);
            exchange.getResponse().beforeCommit(() -> {
                exchange.getResponse().getHeaders().add("X-correlation-id", correlationId);
                exchange.getResponse().getHeaders().add("X-trace-remote", exchange.getRequest().getHeaders().containsKey("X-Forwarded-For") ? exchange.getRequest().getHeaders().getFirst("X-Forwarded-For") : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
                exchange.getResponse().getHeaders().setDate(ZonedDateTime.ofInstant(new Date().toInstant(), ZoneId.of("UTC")));
                long totalTime = System.currentTimeMillis() - startTime;
                exchange.getResponse().getHeaders().add("X-trace-time", Long.toString(totalTime) + "ms");
                return Mono.empty();
            });
            return context;
        }).doFinally(signalType -> {
            if (!pathPatternList.stream().anyMatch(pathPattern -> pathPattern.matches(path.pathWithinApplication()))) {
                long totalTime = System.currentTimeMillis() - startTime;
                exchange.getAttributes().put("totalTime", totalTime);
                log.info("{}: url = {}, from client IP {}, processed with signalType={} with correlation-id={} in {} ms",
                        exchange.getRequest().getMethod(),
                        exchange.getRequest().getURI().getPath(),
                        exchange.getRequest().getHeaders().containsKey("X-Forwarded-For") ? exchange.getRequest().getHeaders().getFirst("X-Forwarded-For") : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress(),
                        signalType,
                        correlationId,
                        totalTime);
            }
        });
    }
}
