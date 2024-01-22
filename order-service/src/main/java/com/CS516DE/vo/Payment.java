package com.CS516DE.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    private Long id;

    private PaymentType paymentType;
    private String fullName;
    private String cardNumber;
    private String ccv;
    private String expiryDate;
    private Address address;
}
