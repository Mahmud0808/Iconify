package com.drdisagree.iconify.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SharedPrefs

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DataStore