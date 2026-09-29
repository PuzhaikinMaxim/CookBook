package com.puj.cookbook.recipes.ui.timer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.puj.cookbook.designsystem.R as DesignSystemR
import com.puj.cookbook.recipes.ui.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Глобальный движок обратного отсчёта. Таймеры живут в singleton-графе Hilt, поэтому
 * продолжают идти при переходах между экранами и по завершении показывают системное уведомление.
 *
 * Один корутин-цикл обслуживает все таймеры, а экраны лишь отрисовывают поток состояний.
 */
@Singleton
class CookingTimerController @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Область корутин, живущая столько же, сколько и приложение. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Менеджер уведомлений для оповещения о завершении таймеров. */
    private val notifications = NotificationManagerCompat.from(context)

    /** Внутреннее изменяемое состояние всех таймеров. */
    private val _timers = MutableStateFlow<Map<String, CookingTimer>>(emptyMap())

    /** Публичный поток состояний всех таймеров по их ключам. */
    val timers: StateFlow<Map<String, CookingTimer>> = _timers.asStateFlow()

    init {
        ensureChannel()
        scope.launch {
            while (isActive) {
                delay(TICK_MILLIS)
                tick()
            }
        }
    }

    /**
     * Регистрирует таймер для [key] при первом появлении; уже идущий отсчёт сохраняется.
     * Если длительность изменилась и таймер не запущен, он сбрасывается на новое значение.
     */
    fun ensure(key: String, label: String, totalSeconds: Long) {
        _timers.update { current ->
            val existing = current[key]
            when {
                existing == null ->
                    current + (key to CookingTimer(key, label, totalSeconds, totalSeconds, false, false))

                existing.totalSeconds != totalSeconds && !existing.isRunning ->
                    current + (
                        key to existing.copy(
                            label = label,
                            totalSeconds = totalSeconds,
                            remainingSeconds = totalSeconds,
                            isFinished = false,
                        )
                        )

                else -> current
            }
        }
    }

    /** Запускает таймер [key]; завершённый таймер перезапускается с начала. */
    fun start(key: String) = _timers.update { current ->
        val timer = current[key] ?: return@update current
        val remaining = if (timer.isFinished || timer.remainingSeconds <= 0L) timer.totalSeconds else timer.remainingSeconds
        current + (key to timer.copy(remainingSeconds = remaining, isRunning = true, isFinished = false))
    }

    /** Ставит таймер [key] на паузу. */
    fun pause(key: String) = _timers.update { current ->
        val timer = current[key] ?: return@update current
        current + (key to timer.copy(isRunning = false))
    }

    /** Сбрасывает таймер [key] к исходной длительности. */
    fun reset(key: String) = _timers.update { current ->
        val timer = current[key] ?: return@update current
        current + (key to timer.copy(remainingSeconds = timer.totalSeconds, isRunning = false, isFinished = false))
    }

    /** Удаляет таймер [key] из состояния. */
    fun remove(key: String) = _timers.update { current -> current - key }

    /** Уменьшает оставшееся время всех запущенных таймеров на одну секунду. */
    private fun tick() {
        val justFinished = mutableListOf<CookingTimer>()
        _timers.update { current ->
            current.mapValues { (_, timer) ->
                if (!timer.isRunning) {
                    timer
                } else {
                    val remaining = timer.remainingSeconds - 1L
                    if (remaining <= 0L) {
                        val finished = timer.copy(remainingSeconds = 0L, isRunning = false, isFinished = true)
                        justFinished += finished
                        finished
                    } else {
                        timer.copy(remainingSeconds = remaining)
                    }
                }
            }
        }
        justFinished.forEach(::notifyFinished)
    }

    /** Показывает системное уведомление о завершении таймера [timer]. */
    private fun notifyFinished(timer: CookingTimer) {
        if (!canNotify()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(DesignSystemR.drawable.ic_timer)
            .setContentTitle(context.getString(R.string.timer_finished_title))
            .setContentText(timer.label.ifBlank { context.getString(R.string.timer_finished_body) })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
        runCatching { notifications.notify(timer.key.hashCode(), notification) }
    }

    /** Проверяет, разрешены ли уведомления на текущей версии Android. */
    private fun canNotify(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** Создаёт канал уведомлений для таймеров, если его ещё нет. */
    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Cooking timers",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = "Alerts when a recipe timer finishes" }
        manager.createNotificationChannel(channel)
    }

    companion object {
        /** Идентификатор канала уведомлений таймеров. */
        const val CHANNEL_ID = "cooking_timers"

        /** Интервал тика движка таймеров в миллисекундах. */
        private const val TICK_MILLIS = 1_000L

        /** Строит стабильный ключ таймера по рецепту и блоку. */
        fun timerKey(recipeId: Long, blockId: Long): String = "$recipeId:$blockId"
    }
}
