package ro.andreidobrescu.activityresulteventbus

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.io.Serializable

@FunctionalInterface
fun interface SerializableMapper<ARGUMENT, RESULT> : Serializable
{
    fun invoke(argument : ARGUMENT) : RESULT
}

@FunctionalInterface
fun interface SerializableProcedure : Serializable
{
    fun invoke()
}

@Suppress("UNCHECKED_CAST")
class VanillaActivityResultCompat : Activity()
{
    lateinit var intentFactory : SerializableMapper<Context, Intent>
    var onIntentActivityStarted : SerializableProcedure? = null
    var onIntentActivityStopped : SerializableProcedure? = null
    lateinit var resultMappers : HashMap<Int, SerializableMapper<Intent?, Any?>>

    companion object
    {
        private const val INTENT_KEY_INTENT_FACTORY = "INTENT_FACTORY"
        private const val INTENT_KEY_INTENT_ACTIVITY_STARTED = "INTENT_ACTIVITY_STARTED"
        private const val INTENT_KEY_INTENT_ACTIVITY_STOPPED = "INTENT_ACTIVITY_STOPPED"
        private const val INTENT_KEY_INTENT_RESULT_MAPPERS = "RESULT_MAPPERS"
        private const val INTENT_REQUEST_CODE = 1234

        fun <EVENT> createCompatibilityLayer() = Builder<EVENT>()
    }

    class Builder<EVENT>
    {
        private var intentFactory : SerializableMapper<Context, Intent>? = null
        fun setIntentFactory(factory : SerializableMapper<Context, Intent>) = also { intentFactory=factory }

        private var onIntentActivityStarted : SerializableProcedure? = null
        fun setOnIntentActivityStarted(listener : SerializableProcedure) = also { onIntentActivityStarted=listener }

        private var onIntentActivityStopped : SerializableProcedure? = null
        fun setOnIntentActivityStopped(listener : SerializableProcedure) = also { onIntentActivityStopped=listener }

        private val resultMappers : HashMap<Int, SerializableMapper<Intent?, EVENT?>> = HashMap()
        fun addResultMapper(resultCode : Int, mapper : SerializableMapper<Intent?, EVENT?>) = also { resultMappers[resultCode]=mapper }

        fun startActivity(context : Context)
        {
            if (intentFactory==null) throw RuntimeException("Please pass intentFactory!!!")
            if (resultMappers.isEmpty()) throw RuntimeException("Please pass resultMappers!!!")

            val intent=Intent(context, VanillaActivityResultCompat::class.java)
            intent.putExtra(INTENT_KEY_INTENT_FACTORY, intentFactory!!)
            intent.putExtra(INTENT_KEY_INTENT_RESULT_MAPPERS, resultMappers)
            if (onIntentActivityStarted!=null) intent.putExtra(INTENT_KEY_INTENT_ACTIVITY_STARTED, onIntentActivityStarted)
            if (onIntentActivityStopped!=null) intent.putExtra(INTENT_KEY_INTENT_ACTIVITY_STOPPED, onIntentActivityStopped)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        intentFactory=intent.getSerializableExtra(INTENT_KEY_INTENT_FACTORY) as SerializableMapper<Context, Intent>
        onIntentActivityStarted=intent.getSerializableExtra(INTENT_KEY_INTENT_ACTIVITY_STARTED) as? SerializableProcedure
        onIntentActivityStopped=intent.getSerializableExtra(INTENT_KEY_INTENT_ACTIVITY_STOPPED) as? SerializableProcedure
        resultMappers=intent.getSerializableExtra(INTENT_KEY_INTENT_RESULT_MAPPERS) as HashMap<Int, SerializableMapper<Intent?, Any?>>

        val intent=intentFactory.invoke(this)
        startActivityForResult(intent, INTENT_REQUEST_CODE)

        try { onIntentActivityStarted?.invoke() }
        catch (ignored : Exception) {}
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        if (requestCode==INTENT_REQUEST_CODE)
        {
            resultMappers[resultCode]?.invoke(data)?.let { event ->
                ActivityResultEventBus.post(event)
            }

            try { onIntentActivityStopped?.invoke() }
            catch (ignored : Exception) {}

            finish()
        }
    }
}
