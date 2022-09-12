package de.phil.json.mapper;

import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;

@NoArgsConstructor
class MemberJsonMap extends HashMap<String, Object> implements JsonMap, Member {
    @Override
    public String getName() {
        return getAsString(Person.Fields.name);
    }

    @Override
    public Instant getMemberSince() {
        return Instant.parse(getAsString(Person.Fields.memberSince));
    }
}
