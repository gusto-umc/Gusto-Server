package com.umc.gusto.domain.store.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode
@Entity
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long cityId;

    @Column(nullable = false, columnDefinition = "VARCHAR(15)")
    private String cityCode;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private String cityName;
}
