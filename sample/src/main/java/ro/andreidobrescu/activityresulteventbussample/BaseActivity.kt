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

    override fun onDestroy()
    {
        super.onDestroy()
        ActivityResultEventBus.onActivityDestroyed(this)
    }
}
