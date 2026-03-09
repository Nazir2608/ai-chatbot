package com.aichatbot.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global Exception Handler
 * 
 * Catches errors and displays user-friendly messages in UI
 * instead of stack traces.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("Unhandled exception", e);
        model.addAttribute("error", "An error occurred: " + e.getMessage());
        return "error"; // templates/error.html
    }
}
