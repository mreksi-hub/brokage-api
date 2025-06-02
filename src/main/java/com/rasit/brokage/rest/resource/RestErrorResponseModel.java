package com.rasit.brokage.rest.resource;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * rest error message class
 */
@Data
@AllArgsConstructor
public class RestErrorResponseModel implements BaseRestModel {

    private static final long serialVersionUID = 5526332760804621180L;

    private String messageId;
    private String text;
    private List<SubErrorResponseModel> subErrors;

    public RestErrorResponseModel(String text) {
        super();
        this.text = text;
    }
}