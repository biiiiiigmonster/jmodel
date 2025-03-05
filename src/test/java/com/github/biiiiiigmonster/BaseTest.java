package com.github.biiiiiigmonster;

import com.github.biiiiiigmonster.mapper.UserMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BaseTest {
    @Autowired
    protected UserMapper userMapper;
}
