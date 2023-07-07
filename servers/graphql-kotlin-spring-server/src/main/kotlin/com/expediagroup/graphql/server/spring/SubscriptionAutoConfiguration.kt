/*
 * Copyright 2023 Expedia, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.expediagroup.graphql.server.spring

import com.expediagroup.graphql.generator.hooks.FlowSubscriptionSchemaGeneratorHooks
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.server.operations.Subscription
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

/**
 * This value is needed so that this url handler is run without a drastically different order
 * to the graphql routes in [GraphQLRoutesConfiguration]. If we use [org.springframework.core.Ordered] to set as extreme
 * high or low, then the requests are not handled properly.
 *
 * Hopefully we can eventually move the url handler to the same router DSL.
 * https://github.com/spring-projects/spring-framework/issues/19476
 */
private const val URL_HANDLER_ORDER = 0

/**
 * SpringBoot auto-configuration that creates default WebSocket handler for GraphQL subscriptions.
 */
@Configuration
@ConditionalOnBean(Subscription::class)
@Import(
    SubscriptionApolloWsAutoConfiguration::class,
    SubscriptionGraphQLWsAutoConfiguration::class
)
class SubscriptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun flowSubscriptionSchemaGeneratorHooks(): SchemaGeneratorHooks = FlowSubscriptionSchemaGeneratorHooks()

    // used for handshake and upgrading connections
    @Bean
    @ConditionalOnMissingBean
    fun websocketHandlerAdapter(): WebSocketHandlerAdapter = WebSocketHandlerAdapter()

    // workaround for https://github.com/spring-projects/spring-framework/issues/19476
    @Bean
    fun subscriptionHandlerMapping(config: GraphQLConfigurationProperties, webSocketHandler: WebSocketHandler): HandlerMapping =
        SimpleUrlHandlerMapping(mapOf(config.subscriptions.endpoint to webSocketHandler), URL_HANDLER_ORDER)
}
