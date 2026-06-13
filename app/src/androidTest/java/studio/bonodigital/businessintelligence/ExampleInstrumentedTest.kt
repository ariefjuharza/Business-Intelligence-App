package studio.bonodigital.businessintelligence

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Pengujian terinstrumentasi, yang akan dieksekusi pada perangkat Android.
 *
 * Lihat [dokumentasi pengujian](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Konteks aplikasi yang sedang diuji.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("studio.bonodigital.businessintelligence", appContext.packageName)
    }
}