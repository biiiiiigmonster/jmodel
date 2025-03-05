package com.github.biiiiiigmonster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.biiiiiigmonster.entity.Comment;
import com.github.biiiiiigmonster.mapper.CommentMapper;
import com.github.biiiiiigmonster.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
} 