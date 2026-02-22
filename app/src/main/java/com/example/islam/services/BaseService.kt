package com.example.islam.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Tüm Android Service sınıfları için temel sınıf.
 * Coroutine yaşam döngüsü yönetimini otomatik olarak sağlar.
 *
 * Kullanım:
 * - Yeni bir servis eklendiğinde bu sınıfı extend et
 * - serviceScope üzerinden coroutine başlat
 * - onServiceDestroy() override ederek kaynakları temizle
 */
abstract class BaseService : Service() {

    protected val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        onServiceDestroy()
    }

    /**
     * Servis durdurulduğunda çağrılır.
     * Alt sınıflar bu metodu override ederek ek temizlik işlemleri yapabilir.
     */
    open fun onServiceDestroy() = Unit
}
