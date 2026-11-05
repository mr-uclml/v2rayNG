package com.v2ray.ang.ui.main

import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.dto.entities.ServersCache
import com.v2ray.ang.enums.EConfigType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class MainServerRealPingTest {
    private fun testProfile() = ProfileItem(configType = EConfigType.VMESS)

    @Test
    fun singleServerPingRequestsRealDelayForOnlyThatServerWithoutGroupSideEffects() {
        val message = singleServerRealPingMessage("server-guid")

        assertEquals(AppConfig.MSG_MEASURE_CONFIG_START, message.key)
        assertEquals(listOf("server-guid"), message.serverGuids)
        assertFalse(message.onlyTcp)
        // An empty subscriptionId stops the service from auto-removing or sorting the whole group after one row.
        assertEquals("", message.subscriptionId)
    }

    @Test
    fun resetClearsOnlyTheRequestedServerAndKeepsOthersUntouched() {
        val target = ServersCache("target", testProfile(), testDelayMillis = 120L)
        val other = ServersCache("other", testProfile(), testDelayMillis = 80L)
        val servers = listOf(other, target)

        val reset = resetTestDelayForGuid(servers, "target")

        assertEquals(0L, reset[1].testDelayMillis)
        assertEquals(80L, reset[0].testDelayMillis)
        assertSame(other, reset[0])
    }

    @Test
    fun resetKeepsAlreadyUntestedOrUnknownServersUnchanged() {
        val untested = ServersCache("target", testProfile(), testDelayMillis = 0L)
        val servers = listOf(untested)

        assertSame(untested, resetTestDelayForGuid(servers, "target")[0])
        assertEquals(0L, resetTestDelayForGuid(servers, "missing")[0].testDelayMillis)
    }

    @Test
    fun resetClearsRowDelayByGuidOnly() {
        val row = ServerRowUiModel(
            guid = "target",
            profile = testProfile(),
            remarks = "name",
            statistics = "",
            typeDescription = "",
            testDelayMillis = 95L,
            subscriptionBadge = "",
        )
        val other = row.copy(guid = "other", testDelayMillis = 40L)

        val reset = resetTestDelayForGuidInRows(listOf(other, row), "target")

        assertEquals(40L, reset[0].testDelayMillis)
        assertEquals(0L, reset[1].testDelayMillis)
    }
}
