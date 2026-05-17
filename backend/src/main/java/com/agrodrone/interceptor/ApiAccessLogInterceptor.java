package com.agrodrone.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiAccessLogInterceptor implements HandlerInterceptor {
    private static final String START_TIME_ATTRIBUTE = "apiStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception exception) {
        Object startTime = request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime instanceof Long start) {
            long cost = System.currentTimeMillis() - start;
            System.out.printf("[SpringMVC] %s %s -> %d, cost=%dms%n",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    cost);
        }
    }
}
