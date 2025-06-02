package com.rasit.brokage.rest.resource;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * rest error message class
 */
@Data
@AllArgsConstructor
public class SubErrorResponseModel implements Serializable {
    private static final long serialVersionUID = -5380759395093471872L;

    private String subText;
}