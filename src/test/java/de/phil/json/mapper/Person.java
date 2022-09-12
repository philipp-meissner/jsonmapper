package de.phil.json.mapper;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
@FieldNameConstants
class Person implements Member {
    private String name;
    private Instant memberSince;
}
