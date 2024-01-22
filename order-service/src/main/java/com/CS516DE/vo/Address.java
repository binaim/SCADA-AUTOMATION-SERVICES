package com.CS516DE.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    private Long id;

    private String street;

    private String city;

    private String state;

    private String zip;
}
