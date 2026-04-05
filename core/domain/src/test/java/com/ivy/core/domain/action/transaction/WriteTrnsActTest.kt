package com.ivy.core.domain.action.transaction

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.ivy.common.time.provider.TimeProviderFake
import com.ivy.data.account.Account
import com.ivy.core.data.TagId
import com.ivy.core.data.sync.UniqueId
import com.ivy.core.domain.algorithm.accountcache.InvalidateAccCacheAct
import com.ivy.core.persistence.algorithm.accountcache.AccountCacheDaoFake
import com.ivy.core.persistence.dao.trn.TransactionDaoFake
import com.ivy.data.Sync
import com.ivy.data.SyncState
import com.ivy.data.Value
import com.ivy.data.account.AccountState
import com.ivy.data.attachment.Attachment
import com.ivy.data.attachment.AttachmentSource
import com.ivy.data.attachment.AttachmentType
import com.ivy.data.tag.Tag
import com.ivy.data.tag.TagState
import com.ivy.data.transaction.Transaction
import com.ivy.data.transaction.TransactionType
import com.ivy.data.transaction.TrnMetadata
import com.ivy.data.transaction.TrnPurpose
import com.ivy.data.transaction.TrnState
import com.ivy.data.transaction.TrnTime
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class WriteTrnsActTest {

    private lateinit var writeTrnsAct: WriteTrnsAct
    private lateinit var transactionDaoFake: TransactionDaoFake
    private lateinit var timeProviderFake: TimeProviderFake
    private lateinit var accountCacheDaoFake: AccountCacheDaoFake

    @BeforeEach
    fun setUp() {
        transactionDaoFake = TransactionDaoFake()
        timeProviderFake = TimeProviderFake()
        accountCacheDaoFake = AccountCacheDaoFake()
        writeTrnsAct = WriteTrnsAct(
            transactionDao = transactionDaoFake,
            trnsSignal = TrnsSignal(),
            timeProvider = timeProviderFake,
            invalidateAccCacheAct = InvalidateAccCacheAct(
                accountCacheDao = accountCacheDaoFake,
                timeProvider = timeProviderFake
            ),
            accountCacheDao = accountCacheDaoFake
        )
    }

    @Test
    fun `Test create new transaction with expense`() = runBlocking<Unit> {
        val account = Account(
            id = UUID.randomUUID(),
            name = "test account",
            currency = "EUR",
            color = 0x00f15e,
            icon = null,
            excluded = false,
            folderId = null,
            orderNum = 1.0,
            state = AccountState.Default,
            sync = Sync(
                state = SyncState.Syncing,
                lastUpdated = LocalDateTime.now()
            )
        )
        val transactionId = UUID.randomUUID()
        val tag = Tag(
            id = UUID.randomUUID().toString(),
            name = "test tag",
            color = 0x00f15e,
            orderNum = 1.0,
            state = TagState.Default,
            sync = Sync(
                state = SyncState.Syncing,
                lastUpdated = LocalDateTime.now()
            ),
        )
        val attachment = Attachment(
            id = UUID.randomUUID().toString(),
            associatedId = transactionId.toString(),
            uri = "test uri",
            source = AttachmentSource.Local,
            filename = null,
            type = AttachmentType.Image,
            sync = Sync(
                state = SyncState.Syncing,
                lastUpdated = LocalDateTime.now()
            )
        )
        val transaction = Transaction(
            id = transactionId,
            account = account,
            type = TransactionType.Expense,
            value = Value(
                amount = 50.0,
                currency = "EUR"
            ),
            category = null,
            time = TrnTime.Actual(LocalDateTime.now()),
            title = "test transaction",
            description = null,
            state = TrnState.Default,
            purpose = TrnPurpose.Fee,
            tags = listOf(tag),
            attachments = listOf(attachment),
            metadata = TrnMetadata(
                recurringRuleId = null,
                loanId = null,
                loanRecordId = null
            ),
            sync = Sync(
                state = SyncState.Syncing,
                lastUpdated = LocalDateTime.now()
            )
        )

        writeTrnsAct(
            input = WriteTrnsAct.Input.CreateNew(transaction)
        )

        val cashedTransaction = transactionDaoFake.transactions.find {
            it.id == transactionId.toString()
        }
        val cashedTag = transactionDaoFake.tags.find {
            it.tagId == tag.id
        }
        val cashedAttachment = transactionDaoFake.attachments.find {
            it.id == attachment.id
        }

        assertThat(cashedTransaction).isNotNull()
        assertThat(cashedTransaction?.type).isEqualTo(TransactionType.Expense)

        assertThat(cashedTag).isNotNull()
        assertThat(cashedAttachment).isNotNull()
    }
}