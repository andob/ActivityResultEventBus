package ro.andreidobrescu.activityresulteventbus

import android.app.Activity
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment

object ActivityResultEventBus
{
    private class ActivityData
    (
        var activity : Activity
    )
    {
        var isActivityInForeground = false
        var eventListeners = mutableListOf<TypedEventListener<Any>>()
    }

    private class TypedEventListener<EVENT>
    (
        val type : Class<EVENT>,
        val listener : (EVENT) -> (Unit)
    ) : (EVENT) -> (Unit)
    {
        override fun invoke(event : EVENT) = listener.invoke(event)
    }

    private val data = mutableListOf<ActivityData>()

    fun <EVENT : Any> post(event : EVENT) = post(event, delay = 0L)

    fun <EVENT : Any> post(event : EVENT, delay : Long)
    {
        val eventClass=event::class.java
        for (activityData in data)
        {
            activityData.eventListeners
                .find { it.type==eventClass }
                ?.let { eventListener ->
                    postOnEventListener(eventListener, activityData, event, delay)
                }
        }
    }

    private fun <EVENT : Any> postOnEventListener(eventListener : TypedEventListener<Any>,
                                                  activityData : ActivityData,
                                                  event : EVENT, delay : Long = 0L)
    {
        if (!activityData.isActivityInForeground)
            Handler().post { postOnEventListener(eventListener, activityData, event, delay) }
        else activityData.activity.runOnUiThread {
            if (delay>0)
                Handler().postDelayed({
                    eventListener.invoke(event)
                }, delay)
            else eventListener.invoke(event)
        }
    }

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

    fun onActivityPostResumed(activity : Activity)
    {
        val activityData=findOrCreateActivityData(activity)
        activityData.isActivityInForeground=true

        if (activityData.eventListeners.isNotEmpty())
            activityData.eventListeners=mutableListOf()
    }

    fun onActivityPaused(activity : Activity)
    {
        val activityData=findOrCreateActivityData(activity)
        activityData.isActivityInForeground=false
    }

    fun onActivityDestroyed(activity : Activity)
    {
        val activityData=data.find { it.activity==activity }
        if (activityData!=null)
            data.remove(activityData)
    }

    fun <EVENT> registerActivityEventListener(activity : Activity, eventType : Class<EVENT>, eventListener : (EVENT) -> (Unit))
    {
        val activityData=findOrCreateActivityData(activity)
        if (!activityData.isActivityInForeground)
            Handler().post { registerActivityEventListener(activity, eventType, eventListener) }
        else activityData.eventListeners.add(TypedEventListener(type = eventType, listener = eventListener) as TypedEventListener<Any>)
    }

    fun <EVENT> registerActivityEventListener(activity : Activity, eventType : Class<EVENT>, eventListener : JActivityResultEventListener<EVENT>)
    {
        registerActivityEventListener(activity = activity, eventType = eventType,
            eventListener = { event -> eventListener.notify(event) })
    }
}

inline fun <reified EVENT> Activity.OnActivityResult(noinline eventListener : (EVENT) -> (Unit))
{
    ActivityResultEventBus.registerActivityEventListener(activity = this,
        eventType = EVENT::class.java, eventListener = eventListener)
}

inline fun <reified EVENT> Fragment.OnActivityResult(noinline eventListener : (EVENT) -> (Unit))
{
    activity?.OnActivityResult(eventListener)
}

inline fun <reified EVENT> View.OnActivityResult(noinline eventListener : (EVENT) -> (Unit))
{
    (context as Activity).OnActivityResult(eventListener)
}
