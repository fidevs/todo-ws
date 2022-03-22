package com.fidev.todo.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "task")
public class Task {

    @Id
    @Column(name = "task_id", length = 36)
    private String id;

    @Column(name = "description", nullable = false, length = 100)
    private String description;

    @Column(name = "duration", nullable = false)
    private float duration;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finalized_at")
    private Date finDate;

    @Column(name = "delay")
    private float delay;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private TaskStatus status;

    @PrePersist
    protected void prePersist() {
        id = UUID.randomUUID().toString();
    }

    public Task(String description, float duration) {
        this.description = description;
        this.duration = duration;
        this.status = TaskStatus.PENDING;
    }
    
}
