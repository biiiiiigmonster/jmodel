package com.github.biiiiiigmonster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.biiiiiigmonster.entity.Taggable;
import com.github.biiiiiigmonster.mapper.TaggableMapper;
import com.github.biiiiiigmonster.service.TaggableService;
import org.springframework.stereotype.Service;

@Service
public class TaggableServiceImpl extends ServiceImpl<TaggableMapper, Taggable> implements TaggableService {
} 