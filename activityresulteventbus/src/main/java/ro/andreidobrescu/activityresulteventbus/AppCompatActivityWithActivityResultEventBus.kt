package ro.andreidobrescu.activityresulteventbus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.*

@Suppress("UNCHECKED_CAST")
abstract class AppCompatActivityWithActivityResultEventBus : AppCompatActivity()
{
    private val rawAREBEventListeners = mutableMapOf<Class<Any>, FunctionalInterfaces.Consumer<Any>>()
    fun <EVENT> getAREBEventListeners() = rawAREBEventListeners as MutableMap<Class<EVENT>, FunctionalInterfaces.Consumer<EVENT>>

    internal val actionsToParseOnActivityResult : Queue<FunctionalInterfaces.Consumer<ActivityResult>> = LinkedList()
    internal val actionsToDoAfterOnActivityResult : Queue<FunctionalInterfaces.Procedure> = LinkedList()
    internal val actionsToDoOnRequestPermissionsResult : Queue<FunctionalInterfaces.Procedure> = LinkedList()

    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        while (actionsToParseOnActivityResult.isNotEmpty() && result!=null)
            actionsToParseOnActivityResult.remove().invoke(result)

        while (actionsToDoAfterOnActivityResult.isNotEmpty())
            actionsToDoAfterOnActivityResult.remove().invoke()
    }

    override fun startActivity(intent : Intent?)
    {
        if (intent!=null)
            activityLauncher.launch(intent)
    }

    override fun startActivity(intent : Intent?, options : Bundle?)
    {
        if (intent!=null && options!=null)
            intent.putExtras(options)

        if (intent!=null)
            activityLauncher.launch(intent)
    }

    inline fun <reified EVENT> onActivityResult(eventListener : FunctionalInterfaces.Consumer<EVENT>)
    {
        onActivityResult(EVENT::class.java, eventListener)
    }

    fun <EVENT> onActivityResult(eventType : Class<EVENT>, eventListener : FunctionalInterfaces.Consumer<EVENT>)
    {
        getAREBEventListeners<EVENT>()[eventType] = eventListener
    }

    override fun onRequestPermissionsResult(requestCode : Int, permissions : Array<out String>, grantResults : IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        while (actionsToDoOnRequestPermissionsResult.isNotEmpty())
            actionsToDoOnRequestPermissionsResult.remove().invoke()
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        ActivityResultEventBus.onActivityCreated(activity = this)
    }

    override fun onDestroy()
    {
        ActivityResultEventBus.onActivityDestroyed(activity = this)
        rawAREBEventListeners.clear()
        actionsToParseOnActivityResult.clear()
        actionsToDoAfterOnActivityResult.clear()
        actionsToDoOnRequestPermissionsResult.clear()
        super.onDestroy()
    }

    companion object
    {
        @JvmStatic
        internal fun findFrom(context : Context) : AppCompatActivityWithActivityResultEventBus
        {
            if (context is AppCompatActivityWithActivityResultEventBus)
                return context

            if (context is android.view.ContextThemeWrapper && context.baseContext!=null)
                return findFrom(context.baseContext)

            if (context is androidx.appcompat.view.ContextThemeWrapper && context.baseContext!=null)
                return findFrom(context.baseContext)

            throw RuntimeException("Invalid context type: ${context::class.java}")
        }
    }
}
