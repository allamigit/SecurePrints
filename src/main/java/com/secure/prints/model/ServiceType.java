package com.secure.prints.model;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Generated
public enum ServiceType {

    BCI("BCI Background Check", BigDecimal.valueOf(38), BigDecimal.valueOf(-22)),
    FBI("FBI Background Check", BigDecimal.valueOf(40), BigDecimal.valueOf(-24)),
    BCI_FBI("BCI and FBI Background Check", BigDecimal.valueOf(68), BigDecimal.valueOf(-46));

    private final String serviceName;
    private final BigDecimal serviceFee;
    private final BigDecimal bciFee;

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

    public static BigDecimal getServiceFee(String serviceCode) {
        for(ServiceType serviceType : ServiceType.values()) {
            if(serviceType.name().equals(serviceCode)) {
                return serviceType.getServiceFee();
            }
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal getBciFee(String serviceCode) {
        for(ServiceType serviceType : ServiceType.values()) {
            if(serviceType.name().equals(serviceCode)) {
                return serviceType.getBciFee();
            }
        }
        return BigDecimal.ZERO;
    }

}
