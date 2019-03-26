package com.mapr.datasender.config

data class Config (
    val host : String = "10.2.15.168",
    val port : Int = 8080,
    val username : String = "mapr",
    val password : String = "mapr"
)