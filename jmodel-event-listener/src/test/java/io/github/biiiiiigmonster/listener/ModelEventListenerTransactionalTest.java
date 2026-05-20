package io.github.biiiiiigmonster.listener;

import io.github.biiiiiigmonster.entity.User;
import io.github.biiiiiigmonster.event.ModelSavedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ModelEventListenerTransactionalTest.TestConfiguration.class)
public class ModelEventListenerTransactionalTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestModelEventListener listener;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Before
    public void setUp() {
        listener.reset();
    }

    @Test
    public void executesAfterTransactionCommit() {
        User user = new User();

        transactionTemplate.execute(status -> {
            applicationContext.publishEvent(new ModelSavedEvent<>(user, user));
            assertEquals(0, listener.getAfterCommitCount().get());
            return null;
        });

        assertEquals(1, listener.getAfterCommitCount().get());
    }

    @Configuration
    @Import(ModelEventListenerTestConfiguration.class)
    static class TestConfiguration {

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }

        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
            return new TransactionTemplate(transactionManager);
        }
    }
}
