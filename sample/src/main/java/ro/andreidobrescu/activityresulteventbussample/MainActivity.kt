package ro.andreidobrescu.activityresulteventbussample

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import ro.andreidobrescu.activityresulteventbus.*
import ro.andreidobrescu.activityresulteventbussample.model.OnCatChoosedEvent
import ro.andreidobrescu.activityresulteventbussample.model.OnImageFileChoosedFromGalleryEvent
import ro.andreidobrescu.activityresulteventbussample.router.ActivityRouter
import ro.andreidobrescu.activityresulteventbussample.router.ExternalActivityRouter

class MainActivity : BaseActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chooseCatButton=findViewById<TextView>(R.id.chooseCatButton)!!
        val choosePictureButton=findViewById<Button>(R.id.choosePictureButton)!!
        val imageView=findViewById<ImageView>(R.id.imageView)!!

        chooseCatButton.setOnClickListener {
            ActivityRouter.startCatListActivity(from = this)
            OnActivityResult<OnCatChoosedEvent> { event ->
                chooseCatButton.text=event.cat.name
            }
        }

        choosePictureButton.setOnClickListener {
            PermissionAskerActivity.ask(it.context, Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            OnActivityResult<OnPermissionsGrantedEvent> { grantedEvent ->
                ExternalActivityRouter.startChoosePictureFromGalleryActivity(it.context)
                OnActivityResult<OnImageFileChoosedFromGalleryEvent> { event ->
                    Picasso.get().load(event.picturePath).into(imageView)
                }
            }
        }
    }
}
