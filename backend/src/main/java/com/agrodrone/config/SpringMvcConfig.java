package com.agrodrone.config;

import com.agrodrone.interceptor.ApiAccessLogInterceptor;
import com.agrodrone.interceptor.AuthInterceptor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload-path:uploads/pest_detection}")
    private String configuredUploadPath;

    private String resourceLocation;

    private final ApiAccessLogInterceptor apiAccessLogInterceptor;
    private final AuthInterceptor authInterceptor;

    public SpringMvcConfig(ApiAccessLogInterceptor apiAccessLogInterceptor, AuthInterceptor authInterceptor) {
        this.apiAccessLogInterceptor = apiAccessLogInterceptor;
        this.authInterceptor = authInterceptor;
    }

    @PostConstruct
    public void init() {
        try {
            URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
            Path classRoot = Paths.get(location.toURI());
            Path current = classRoot;
            Path projectRoot = null;
            while (current != null) {
                if (Files.exists(current.resolve("backend"))) {
                    projectRoot = current;
                    break;
                }
                if (current.getFileName() != null && "backend".equals(current.getFileName().toString())) {
                    projectRoot = current.getParent();
                    break;
                }
                current = current.getParent();
            }
            Path resolved;
            if (projectRoot != null) {
                resolved = projectRoot.resolve(configuredUploadPath).normalize().toAbsolutePath();
            } else {
                resolved = Paths.get(configuredUploadPath).toAbsolutePath().normalize();
            }
            this.resourceLocation = "file:" + resolved.toString().replace("\\", "/") + "/";
        } catch (Exception e) {
            this.resourceLocation = "file:" + configuredUploadPath + "/";
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(resourceLocation);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/uploads/**");
        registry.addInterceptor(apiAccessLogInterceptor)
                .addPathPatterns("/api/**");
    }
}
