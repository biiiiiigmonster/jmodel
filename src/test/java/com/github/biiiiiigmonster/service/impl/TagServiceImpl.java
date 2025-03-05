package com.github.biiiiiigmonster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.biiiiiigmonster.entity.Tag;
import com.github.biiiiiigmonster.mapper.TagMapper;
import com.github.biiiiiigmonster.service.TagService;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
} 