package com.fidev.todo.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TaskDetailsDTO {
    private String id;
    private String desc;
    private float duration;
    private String finalDate;
    private float delay;
    private String status;
}
