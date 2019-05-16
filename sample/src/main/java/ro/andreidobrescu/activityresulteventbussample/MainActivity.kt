package ro.andreidobrescu.activityresulteventbussample

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import ro.andreidobrescu.activityresulteventbus.OnActivityResult
import ro.andreidobrescu.activityresulteventbussample.model.OnCatChoosedEvent
import ro.andreidobrescu.activityresulteventbussample.router.ActivityRouter

class MainActivity : BaseActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        catLabel.setOnClickListener {
            ActivityRouter.startCatListActivity(from = this)
            OnActivityResult<OnCatChoosedEvent> { event ->
                catLabel.text=event.cat.name
            }
        }
    }
}
