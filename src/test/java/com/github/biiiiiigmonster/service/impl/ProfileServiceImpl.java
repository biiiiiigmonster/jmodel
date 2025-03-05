package com.github.biiiiiigmonster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.biiiiiigmonster.entity.Profile;
import com.github.biiiiiigmonster.mapper.ProfileMapper;
import com.github.biiiiiigmonster.service.ProfileService;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl extends ServiceImpl<ProfileMapper, Profile> implements ProfileService {
} 