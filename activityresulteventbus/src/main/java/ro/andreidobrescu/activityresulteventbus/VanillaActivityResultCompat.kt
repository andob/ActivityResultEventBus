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
fun interface SerializableSupplier<RESULT> : Serializable
{
    fun invoke() : RESULT
}

@FunctionalInterface
fun interface SerializableProcedure : Serializable
{
    fun invoke()
}

data class VanillaActivityResult
(
    val resultCode : Int,
    val data : Intent?
)

@Suppress("UNCHECKED_CAST")
class VanillaActivityResultCompat : Activity()
{
    lateinit var intentFactory : SerializableMapper<Context, Intent>
    var onIntentActivityStarted : SerializableProcedure? = null
    var onIntentActivityStopped : SerializableProcedure? = null
    lateinit var resultMapper : SerializableMapper<VanillaActivityResult, Any?>

    companion object
    {
        private const val INTENT_KEY_INTENT_FACTORY = "INTENT_FACTORY"
        private const val INTENT_KEY_INTENT_ACTIVITY_STARTED = "INTENT_ACTIVITY_STARTED"
        private const val INTENT_KEY_INTENT_ACTIVITY_STOPPED = "INTENT_ACTIVITY_STOPPED"
        private const val INTENT_KEY_INTENT_RESULT_MAPPER = "RESULT_MAPPER"
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

        private var resultMapper : SerializableMapper<VanillaActivityResult, EVENT?>? = null
        fun setResultMapper(mapper : SerializableMapper<VanillaActivityResult, EVENT?>) = also { resultMapper=mapper }

        fun startActivity(context : Context)
        {
            if (intentFactory==null) throw RuntimeException("Please pass intentFactory!!!")
            if (resultMapper==null) throw RuntimeException("Please pass resultMapper!!!")

            val intent=Intent(context, VanillaActivityResultCompat::class.java)
            intent.putExtra(INTENT_KEY_INTENT_FACTORY, intentFactory!!)
            intent.putExtra(INTENT_KEY_INTENT_RESULT_MAPPER, resultMapper!!)
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
        resultMapper=intent.getSerializableExtra(INTENT_KEY_INTENT_RESULT_MAPPER) as SerializableMapper<VanillaActivityResult, Any?>

        val intent=intentFactory.invoke(this)
        startActivityForResult(intent, INTENT_REQUEST_CODE)
        onIntentActivityStarted?.invoke()
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        if (requestCode==INTENT_REQUEST_CODE)
        {
            resultMapper.invoke(VanillaActivityResult(resultCode, data))
                ?.let { event -> ActivityResultEventBus.post(event) }
            onIntentActivityStopped?.invoke()
            finish()
        }
    }
}
