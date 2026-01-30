package com.secure.prints.model;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Generated
public enum PaymentMethod {

    Card(301),
    Cash(302),
    Zelle(303),
    Check(304),
    DD(305);

    private final int paymentMethodCode;

    public static int getPaymentMethodCode(String name) {
        for(PaymentMethod pmt : PaymentMethod.values()) {
            if(pmt.name().equals(name)) {
                return pmt.getPaymentMethodCode();
            }
        }
        return 0;
    }

    public static String getPaymentMethodName(int code) {
        for(PaymentMethod pmt : PaymentMethod.values()) {
            if(pmt.getPaymentMethodCode() == code) {
                return pmt.name();
            }
        }
        return null;
    }

}
