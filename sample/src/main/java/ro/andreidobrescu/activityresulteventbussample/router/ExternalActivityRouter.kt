package ro.andreidobrescu.activityresulteventbussample.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import ro.andreidobrescu.activityresulteventbus.ActivityResultEventBus
import ro.andreidobrescu.activityresulteventbussample.model.OnImageFileChoosedFromGalleryEvent

object ExternalActivityRouter
{
    fun startChoosePictureFromGalleryActivity(context : Context)
    {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        ActivityResultEventBus.createCompatibilityLayer()
            .addResultMapper(Activity.RESULT_OK) { resultIntent ->
                resultIntent?.data?.toString()?.let { imagePath ->
                    OnImageFileChoosedFromGalleryEvent(imagePath)
                }
            }
            .startActivity(context, intent)
    }
}
