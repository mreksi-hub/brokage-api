package com.rasit.brokage.rest.resource.order;

import com.rasit.brokage.utility.BrokageUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Order information.")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRestResponseModel extends OrderRestRequestModel {

    @Schema(description = "Unique identifier of the order.")
    private String identifier;

    @Schema(description = "Status of the order.")
    private String status;

    @Schema(description = "The date and time of the creation.", pattern = BrokageUtil.DATE_TIME_PATTERN)
    @JsonProperty("createDate")
    private String createDate;
}
