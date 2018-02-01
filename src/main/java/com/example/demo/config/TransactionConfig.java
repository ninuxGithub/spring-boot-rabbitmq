
package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory", 
						transactionManagerRef = "transactionManager", 
						basePackages = {"com.example.demo.repository" }) // 设置Repository所在位置
public class TransactionConfig {
	
	@Autowired
	@Qualifier("dataSource")
	private DataSource persistDataSource;

	@Bean(name = "entityManager")
	public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
		return oracleEntityManagerFactory(builder).getObject().createEntityManager();
	}

	@Bean(name = "entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory(EntityManagerFactoryBuilder builder) {
		return builder.dataSource(persistDataSource).properties(getVendorProperties(persistDataSource))
				.packages("com.example.demo.bean") // 设置实体类所在位置
				.persistenceUnit("persistenceUnit").build();
	}

	@Autowired
	private JpaProperties jpaProperties;

	private Map<String, String> getVendorProperties(DataSource dataSource) {
		Map<String, String> params = new HashMap<>();
		params.put("hibernate.hbm2ddl.auto", "none");
		params.put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
		jpaProperties.setProperties(params);
		return jpaProperties.getHibernateProperties(dataSource);
	}

	@Bean(name = "transactionManager")
	public PlatformTransactionManager transactionManager(EntityManagerFactoryBuilder builder) {
		return new JpaTransactionManager(oracleEntityManagerFactory(builder).getObject());
	}
}
