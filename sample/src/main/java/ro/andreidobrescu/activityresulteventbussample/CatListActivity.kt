package ro.andreidobrescu.activityresulteventbussample

import android.os.Bundle
import android.widget.Button
import ro.andreidobrescu.activityresulteventbus.ActivityResultEventBus
import ro.andreidobrescu.activityresulteventbussample.model.Cat
import ro.andreidobrescu.activityresulteventbussample.model.OnCatChoosedEvent

class CatListActivity : BaseActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_list)

        val catButton=findViewById<Button>(R.id.catButton)!!

        val cat=Cat()
        catButton.text=cat.name

        catButton.setOnClickListener {
            ActivityResultEventBus.post(OnCatChoosedEvent(cat))
            finish()
        }
    }
}
