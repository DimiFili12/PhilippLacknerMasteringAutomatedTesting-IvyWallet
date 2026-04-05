package com.ivy.core.persistence.algorithm.accountcache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

// could be dummy for the specific unit test
class AccountCacheDaoFake: AccountCacheDao {

    private val accounts = MutableStateFlow<List<AccountCacheEntity>>(emptyList())

    override fun findAccountCache(accountId: String): Flow<AccountCacheEntity?> {
        return accounts
            .map { entities ->
                entities.find {
                    it.accountId == accountId
                }
            }
    }

    override suspend fun findTimestampById(accountId: String): Instant? {
        return accounts.value.find {
            it.accountId == accountId
        }?.timestamp
    }

    override suspend fun save(cache: AccountCacheEntity) {
        accounts.value += cache
    }

    override suspend fun delete(accountId: String) {
        val accountToDelete = accounts.value.find { it.accountId == accountId } ?: return
        accounts.value -= accountToDelete
    }

    override suspend fun deleteAll() {
        accounts.value = emptyList()
    }
}