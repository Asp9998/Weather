package com.example.weather.domain.model

sealed class RefreshResult{
    data object Updated : RefreshResult()
    data object NotModified: RefreshResult()
    data object SkippedFresh: RefreshResult()
}