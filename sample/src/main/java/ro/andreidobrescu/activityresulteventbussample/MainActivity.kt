package ro.andreidobrescu.activityresulteventbussample

import android.os.Bundle
import android.widget.TextView
import ro.andreidobrescu.activityresulteventbus.OnActivityResult
import ro.andreidobrescu.activityresulteventbussample.model.OnCatChoosedEvent
import ro.andreidobrescu.activityresulteventbussample.router.ActivityRouter

class MainActivity : BaseActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val catLabel=findViewById<TextView>(R.id.catLabel)!!

        catLabel.setOnClickListener {
            ActivityRouter.startCatListActivity(from = this)
            OnActivityResult<OnCatChoosedEvent> { event ->
                catLabel.text=event.cat.name
            }
        }
    }
}
