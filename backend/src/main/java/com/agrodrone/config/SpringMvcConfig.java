package com.agrodrone.config;

import com.agrodrone.interceptor.ApiAccessLogInterceptor;
import com.agrodrone.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {
    private final ApiAccessLogInterceptor apiAccessLogInterceptor;
    private final AuthInterceptor authInterceptor;

    public SpringMvcConfig(ApiAccessLogInterceptor apiAccessLogInterceptor, AuthInterceptor authInterceptor) {
        this.apiAccessLogInterceptor = apiAccessLogInterceptor;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
        registry.addInterceptor(apiAccessLogInterceptor)
                .addPathPatterns("/api/**");
    }
}
