package com.ivy.common.time.provider

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class TimeProviderFake: TimeProvider {
    override fun timeNow(): LocalDateTime {
        return LocalDateTime.now()
    }

    override fun dateNow(): LocalDate {
        return LocalDate.now()
    }

    override fun zoneId(): ZoneId {
        return ZoneId.of("UTC")
    }

}