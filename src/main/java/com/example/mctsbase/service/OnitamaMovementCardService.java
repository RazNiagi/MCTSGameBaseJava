package com.example.mctsbase.service;

import com.example.mctsbase.enums.OnitamaExpansion;
import com.example.mctsbase.model.OnitamaMovementCard;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OnitamaMovementCardService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private List<OnitamaMovementCard> onitamaMovementCards;

    @PostConstruct
    public void init() {
        try {
            File onitamaCardsResourceFile = new ClassPathResource("onitamaCards.json").getFile();
            onitamaMovementCards = objectMapper.readValue(onitamaCardsResourceFile, new TypeReference<>() {});
            log.info("Loaded Onitama movement cards successfully");
        } catch (IOException e) {
            log.error("Failed to load Onitama cards.", e);
            throw new RuntimeException(e);
        }
    }

    public List<OnitamaMovementCard> getOnitamaMovementCardsFromExpansions(List<OnitamaExpansion> expansions) {
        List<String> expansionNames = expansions.stream().map(OnitamaExpansion::name).toList();
        return new ArrayList<>(onitamaMovementCards.stream().filter(card -> expansionNames.contains(card.getExpansion())).toList());
    }

    public List<OnitamaMovementCard> getOnitamaMovementCardsFromExpansionStrings(List<String> expansions) {
        return new ArrayList<>(onitamaMovementCards.stream().filter(card -> expansions.contains(card.getExpansion())).toList());
    }

    public List<OnitamaMovementCard> getFilteredCardsFromNames(List<String> names) {
        return new ArrayList<>(onitamaMovementCards.stream().filter(card -> names.contains(card.getName())).toList());
    }
}
