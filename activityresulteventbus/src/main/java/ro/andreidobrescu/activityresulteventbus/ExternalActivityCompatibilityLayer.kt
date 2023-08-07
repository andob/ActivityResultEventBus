package ro.andreidobrescu.activityresulteventbus

import android.content.Context
import android.content.Intent

class ExternalActivityCompatibilityLayer
{
    private val resultMappers = mutableMapOf<Int, FunctionalInterfaces.Mapper<Intent?, Any?>>()
    fun addResultMapper(resultCode : Int, mapper : FunctionalInterfaces.Mapper<Intent?, Any?>) = also { resultMappers[resultCode] = mapper }

    fun doOnResult(resultCode : Int, consumer : FunctionalInterfaces.Consumer<Intent?>) = also {
        resultMappers[resultCode] = FunctionalInterfaces.Mapper<Intent?, Any?> { consumer(it); null }
    }

    fun startActivity(context : Context?, intent : Intent?)
    {
        if (intent==null) throw RuntimeException("Please pass intent!!!")
        if (context==null) throw RuntimeException("Please pass context!!!")
        if (resultMappers.isEmpty()) throw RuntimeException("Please pass resultMappers!!!")

        val activity = AppCompatActivityWithActivityResultEventBus.findFrom(context)

        activity.actionsToParseOnActivityResult.add { activityResult ->
            resultMappers[activityResult.resultCode]?.invoke(activityResult.data)?.let { event ->
                ActivityResultEventBus.post(event)
            }
        }

        context.startActivity(intent)
    }
}
