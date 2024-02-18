package com.umc.gusto.domain.myCategory.service;

import com.umc.gusto.domain.myCategory.entity.MyCategory;
import com.umc.gusto.domain.myCategory.entity.Pin;
import com.umc.gusto.domain.myCategory.model.request.CreatePinRequest;
import com.umc.gusto.domain.myCategory.model.response.CreatePinResponse;
import com.umc.gusto.domain.myCategory.repository.MyCategoryRepository;
import com.umc.gusto.domain.myCategory.repository.PinRepository;
import com.umc.gusto.domain.store.entity.Store;
import com.umc.gusto.domain.store.repository.StoreRepository;
import com.umc.gusto.domain.user.entity.User;
import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PinServiceImpl implements PinService{
    private final PinRepository pinRepository;
    private final MyCategoryRepository myCategoryRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public CreatePinResponse createPin(User user, Long myCategoryId, CreatePinRequest createPin) {
        MyCategory myCategory = myCategoryRepository.findByUserAndMyCategoryId(user, myCategoryId)
                .orElseThrow(() -> new GeneralException(Code.MY_CATEGORY_NOT_FOUND));
        Store store = storeRepository.findById(createPin.getStoreId())
                        .orElseThrow(() -> new GeneralException(Code.STORE_NOT_FOUND));

        Pin pin = Pin.builder()
                .user(user)
                .myCategory(myCategory)
                .store(store)
                .build();

        Pin savedPin = pinRepository.save(pin);

        return CreatePinResponse.builder()
                .pinId(savedPin.getPinId())
                .build();
    }

    @Transactional
    public void deletePin(User user, List<Long> pinIds) {
        for (Long pinId : pinIds) {
            Pin pin = pinRepository.findByUserAndPinId(user, pinId)
                    .orElseThrow(() -> new GeneralException(Code.PIN_NOT_FOUND));

            pinRepository.delete(pin);

        }

    }

}
