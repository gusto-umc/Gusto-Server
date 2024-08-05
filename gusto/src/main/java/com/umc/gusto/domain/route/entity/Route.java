package com.umc.gusto.domain.route.entity;

import com.umc.gusto.domain.group.entity.Group;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.global.common.BaseEntity;
import com.umc.gusto.global.common.PublishStatus;
import jakarta.persistence.*;
import lombok.*;
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
public class Route extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long routeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId")
    private Group group;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private String routeName;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "publishRoute", nullable = false, length = 10)
    private PublishStatus publishRoute = PublishStatus.PUBLIC;


    public void updateStatus(BaseEntity.Status status) {this.status = status;}
    public void updateRouteName(String routeName){this.routeName = routeName;}
    public void updatePublishRoute(PublishStatus status) {
        this.publishRoute = status;
    }
}
