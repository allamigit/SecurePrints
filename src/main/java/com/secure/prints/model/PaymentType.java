package com.secure.prints.model;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Generated
public enum PaymentType {

    Fee(201),
    Other(202);

    private final int paymentTypeCode;

    public static int getPaymentTypeCode(String name) {
        for(PaymentType pmt : PaymentType.values()) {
            if(pmt.name().equals(name)) {
                return pmt.getPaymentTypeCode();
            }
        }
        return 0;
    }

    public static String getPaymentTypeName(int code) {
        for(PaymentType pmt : PaymentType.values()) {
            if(pmt.getPaymentTypeCode() == code) {
                return pmt.name();
            }
        }
        return null;
    }

}
