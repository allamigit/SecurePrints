package com.secure_prints.model;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Generated
public enum ServiceType {

    BCI("BCI Background Check", BigDecimal.valueOf(38)),
    FBI("FBI Background Check", BigDecimal.valueOf(48)),
    BCI_FBI("BCI and FBI Background Check", BigDecimal.valueOf(68));

    private final String serviceName;
    private final BigDecimal servicePrice;

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

    public static BigDecimal getServicePrice(String serviceCode) {
        for(ServiceType serviceType : ServiceType.values()) {
            if(serviceType.name().equals(serviceCode)) {
                return serviceType.getServicePrice();
            }
        }
        return BigDecimal.ZERO;
    }

}
