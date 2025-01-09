// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.spring.server.grpc

import me.kpavlov.spring.server.grpc.GrpcServerConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import

@AutoConfiguration
@EnableConfigurationProperties(GrpcServerProperties::class)
@Import(GrpcServerConfiguration::class)
open class GrpcServerAutoConfiguration {
    @Autowired
    lateinit var properties: GrpcServerProperties
}
