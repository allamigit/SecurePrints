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
public class Revenue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigDecimal totalRevenue;
    private BigDecimal bciFees;
    private BigDecimal bankFees;
    private BigDecimal grossProfit;
    private BigDecimal totalExpense;
    private BigDecimal netProfit;
    private BigDecimal totalInvoiced;
    private BigDecimal totalNotInvoiced;

}
