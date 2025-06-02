package com.rasit.brokage.core.data.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "assets")
@Data
public class AssetEntity implements Serializable {

    private static final long serialVersionUID = -9175226725747386054L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "asset_id", nullable = false, unique = true)
    private UUID assetId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "asset_name")
    private String assetName;

    @Column(name = "size")
    private BigDecimal size;

    @Column(name = "usable_size")
    private BigDecimal usableSize;
}
