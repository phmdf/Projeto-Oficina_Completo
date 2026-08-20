package com.example.oficina.controle

import android.app.Application
import com.example.oficina.controle.data.AppDatabase
import com.example.oficina.controle.data.Repository

class OficinaApplication : Application() {
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = Repository(db)
    }
}
