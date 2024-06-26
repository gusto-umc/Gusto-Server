package com.umc.gusto.domain.store.entity;

import com.umc.gusto.domain.myCategory.entity.MyCategory;
import com.umc.gusto.global.common.BaseTime;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

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

    @Column(columnDefinition = "VARCHAR(30)")
    private String storeName;

    @Column(columnDefinition = "DOUBLE(17,14)")
    private Double longitude;

    @Column(columnDefinition = "DOUBLE(17,15)")
    private Double latitude;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoryId")
    private Category category;

    @Column(columnDefinition = "VARCHAR(20)")
    private String categoryString;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stateId", nullable = false)
    private State state;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cityId", nullable = false)
    private City city;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "townId", nullable = false)
    private Town town;

    @Column(nullable = false, columnDefinition = "VARCHAR(60)")
    private String address;

    @Column(columnDefinition = "VARCHAR(60)")
    private String oldAddress;

    @Column(columnDefinition = "VARCHAR(20)")
    private String contact;

    private String img1;

    private String img2;

    private String img3;

    private String img4;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private StoreStatus storeStatus = StoreStatus.ACTIVE;

    public enum StoreStatus {
        ACTIVE, INACTIVE, CLOSED
    }

    public void updateImages(List<String> reviewImages) {
        // reviewImages 리스트에서 이미지를 가져와 각 필드에 할당
        if (!reviewImages.isEmpty()) {
            this.img1 = reviewImages.get(0);
        }
        if (reviewImages.size() > 1) {
            this.img2 = reviewImages.get(1);
        }
        if (reviewImages.size() > 2) {
            this.img3 = reviewImages.get(2);
        }
        if (reviewImages.size() > 3) {
            this.img4 = reviewImages.get(3);
        }
    }

}

