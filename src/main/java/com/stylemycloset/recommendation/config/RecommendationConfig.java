package com.stylemycloset.recommendation.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({HuggingFaceProperties.class})
public class RecommendationConfig {
}


