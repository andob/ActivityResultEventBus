package ro.andreidobrescu.activityresulteventbussample

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import ro.andreidobrescu.activityresulteventbus.PermissionAsker
import ro.andreidobrescu.activityresulteventbussample.model.OnCatChoosedEvent
import ro.andreidobrescu.activityresulteventbussample.model.OnImageFileChoosedFromGalleryEvent
import ro.andreidobrescu.activityresulteventbussample.router.ActivityRouter
import ro.andreidobrescu.activityresulteventbussample.router.ExternalActivityRouter

class MainActivity : BaseActivity()
{
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chooseCatButton = findViewById<TextView>(R.id.chooseCatButton)!!
        val choosePictureButton = findViewById<Button>(R.id.choosePictureButton)!!
        val imageView = findViewById<ImageView>(R.id.imageView)!!

        chooseCatButton.setOnClickListener {
            ActivityRouter.startCatListActivity(from = this)
            onActivityResult<OnCatChoosedEvent> { event ->
                chooseCatButton.text = event.cat.name
            }
        }

        choosePictureButton.setOnClickListener {
            PermissionAsker.ask(it.context, Manifest.permission.CAMERA).onGranted {
                ExternalActivityRouter.startChoosePictureFromGalleryActivity(it.context)
                onActivityResult<OnImageFileChoosedFromGalleryEvent> { event ->
                    Picasso.get().load(event.picturePath).into(imageView)
                }
            }
        }
    }
}
