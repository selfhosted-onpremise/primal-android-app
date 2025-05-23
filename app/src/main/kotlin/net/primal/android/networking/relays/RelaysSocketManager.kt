package net.primal.android.networking.relays

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.db.UsersDatabase
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.mapToRelayDO
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.sockets.NostrSocketClientFactory
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.NostrEvent
import timber.log.Timber

@Singleton
class RelaysSocketManager @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val nostrSocketClientFactory: NostrSocketClientFactory,
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val activeAccountStore: ActiveAccountStore,
    private val usersDatabase: UsersDatabase,
) {
    private val scope = CoroutineScope(dispatchers.io())
    private val relayPoolsMutex = Mutex()

    private var relaysObserverJob: Job? = null

    private fun buildRelayPool() =
        RelayPool(
            dispatchers = dispatchers,
            nostrSocketClientFactory = nostrSocketClientFactory,
            primalApiClient = primalApiClient,
        )
    private val userRelaysPool: RelayPool = buildRelayPool()
    private val nwcRelaysPool: RelayPool = buildRelayPool()
    private val fallbackRelaysPool: RelayPool = buildRelayPool()

    val userRelayPoolStatus = userRelaysPool.relayPoolStatus

    init {
        initFallbackRelaysPool()
        observeActiveUserId()
    }

    private fun initFallbackRelaysPool() = fallbackRelaysPool.changeRelays(FALLBACK_RELAYS)

    private fun observeActiveUserId() =
        scope.launch {
            activeAccountStore.activeUserId.collect { userId ->
                when {
                    userId.isEmpty() -> {
                        relaysObserverJob?.cancel()
                        relaysObserverJob = null
                        clearRelayPools()
                    }

                    else -> {
                        relaysObserverJob?.cancel()
                        relaysObserverJob = observeRelays(userId)
                    }
                }
            }
        }

    private suspend fun isCachingProxyEnabled() = activeAccountStore.activeUserAccount().cachingProxyEnabled

    private fun observeRelays(userId: String): Job =
        scope.launch {
            try {
                usersDatabase.relays().observeRelays(userId = userId).collect { relays ->
                    val userRelays = relays.filter { it.kind == RelayKind.UserRelay }.map { it.mapToRelayDO() }
                    val nwcRelays = relays.filter { it.kind == RelayKind.NwcRelay }.map { it.mapToRelayDO() }
                    updateRelayPools(regularRelays = userRelays, walletRelays = nwcRelays)
                }
            } catch (error: CancellationException) {
                Timber.w(error)
            }
        }

    private suspend fun updateRelayPools(regularRelays: List<Relay>?, walletRelays: List<Relay>?) {
        relayPoolsMutex.withLock {
            val userRelaysChanged = userRelaysPool.relays != regularRelays
            if (userRelaysChanged && !regularRelays.isNullOrEmpty()) {
                userRelaysPool.changeRelays(relays = regularRelays)
            }

            val nwcRelaysChanged = nwcRelaysPool.relays != walletRelays
            if (nwcRelaysChanged && !walletRelays.isNullOrEmpty()) {
                nwcRelaysPool.changeRelays(relays = walletRelays)
            }
        }
    }

    private suspend fun clearRelayPools() =
        relayPoolsMutex.withLock {
            userRelaysPool.closePool()
            nwcRelaysPool.closePool()
        }

    @Throws(NostrPublishException::class)
    suspend fun publishEvent(nostrEvent: NostrEvent) {
        if (userRelaysPool.hasRelays()) {
            userRelaysPool.publishEvent(nostrEvent = nostrEvent, cachingProxyEnabled = isCachingProxyEnabled())
        } else {
            fallbackRelaysPool.publishEvent(nostrEvent = nostrEvent, cachingProxyEnabled = isCachingProxyEnabled())
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun publishEvent(nostrEvent: NostrEvent, relays: List<Relay>) {
        val customPool = buildRelayPool()
        customPool.changeRelays(relays = relays)
        customPool.tryConnectingToAllRelays()
        customPool.publishEvent(nostrEvent = nostrEvent, cachingProxyEnabled = isCachingProxyEnabled())
        customPool.closePool()
    }

    @Throws(NostrPublishException::class)
    suspend fun publishNwcEvent(nostrEvent: NostrEvent) {
        if (!nwcRelaysPool.hasRelays()) {
            throw NostrPublishException(cause = IllegalStateException("nwc relay not found"))
        }

        nwcRelaysPool.publishEvent(nostrEvent = nostrEvent, cachingProxyEnabled = isCachingProxyEnabled())
    }

    suspend fun tryConnectingToAllUserRelays() = userRelaysPool.tryConnectingToAllRelays()

    suspend fun tryConnectingToUserRelay(url: String) = userRelaysPool.tryConnectingToRelay(url)
}
