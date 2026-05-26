package com.msa.eshop.backend

import com.msa.eshop.backend.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
class EshopApplication

fun main(args: Array<String>) {
    runApplication<EshopApplication>(*args)
}
