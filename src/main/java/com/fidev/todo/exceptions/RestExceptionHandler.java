package com.fidev.todo.exceptions;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(TodoException.class)
    public HttpEntity<Object> handleTodoException(TodoException e) {
        return new ResponseEntity<>(e.toResponse(), e.getStatus());
    }
    
}
