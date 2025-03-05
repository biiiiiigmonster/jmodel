package com.github.biiiiiigmonster;

import com.github.biiiiiigmonster.mapper.AddressMapper;
import com.github.biiiiiigmonster.mapper.CommentMapper;
import com.github.biiiiiigmonster.mapper.ImageMapper;
import com.github.biiiiiigmonster.mapper.PostMapper;
import com.github.biiiiiigmonster.mapper.ProfileMapper;
import com.github.biiiiiigmonster.mapper.RoleMapper;
import com.github.biiiiiigmonster.mapper.TagMapper;
import com.github.biiiiiigmonster.mapper.TaggableMapper;
import com.github.biiiiiigmonster.mapper.UserMapper;
import com.github.biiiiiigmonster.mapper.UserRoleMapper;
import com.github.biiiiiigmonster.mapper.VideoMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BaseTest {
    @Autowired
    protected UserMapper userMapper;
    @Autowired
    protected PostMapper postMapper;
    @Autowired
    protected VideoMapper videoMapper;
    @Autowired
    protected RoleMapper roleMapper;
    @Autowired
    protected UserRoleMapper userRoleMapper;
    @Autowired
    protected ProfileMapper profileMapper;
    @Autowired
    protected AddressMapper addressMapper;
    @Autowired
    protected CommentMapper commentMapper;
    @Autowired
    protected ImageMapper imageMapper;
    @Autowired
    protected TagMapper tagMapper;
    @Autowired
    protected TaggableMapper taggableMapper;
}
