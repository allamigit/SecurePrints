package com.secure.prints.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DateRange implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private OffsetDateTime startTimestamp;
    private OffsetDateTime endTimestamp;

}
