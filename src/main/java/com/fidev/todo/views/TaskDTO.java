package com.fidev.todo.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class TaskDTO {
    private String desc;
    private float duration;
}
