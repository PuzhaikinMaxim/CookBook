package com.puj.cookbook

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Точка входа Hilt: зависимости предоставляются модулями внутри data-слоёв фич. */
@HiltAndroidApp
class CookBookApplication : Application()
