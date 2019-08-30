package ro.andreidobrescu.activityresulteventbussample

import androidx.appcompat.app.AppCompatActivity
import ro.andreidobrescu.activityresulteventbus.ActivityResultEventBus

abstract class BaseActivity : AppCompatActivity()
{
    override fun onPostResume()
    {
        super.onPostResume()
        ActivityResultEventBus.onActivityPostResumed(this)
    }

    override fun onPause()
    {
        super.onPause()
        ActivityResultEventBus.onActivityPaused(this)
    }

    override fun onDestroy()
    {
        ActivityResultEventBus.onActivityDestroyed(this)
        super.onDestroy()
    }
}
