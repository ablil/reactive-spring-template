package com.ablil.minitrello.commons

import com.ablil.minitrello.MainApplication
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories


@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = [MainApplication::class])
class PersistenceConfig {
}