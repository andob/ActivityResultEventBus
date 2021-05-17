package ro.andreidobrescu.activityresulteventbus

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.ContextThemeWrapper
import android.view.View
import androidx.fragment.app.Fragment
import java.util.*

@Suppress("UNCHECKED_CAST")
object ActivityResultEventBus
{
    private class ActivityData
    (
        var activity : Activity
    )
    {
        var isActivityInForeground = false

        private val eventListeners = mutableMapOf<Class<Any>, (Any) -> (Unit)>()
        fun <EVENT> eventListeners() = eventListeners as MutableMap<Class<EVENT>, (EVENT) -> (Unit)>

        val actionsToDoAfterActivityComesToForeground : Queue<() -> (Unit)> = LinkedList()
    }

    @JvmStatic
    private val data = mutableListOf<ActivityData>()

    @JvmStatic
    fun <EVENT : Any> post(event : EVENT) = post(event, delay = 0L)

    @JvmStatic
    fun <EVENT : Any> post(event : EVENT, delay : Long)
    {
        val eventClass=event::class.java
        for (activityData in data)
        {
            activityData.eventListeners<EVENT>()[eventClass]?.let { eventListener ->
                scheduleEventListener(eventListener, activityData, event, delay)
            }
        }
    }

    @JvmStatic
    private fun <EVENT : Any> scheduleEventListener
    (
        eventListener : (EVENT) -> (Unit),
        activityData : ActivityData,
        event : EVENT, delay : Long = 0L
    )
    {
        if (!activityData.isActivityInForeground)
        {
            activityData.actionsToDoAfterActivityComesToForeground.add {
                scheduleEventListener(eventListener, activityData, event, delay)
            }
        }
        else
        {
            activityData.activity.runOnUiThread {
                activityData.eventListeners<EVENT>().remove(event::class.java)
                invokeEventListener(eventListener, event, delay)
            }
        }
    }

    @JvmStatic
    private fun <EVENT : Any> invokeEventListener
    (
        eventListener : (EVENT) -> (Unit),
        event : EVENT, delay : Long
    )
    {
        if (delay>0)
        {
            Handler(Looper.getMainLooper()).postDelayed({
                eventListener.invoke(event)
            }, delay)
        }
        else
        {
            eventListener.invoke(event)
        }
    }

    @JvmStatic
    private fun findOrCreateActivityData(activity : Activity) : ActivityData
    {
        var activityData=data.find { it.activity==activity }
        if (activityData==null)
        {
            activityData=ActivityData(activity)
            data.add(activityData)
        }

        return activityData
    }

    @JvmStatic
    fun onActivityPostResumed(activity : Activity)
    {
        val activityData=findOrCreateActivityData(activity)
        activityData.isActivityInForeground=true

        while(activityData.actionsToDoAfterActivityComesToForeground.isNotEmpty())
            activityData.actionsToDoAfterActivityComesToForeground.remove().invoke()
    }

    @JvmStatic
    fun onActivityPaused(activity : Activity)
    {
        val activityData=findOrCreateActivityData(activity)
        activityData.isActivityInForeground=false
    }

    @JvmStatic
    fun onActivityDestroyed(activity : Activity)
    {
        val activityData=data.find { it.activity==activity }
        if (activityData!=null)
            data.remove(activityData)
    }

    @JvmStatic
    fun <EVENT> registerActivityEventListener(activity : Activity, eventType : Class<EVENT>, eventListener : (EVENT) -> (Unit))
    {
        val activityData=findOrCreateActivityData(activity)
        if (!activityData.isActivityInForeground)
        {
            activityData.actionsToDoAfterActivityComesToForeground.add {
                registerActivityEventListener(activity, eventType, eventListener)
            }
        }
        else
        {
            activityData.eventListeners<EVENT>()[eventType]=eventListener
        }
    }

    @JvmStatic
    fun <EVENT> registerActivityEventListener(activity : Activity, eventType : Class<EVENT>, eventListener : JActivityResultEventListener<EVENT>)
    {
        registerActivityEventListener(activity = activity, eventType = eventType,
            eventListener = { event -> eventListener.notify(event) })
    }
}

@Suppress("FunctionName")
inline fun <reified EVENT> Activity.OnActivityResult(noinline eventListener : (EVENT) -> (Unit))
{
    ActivityResultEventBus.registerActivityEventListener(activity = this,
        eventType = EVENT::class.java, eventListener = eventListener)
}

@Suppress("FunctionName")
inline fun <reified EVENT> Fragment.OnActivityResult(noinline eventListener : (EVENT) -> (Unit))
{
    activity?.OnActivityResult(eventListener)
}

@Suppress("FunctionName")
inline fun <reified EVENT> View.OnActivityResult(noinline eventListener : (EVENT) -> (Unit))
{
    var lookupContext=context

    do
    {
        if (lookupContext is Activity)
            lookupContext.OnActivityResult(eventListener)
        else if (lookupContext is ContextThemeWrapper&&lookupContext.baseContext!=null)
            lookupContext=lookupContext.baseContext
    }
    while (lookupContext !is Activity)
}
