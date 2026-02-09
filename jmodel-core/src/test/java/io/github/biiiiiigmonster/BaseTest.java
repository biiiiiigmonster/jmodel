package io.github.biiiiiigmonster;

import io.github.biiiiiigmonster.driver.InMemoryDataDriver;
import io.github.biiiiiigmonster.driver.QueryCondition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class BaseTest {

    @Autowired
    protected InMemoryDataDriver driver;

    @Before
    public void initData() {
        driver.clear();
        TestDataInitializer.init(driver);
    }

    protected <T extends Model<?>> T findById(Class<T> clazz, Long id) {
        return driver.findById(clazz, id);
    }

    protected <T extends Model<?>> List<T> findByIds(Class<T> clazz, List<Long> ids) {
        QueryCondition<T> cond = QueryCondition.create(clazz).in("id", ids);
        return driver.findByCondition(cond);
    }
}
