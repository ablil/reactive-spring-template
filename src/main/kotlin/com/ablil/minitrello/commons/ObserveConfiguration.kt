package com.ablil.minitrello.commons

import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.aop.ObservedAspect
import org.springframework.context.annotation.Configuration


@Configuration(proxyBeanMethods = false)
class ObserveConfiguration {
    //    @Bean // uncomment this visualize methods execution on zipkin
    fun observedAspect(observationRegistry: ObservationRegistry): ObservedAspect {
        return ObservedAspect(observationRegistry)
    }
}