package com.wakfoverlay.infrastructure;

import com.wakfoverlay.domain.fight.model.Shields;
import com.wakfoverlay.domain.fight.port.secondary.ShieldsRepository;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryShieldsRepository implements ShieldsRepository {
    private final Map<ShieldsKey, LocalTime> shieldsMap = new HashMap<>();

    @Override
    public void addShields(Shields shields) {
        ShieldsKey key = new ShieldsKey(shields.targetName(), shields.amount());
        shieldsMap.put(key, shields.timestamp());
    }

    @Override
    public Optional<Shields> find(Shields shields) {
        ShieldsKey key = new ShieldsKey(shields.targetName(), shields.amount());
        LocalTime timestamp = shieldsMap.get(key);
        if (timestamp != null) {
            return Optional.of(new Shields(timestamp, shields.targetName(), shields.amount()));
        }
        return Optional.empty();
    }

    @Override
    public void resetShields() {
        shieldsMap.clear();
    }
}
