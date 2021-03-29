package ro.andreidobrescu.activityresulteventbussample.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import ro.andreidobrescu.activityresulteventbus.VanillaActivityResultCompat
import ro.andreidobrescu.activityresulteventbussample.model.OnImageFileChoosedFromGalleryEvent

object ExternalActivityRouter
{
    fun startChoosePictureFromGalleryActivity(context : Context)
    {
        VanillaActivityResultCompat.createCompatibilityLayer<OnImageFileChoosedFromGalleryEvent>()
            .setIntentFactory factory@ { wrappedContext : Context ->
                val intent=Intent(/*wrappedContext, clazz*/)
                intent.type="image/*"
                intent.action=Intent.ACTION_GET_CONTENT
                return@factory intent
            }
            .addResultMapper(Activity.RESULT_OK) { resultIntent ->
                resultIntent?.data?.toString()?.let { imagePath ->
                    OnImageFileChoosedFromGalleryEvent(imagePath)
                }
            }
            .startActivity(context)
    }
}
