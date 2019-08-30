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

    fun <EVENT : Any> post(event : EVENT, delay : Long = 0L)
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

    fun onActivityPostResumed(activity : Activity)
    {
        val activityData=data.find { it.activity==activity }
        if (activityData?.eventListeners?.isEmpty()==false)
            activityData.eventListeners=mutableListOf()
        if (activityData!=null)
            activityData.isActivityInForeground=true
    }

    fun onActivityPaused(activity : Activity)
    {
        val activityData=data.find { it.activity==activity }
        if (activityData!=null)
            activityData.isActivityInForeground=false
    }

    fun <EVENT> registerActivityEventListener(activity : Activity, eventType : Class<EVENT>, eventListener : (EVENT) -> (Unit))
    {
        var activityData=data.find { it.activity==activity }
        if (activityData?.isActivityInForeground==false)
            Handler().post { registerActivityEventListener(activity, eventType, eventListener) }
        else
        {
            activityData=activityData?:ActivityData(activity)
            activityData.eventListeners.add(TypedEventListener(type = eventType, listener = eventListener) as TypedEventListener<Any>)

            if (!data.contains(activityData))
                data.add(activityData)
        }
    }

    fun onActivityDestroyed(activity : Activity)
    {
        val activityData=data.find { it.activity==activity }
        if (activityData!=null)
            data.remove(activityData)
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
