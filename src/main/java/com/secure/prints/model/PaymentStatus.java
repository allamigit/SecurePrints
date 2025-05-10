package com.secure.prints.model;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Generated
public enum PaymentStatus {

    Pending(201),
    Processed(202),
    Cancelled(203),
    Refunded(204);

    private final int paymentStatusCode;

    public static int getPaymentStatusCode(String name) {
        for(PaymentStatus pmt : PaymentStatus.values()) {
            if(pmt.name().equals(name)) {
                return pmt.getPaymentStatusCode();
            }
        }
        return 0;
    }

    public static String getPaymentStatusName(int code) {
        for(PaymentStatus pmt : PaymentStatus.values()) {
            if(pmt.getPaymentStatusCode() == code) {
                return pmt.name();
            }
        }
        return null;
    }

}
