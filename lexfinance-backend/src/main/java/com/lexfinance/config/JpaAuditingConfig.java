package com.lexfinance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@Import({HibernateConfig.class, TenantIdentifierResolver.class})
public class JpaAuditingConfig {
}
