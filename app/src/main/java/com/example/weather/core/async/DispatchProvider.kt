package com.example.weather.core.async

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

class DefaultDispatchProvider : DispatcherProvider{
    override val main = Dispatchers.Main
    override val io =  Dispatchers.IO
    override val default =  Dispatchers.Default
    override val unconfined = Dispatchers.Unconfined
}
