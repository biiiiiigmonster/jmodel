package io.github.biiiiiigmonster;

import io.github.biiiiiigmonster.mapper.AddressMapper;
import io.github.biiiiiigmonster.mapper.CommentMapper;
import io.github.biiiiiigmonster.mapper.ImageMapper;
import io.github.biiiiiigmonster.mapper.PhoneMapper;
import io.github.biiiiiigmonster.mapper.PostMapper;
import io.github.biiiiiigmonster.mapper.ProfileMapper;
import io.github.biiiiiigmonster.mapper.RoleMapper;
import io.github.biiiiiigmonster.mapper.TagMapper;
import io.github.biiiiiigmonster.mapper.TaggableMapper;
import io.github.biiiiiigmonster.mapper.UserMapper;
import io.github.biiiiiigmonster.mapper.UserRoleMapper;
import io.github.biiiiiigmonster.mapper.VideoMapper;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Ignore
public class BaseTest {
    @Autowired
    protected UserMapper userMapper;
    @Autowired
    protected PostMapper postMapper;
    @Autowired
    protected PhoneMapper phoneMapper;
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