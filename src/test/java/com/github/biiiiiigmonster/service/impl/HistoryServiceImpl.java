package com.github.biiiiiigmonster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.biiiiiigmonster.entity.History;
import com.github.biiiiiigmonster.mapper.HistoryMapper;
import com.github.biiiiiigmonster.service.HistoryService;
import org.springframework.stereotype.Service;

@Service
public class HistoryServiceImpl extends ServiceImpl<HistoryMapper, History> implements HistoryService {
}