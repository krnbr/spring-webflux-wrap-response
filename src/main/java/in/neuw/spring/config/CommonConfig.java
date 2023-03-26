package in.neuw.spring.config;

import in.neuw.spring.web.handler.CustomResponseBodyResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;

@Configuration
public class CommonConfig {

    private final ServerCodecConfigurer serverCodecConfigurer;

    private final RequestedContentTypeResolver requestedContentTypeResolver;

    public CommonConfig(ServerCodecConfigurer serverCodecConfigurer, RequestedContentTypeResolver requestedContentTypeResolver) {
        this.serverCodecConfigurer = serverCodecConfigurer;
        this.requestedContentTypeResolver = requestedContentTypeResolver;
    }

    @Bean
    public CustomResponseBodyResultHandler customResponseBodyResultHandler() {
        return new CustomResponseBodyResultHandler(
                serverCodecConfigurer.getWriters(), requestedContentTypeResolver
        );
    }

}
