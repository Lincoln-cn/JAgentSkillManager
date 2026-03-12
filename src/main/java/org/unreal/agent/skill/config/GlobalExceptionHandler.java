package org.unreal.agent.skill.config;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.unreal.agent.skill.core.exception.SkillException;
import org.unreal.agent.skill.core.exception.SkillExecutionException;
import org.unreal.agent.skill.core.exception.SkillValidationException;

import java.util.Map;

/**
 * Global exception handler for the Agent Skill framework.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(SkillValidationException.class)
    public ResponseEntity<Map<String, Object>> handleSkillValidationException(SkillValidationException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Skill Validation Failed",
                    "message", ex.getMessage()
                ));
    }
    
    @ExceptionHandler(SkillExecutionException.class)
    public ResponseEntity<Map<String, Object>> handleSkillExecutionException(SkillExecutionException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Skill Execution Failed",
                    "message", ex.getMessage()
                ));
    }
    
    @ExceptionHandler(SkillException.class)
    public ResponseEntity<Map<String, Object>> handleSkillException(SkillException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Skill Operation Failed",
                    "message", ex.getMessage()
                ));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Internal Server Error",
                    "message", "An unexpected error occurred: " + ex.getMessage()
                ));
    }
}