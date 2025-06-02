package com.rasit.brokage.rest.resource.order;

import com.rasit.brokage.rest.resource.BaseListRestResponseModel;
import com.rasit.brokage.rest.resource.BaseRestModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrdersRestResponseListModel extends BaseListRestResponseModel implements BaseRestModel {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Orders information.")
    private List<OrderRestResponseModel> orderRestResponseModels;

    public OrdersRestResponseListModel() {
        orderRestResponseModels = new ArrayList<>();
    }

    public OrdersRestResponseListModel(List<OrderRestResponseModel> orderRestResponseModels, int totalPages, Integer pageNumber, Integer pageSize, long totalElements) {
        super(totalPages, pageNumber, pageSize, totalElements);
        this.orderRestResponseModels = orderRestResponseModels;
    }

    public OrdersRestResponseListModel(List<OrderRestResponseModel> matchedOrdersList) {
        this.orderRestResponseModels = matchedOrdersList;
    }
}