package com.umc.gusto.global;

import com.umc.gusto.domain.group.service.GroupService;
import com.umc.gusto.domain.myCategory.service.MyCategoryService;
import com.umc.gusto.domain.review.service.ReviewService;
import com.umc.gusto.domain.route.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@EnableScheduling
@Configuration
@Component
@RequiredArgsConstructor
public class DeleteSchedulingConfig {
// 삭제 시 상호 참조 주의!
    private final RouteService routeService;
    private final GroupService groupService;
    private final MyCategoryService myCategoryService;
    private final ReviewService reviewService;

    // 일정 시간마다 실행
    @Transactional
    @Async
    //@Scheduled(fixedRate = 60000)
    @Scheduled(cron = "0 0 0 1 * ?") // 한 달 주기
    public void autoDelete() {
        routeService.hardDeleteAllSoftDeleted();
        groupService.hardDeleteAllSoftDeleted();
        myCategoryService.hardDeleteAllSoftDeleted();
        reviewService.hardDeleteAllSoftDeleted();

    }

}
