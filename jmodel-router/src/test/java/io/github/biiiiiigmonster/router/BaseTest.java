package io.github.biiiiiigmonster.router;

import io.github.biiiiiigmonster.router.mapper.PostMapper;
import io.github.biiiiiigmonster.router.mapper.UserMapper;
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
}
