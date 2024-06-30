package com.umc.gusto.domain.myCategory.entity;

import com.umc.gusto.domain.store.entity.Store;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.global.common.BaseEntity;
import com.umc.gusto.global.common.BaseTime;
import com.umc.gusto.global.common.PublishStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
public class MyCategory extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long myCategoryId;

    @Column(nullable = false, length = 10)
    private String myCategoryName;

    @Column(nullable = false)
    private Integer myCategoryIcon;

    @Column(length = 20)
    private String myCategoryScript;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "publishCategory",nullable = false, length = 10)
    private PublishStatus publishCategory = PublishStatus.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @OneToMany(mappedBy = "myCategory", cascade = CascadeType.ALL)
    private final List<Pin> pinList = new ArrayList<>();

    public void updateMyCategoryName(String myCategoryName) {
        this.myCategoryName = myCategoryName;
    }

    public void updateMyCategoryIcon(Integer myCategoryIcon) {
        this.myCategoryIcon = myCategoryIcon;
    }

    public void updateMyCategoryScript(String myCategoryScript) {
        this.myCategoryScript = myCategoryScript;
    }

    // 데모데이 이후 진행
    public void updatePublishCategory(PublishStatus publishCategory) {
        this.publishCategory = publishCategory;
    }
    public void updateStatus(BaseEntity.Status status) {this.status = status;}

}
