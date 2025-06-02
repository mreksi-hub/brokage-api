package com.rasit.brokage.rest.resource.asset;

import com.rasit.brokage.rest.resource.BaseListRestResponseModel;
import com.rasit.brokage.rest.resource.BaseRestModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssetsRestResponseListModel extends BaseListRestResponseModel implements BaseRestModel {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Orders information.")
    private List<AssetRestResponseModel> assetRestResponseModels;

    public AssetsRestResponseListModel() {
        assetRestResponseModels = new ArrayList<>();
    }

    public AssetsRestResponseListModel(List<AssetRestResponseModel> assetRestResponseModels, int totalPages, Integer pageNumber, Integer pageSize, long totalElements) {
        super(totalPages, pageNumber, pageSize, totalElements);
        this.assetRestResponseModels = assetRestResponseModels;
    }
}