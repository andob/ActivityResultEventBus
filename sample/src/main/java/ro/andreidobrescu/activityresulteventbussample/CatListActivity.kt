package ro.andreidobrescu.activityresulteventbussample

import android.os.Bundle
import android.widget.Button
import ro.andreidobrescu.activityresulteventbus.ActivityResultEventBus
import ro.andreidobrescu.activityresulteventbus.OnActivityResult
import ro.andreidobrescu.activityresulteventbus.OnPermissionsGrantedEvent
import ro.andreidobrescu.activityresulteventbus.PermissionAskerActivity
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
            PermissionAskerActivity.ask(it.context, android.Manifest.permission.CAMERA)
            OnActivityResult<OnPermissionsGrantedEvent> { event ->
                ActivityResultEventBus.post(OnCatChoosedEvent(cat))
                finish()
            }
        }
    }
}
