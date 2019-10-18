package io.github.shankai.auth.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthIndicatorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
@SpringBootApplication(exclude = {
    HibernateJpaAutoConfiguration.class,
    JerseyAutoConfiguration.class,
    GroovyTemplateAutoConfiguration.class,
    JmxAutoConfiguration.class,
    DataSourceAutoConfiguration.class,
    DataSourceHealthIndicatorAutoConfiguration.class,
    RedisAutoConfiguration.class,
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class,
    CassandraAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class
})
@EnableSwagger2
public class TicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketApplication.class, args);
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.basePackage("io.github.shankai.auth.ticket")).build();
    }

}
