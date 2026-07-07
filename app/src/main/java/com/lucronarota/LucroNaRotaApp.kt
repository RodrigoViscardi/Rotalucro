package com.lucronarota

import android.app.Application
import com.lucronarota.data.local.database.AppDatabase
import com.lucronarota.data.repository.CorridaRepository
import com.lucronarota.data.repository.CustoRepository
import com.lucronarota.data.repository.JornadaRepository
import com.lucronarota.data.repository.MetaRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class LucroNaRotaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@LucroNaRotaApp)
            modules(appModule)
        }
    }
}

val appModule = module {
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().jornadaDao() }
    single { get<AppDatabase>().corridaDao() }
    single { get<AppDatabase>().custoFixoDao() }
    single { get<AppDatabase>().custoVariavelDao() }
    single { get<AppDatabase>().abastecimentoDao() }
    single { get<AppDatabase>().metaDao() }

    single { JornadaRepository(get()) }
    single { CorridaRepository(get()) }
    single { CustoRepository(get(), get(), get()) }
    single { MetaRepository(get()) }
}
