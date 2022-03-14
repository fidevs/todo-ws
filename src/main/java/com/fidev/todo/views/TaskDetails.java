package com.fidev.todo.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class TaskDetails {
    private String id;
    private String desc;
    private float duration;
    private String finalDate;
    private float delay;
    private String status;
}
