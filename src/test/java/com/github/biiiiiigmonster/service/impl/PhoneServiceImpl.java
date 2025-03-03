package com.github.biiiiiigmonster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.biiiiiigmonster.entity.Phone;
import com.github.biiiiiigmonster.entity.User;
import com.github.biiiiiigmonster.mapper.PhoneMapper;
import com.github.biiiiiigmonster.mapper.UserMapper;
import com.github.biiiiiigmonster.service.PhoneService;
import com.github.biiiiiigmonster.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class PhoneServiceImpl extends ServiceImpl<PhoneMapper, Phone> implements PhoneService {
}