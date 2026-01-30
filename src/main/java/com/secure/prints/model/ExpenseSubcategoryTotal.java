package com.secure.prints.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSubcategoryTotal implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer subcategoryCode;
    private String subcategoryName;
    private BigDecimal subcategoryTotal;

}
