package com.fidev.todo.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception to return with error data
 */

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TodoException extends Throwable {
    private HttpStatus status;
    private String code;
    private String message;

    public TodoException(String message, String code, HttpStatus status) {
        this.message = message;
        this.code = code;
        this.status = status;
    }

    public Map<String, Object> toResponse() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);

        return map;
    }
}
