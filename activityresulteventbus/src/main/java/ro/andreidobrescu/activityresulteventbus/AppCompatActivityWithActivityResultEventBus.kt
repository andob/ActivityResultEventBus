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
    private val rawEventListeners = mutableMapOf<Class<Any>, (Any) -> Unit>()
    internal fun <EVENT> eventListeners() = rawEventListeners as MutableMap<Class<EVENT>, (EVENT) -> Unit>

    internal val actionsToParseOnActivityResult : Queue<(ActivityResult) -> Unit> = LinkedList()
    internal val actionsToDoAfterOnActivityResult : Queue<() -> Unit> = LinkedList()
    internal val actionsToDoOnRequestPermissionsResult : Queue<() -> Unit> = LinkedList()

    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        while (actionsToParseOnActivityResult.isNotEmpty()&&result!=null)
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
        if (intent!=null&&options!=null)
            intent.putExtras(options)

        if (intent!=null)
            activityLauncher.launch(intent)
    }

    inline fun <reified EVENT> onActivityResult(noinline eventListener : (EVENT) -> Unit)
    {
        onActivityResult(eventType = EVENT::class.java, eventListener = eventListener)
    }

    fun <EVENT> onActivityResult(eventType : Class<EVENT>, eventListener : JActivityResultEventListener<EVENT>)
    {
        onActivityResult(eventType = eventType, eventListener = { event -> eventListener.notify(event) })
    }

    fun <EVENT> onActivityResult(eventType : Class<EVENT>, eventListener : (EVENT) -> Unit)
    {
        eventListeners<EVENT>()[eventType]=eventListener
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
        rawEventListeners.clear()
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

            if (context is android.view.ContextThemeWrapper&&context.baseContext!=null)
                return findFrom(context.baseContext)

            if (context is androidx.appcompat.view.ContextThemeWrapper&&context.baseContext!=null)
                return findFrom(context.baseContext)

            throw RuntimeException("Invalid context type: ${context::class.java}")
        }
    }
}
