package com.rasit.brokage.rest.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseListRestResponseModel implements BaseRestModel {

    private static final long serialVersionUID = 1L;

    @Schema(description = "The number of pages returned for the request made.")
    @JsonProperty("page_count")
    private int pageCount;

    @Schema(description = "page_number is used to paginate through large result sets.", defaultValue = "0")
    @JsonProperty("page_number")
    private int pageNumber;

    @Schema(description = "The number of records returned with a single API call.", defaultValue = "10")
    @JsonProperty("page_size")
    private int pageSize;

    @Schema(description = "The total number of all the records available across pages.")
    @JsonProperty("total_records")
    private long totalRecords;

}