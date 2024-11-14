package com.example.mctsbase.model;

import com.example.mctsbase.enums.OnitamaMovementBias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class OnitamaMovementCard extends OnitamaSimpleMovementCard {
    private char stampColor;
    private String quote;
    private OnitamaMovementBias movementBias;
}
