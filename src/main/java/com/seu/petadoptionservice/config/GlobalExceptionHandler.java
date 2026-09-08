package com.seu.petadoptionservice.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.NoSuchElementException;

/**
 * Last line of defense against Spring's Whitelabel Error Page.
 *
 * Individual controllers should still handle their own "not found" and
 * validation cases with friendly redirects (see PetController, PaymentController,
 * etc.) - this class exists to catch anything that slips through: an unexpected
 * RuntimeException, a bad path that doesn't map to a resource, or any other
 * unhandled failure. It always renders the themed error.html page instead of
 * the default whitelabel one.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNotFound(NoSuchElementException ex, Model model) {
        model.addAttribute("message", "We couldn't find what you were looking for.");
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResource(NoResourceFoundException ex, Model model) {
        model.addAttribute("message", "That page doesn't exist.");
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, HttpServletRequest request, Model model) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        model.addAttribute("message", "Something went wrong on our end. Please try again.");
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "error";
    }
}
