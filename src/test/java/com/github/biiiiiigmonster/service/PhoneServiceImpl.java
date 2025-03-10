package com.github.biiiiiigmonster.service;

import com.github.biiiiiigmonster.entity.Phone;
import com.github.biiiiiigmonster.relation.annotation.config.Related;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhoneServiceImpl {

    @Related(field = "userId")
    public List<Phone> mock(List<Long> userIds) {
        return userIds.stream().filter(userId -> userId < 10).map(userId -> {
            Phone phone = new Phone();
            phone.setId(userId);
            phone.setUserId(userId);
            phone.setNumber(String.format("1000%d", userId));
            return phone;
        }).collect(Collectors.toList());
    }
}
