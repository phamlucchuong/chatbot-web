
package com.example.bigfood.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Table(name = "diseases")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Disease {
    @Id
    @Column(columnDefinition = "VARCHAR(10)", nullable = false)
    String id;
    String name;
    String description;
    String symptoms;
    String causes;
    String preventions;
}
