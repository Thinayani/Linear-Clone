package com.linearclone.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "labels",
        uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Label extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 7)
    private String color;

    @Column(columnDefinition = "TEXT")
    private String description;
}
