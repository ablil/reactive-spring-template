package com.ablil.minitrello.commons

import com.ablil.minitrello.MainApplication
import com.mongodb.MongoClientSettings
import io.micrometer.observation.ObservationRegistry
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.observability.ContextProviderFactory
import org.springframework.data.mongodb.observability.MongoObservationCommandListener
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories


@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = [MainApplication::class])
class PersistenceConfig {

    @Bean
    fun mongoMetricsSynchronousContextProvider(registry: ObservationRegistry): MongoClientSettingsBuilderCustomizer {
        return MongoClientSettingsBuilderCustomizer { clientSettingsBuilder: MongoClientSettings.Builder? ->
            clientSettingsBuilder!!.contextProvider(ContextProviderFactory.create(registry))
                .addCommandListener(MongoObservationCommandListener(registry))
        }
    }
}