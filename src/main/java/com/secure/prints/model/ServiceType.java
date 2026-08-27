package com.secure.prints.model;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Generated
public enum ServiceType {

    // PROD Values
    BCI("BCI Background Check", BigDecimal.valueOf(38), BigDecimal.valueOf(22), BigDecimal.valueOf(38), BigDecimal.valueOf(22), LocalDate.parse("2025-01-01")),
    FBI("FBI Background Check", BigDecimal.valueOf(40), BigDecimal.valueOf(24), BigDecimal.valueOf(45), BigDecimal.valueOf(27), LocalDate.parse("2026-10-01")),
    BCI_FBI("BCI and FBI Background Check", BigDecimal.valueOf(68), BigDecimal.valueOf(46), BigDecimal.valueOf(72), BigDecimal.valueOf(49), LocalDate.parse("2026-10-01"));

    // DEV Values
    /*BCI("BCI Background Check", BigDecimal.valueOf(32), BigDecimal.valueOf(12)),
    FBI("FBI Background Check", BigDecimal.valueOf(34), BigDecimal.valueOf(14)),
    BCI_FBI("BCI and FBI Background Check", BigDecimal.valueOf(48), BigDecimal.valueOf(28));*/

    private final String serviceName;
    private final BigDecimal serviceFee;
    private final BigDecimal bciFee;
    private final BigDecimal newServiceFee;
    private final BigDecimal newBciFee;
    private final LocalDate effectiveDate;

    public static String getServiceCode(String serviceName) {
        for(ServiceType serviceType : ServiceType.values()) {
            if(serviceType.getServiceName().equals(serviceName)) {
                return serviceType.name();
            }
        }
        return null;
    }

    public static String getServiceName(String serviceCode) {
        for(ServiceType serviceType : ServiceType.values()) {
            if(serviceType.name().equals(serviceCode)) {
                return serviceType.getServiceName();
            }
        }
        return null;
    }

    public static BigDecimal getServiceFee(String serviceCode, LocalDate appointmentDate) {
        for(ServiceType serviceType : ServiceType.values()) {
            if(serviceType.name().equals(serviceCode) && appointmentDate.isBefore(serviceType.getEffectiveDate())) {
                return serviceType.getServiceFee();
            } else if(serviceType.name().equals(serviceCode) && !appointmentDate.isBefore(serviceType.getEffectiveDate())) {
                return serviceType.getNewServiceFee();
            }
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal getBciFee(String serviceCode, LocalDate appointmentDate) {
        for(ServiceType serviceType : ServiceType.values()) {
            if(serviceType.name().equals(serviceCode) && appointmentDate.isBefore(serviceType.getEffectiveDate())) {
                return serviceType.getBciFee();
            } else if(serviceType.name().equals(serviceCode) && !appointmentDate.isBefore(serviceType.getEffectiveDate())) {
                return serviceType.getNewBciFee();
            }
        }
        return BigDecimal.ZERO;
    }

}
