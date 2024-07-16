package com.zhukew.circle.server.config;

import com.zhukew.circle.server.sensitive.WordContext;
import com.zhukew.circle.server.sensitive.WordFilter;
import com.zhukew.circle.server.service.SensitiveWordsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SensitiveConfig {

    @Bean
    public WordContext wordContext(SensitiveWordsService service) {
        return new WordContext(true, service);
    }

    @Bean
    public WordFilter wordFilter(WordContext wordContext) {
        return new WordFilter(wordContext);
    }

}
