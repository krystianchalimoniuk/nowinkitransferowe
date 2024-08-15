package pl.nowinkitransferowe.core.common.network

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val ntDispatcher: NtDispatchers)

enum class NtDispatchers {
    Default,
    IO
}
