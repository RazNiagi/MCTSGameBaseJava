package com.example.mctsbase.model;

import com.example.mctsbase.enums.OnitamaMovementBias;
import lombok.*;


@EqualsAndHashCode(callSuper = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OnitamaMovementCard extends OnitamaSimpleMovementCard {
    private char stampColor;
    private String quote;
    private OnitamaMovementBias movementBias;
}
