package com.example.weather.core.async


import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


suspend inline fun <T> withIo(dp: DispatcherProvider, crossinline block: suspend () -> T) =
    withContext(dp.io) { block() }

suspend inline fun <T> withCpu(dp: DispatcherProvider, crossinline block: suspend () -> T) =
    withContext(dp.default) { block() }


suspend fun <T> Task<T>.awaitIo(dp: DispatcherProvider): T =
    withContext(dp.io) { await() }