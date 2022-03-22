package com.fidev.todo.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
public class TaskDTO {
    private String desc;
    private float duration;
}
