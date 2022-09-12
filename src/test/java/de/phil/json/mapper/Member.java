package de.phil.json.mapper;

import java.time.Instant;

@SuppressWarnings("unused")
interface Member {
    String getName();

    Instant getMemberSince();
}
