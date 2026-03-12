package org.unreal.agent.skill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuration for exception handling and error responses.
 */
@Configuration
public class ExceptionHandlingConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new GlobalExceptionHandlerInterceptor());
    }
    
    /**
     * Handler interceptor that catches exceptions and returns appropriate responses.
     */
    public static class GlobalExceptionHandlerInterceptor implements HandlerInterceptor {
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            return true;
        }
        
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            if (ex != null) {
                // Log the exception
                System.err.println("Exception occurred: " + ex.getMessage());
                ex.printStackTrace();
                
                // Set error response
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Internal Server Error\",\"message\":\"" + ex.getMessage() + "\"}");
            }
        }
    }
}