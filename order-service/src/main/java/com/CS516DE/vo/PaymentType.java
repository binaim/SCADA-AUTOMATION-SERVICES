package com.CS516DE.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentType {

    private Long id;

    private PType name;

    public PaymentType(PType name) {
        this.name = name;
    }

}
