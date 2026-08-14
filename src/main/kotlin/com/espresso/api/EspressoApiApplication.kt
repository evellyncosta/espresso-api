package com.espresso.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EspressoApiApplication

fun main(args: Array<String>) {
	runApplication<EspressoApiApplication>(*args)
}
