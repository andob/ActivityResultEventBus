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
                /*please use wrappedContext, not context here, for instance new Intent(wrappedContext, clazz)*/
                val intent=Intent()
                intent.type="image/*"
                intent.action=Intent.ACTION_GET_CONTENT
                return@factory intent
            }
            .setResultMapper mapper@ { activityResult ->
                if (activityResult.resultCode==Activity.RESULT_OK)
                {
                    activityResult.data?.data?.toString()?.let { imageUrl ->
                        val imagePath=imageUrl.replace("file://", "")
                        return@mapper OnImageFileChoosedFromGalleryEvent(imagePath)
                    }
                }

                return@mapper null
            }
            .startActivity(context)
    }
}
