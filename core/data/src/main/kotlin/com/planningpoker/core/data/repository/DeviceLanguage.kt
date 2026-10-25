package com.planningpoker.core.data.repository

/** The language the Tech Lead must answer in, as a BCP 47 tag such as `pt-BR`. */
fun interface DeviceLanguage {
    fun tag(): String
}
