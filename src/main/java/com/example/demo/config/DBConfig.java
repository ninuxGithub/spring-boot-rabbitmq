package com.example.demo.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBConfig {

	@Bean(name = "dataSource")
	@Qualifier("dataSource")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource DataSource() throws Exception {
		return DataSourceBuilder.create().type(com.alibaba.druid.pool.DruidDataSource.class).build();
	}

}
