package com.rasit.brokage.core.data.entity;

import com.rasit.brokage.utility.SideType;
import com.rasit.brokage.utility.StatusType;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = -734380602981428922L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    // TODO: CustomerEntity relation
    //    @ManyToOne
    //    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    //    private CustomerEntity customer;
    @Column(name = "customer_id")
    private String customerId;

    // TODO: prefer to keep asset_id
    @Column(name = "asset_name")
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_side")
    private SideType orderSide;

    @Column(name = "size")
    private BigDecimal size;

    @Column(name = "price")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusType status;

    @CreatedDate
    @Column(name = "create_date", nullable = false)
    private ZonedDateTime createDate;
}
