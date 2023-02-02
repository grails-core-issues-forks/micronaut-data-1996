package com.unicorn.store.service;

import com.unicorn.store.data.UnicornRepository;
import com.unicorn.store.exceptions.ResourceNotFoundException;
import com.unicorn.store.model.Unicorn;
import com.unicorn.store.model.UnicornEventType;
import jakarta.inject.Singleton;

@Singleton
public class UnicornService {
    private final UnicornRepository unicornRepository;
    private final IdGenerator idGenerator;

    public UnicornService(UnicornRepository unicornRepository,
                          IdGenerator idGenerator) {
        this.unicornRepository = unicornRepository;
        this.idGenerator = idGenerator;
    }

    public Unicorn createUnicorn(Unicorn unicorn) {
        if (unicorn.getId() == null) {
            unicorn.setId(idGenerator.generate());
        }
        var savedUnicorn = unicornRepository.save(unicorn);
        return savedUnicorn;
    }

    public Unicorn updateUnicorn(Unicorn unicorn, String unicornId) {
        unicorn.setId(unicornId);
        var savedUnicorn = unicornRepository.save(unicorn);
        return savedUnicorn;
    }

    public Unicorn getUnicorn(String unicornId) {
        var unicorn = unicornRepository.findById(unicornId);
        return unicorn.orElseThrow(ResourceNotFoundException::new);
    }

    public void deleteUnicorn(String unicornId) {
        unicornRepository.deleteById(unicornId);
    }
}
