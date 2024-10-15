package com.umc.gusto.domain.store.entity;

import com.umc.gusto.global.common.BaseTime;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
public class Store extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long storeId;

    @Column(columnDefinition = "VARCHAR(40)")
    private String storeName;

    @Column(columnDefinition = "DOUBLE(17,14)")
    private Double longitude;

    @Column(columnDefinition = "DOUBLE(17,15)")
    private Double latitude;

    @Column(columnDefinition = "VARCHAR(20)")
    private String categoryString;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stateCode", nullable = false)
    private State state;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cityCode", nullable = false)
    private City city;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "townCode", nullable = false)
    private Town town;

    @Column(nullable = false, columnDefinition = "VARCHAR(60)")
    private String address;

    @Column(columnDefinition = "VARCHAR(60)")
    private String oldAddress;

    @Column(columnDefinition = "VARCHAR(20)")
    private String contact;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private StoreStatus storeStatus = StoreStatus.ACTIVE;

    public enum StoreStatus {
        ACTIVE, INACTIVE, CLOSED
    }
}

