package ro.andreidobrescu.activityresulteventbus

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment

object ActivityResultEventBus
{
    @JvmStatic
    private val registeredActivities = mutableListOf<AppCompatActivityWithActivityResultEventBus>()

    @JvmStatic
    internal fun onActivityCreated(activity : AppCompatActivityWithActivityResultEventBus)
    {
        registeredActivities.add(activity)
    }

    @JvmStatic
    internal fun onActivityDestroyed(activity : AppCompatActivityWithActivityResultEventBus)
    {
        registeredActivities.remove(activity)
    }

    @JvmStatic
    fun <EVENT : Any> post(event : EVENT) = post(event, delay = 0L)

    @JvmStatic
    fun <EVENT : Any> post(event : EVENT, delay : Long)
    {
        val eventClass=event::class.java
        for (activity in registeredActivities)
        {
            activity.eventListeners<EVENT>()[eventClass]?.let { eventListener ->
                activity.actionsToDoAfterOnActivityResult.add {
                    Handler(Looper.getMainLooper()).postDelayed({
                        eventListener.invoke(event)
                    }, delay)
                }
            }
        }
    }

    @JvmStatic
    fun createCompatibilityLayer() = ExternalActivityCompatibilityLayer()
}

//Java compatibility layer for Kotlin's (EVENT) -> (Unit)
interface JActivityResultEventListener<EVENT>
{
    fun notify(event : EVENT)
}

inline fun <reified EVENT> Fragment.onActivityResult(noinline eventListener : (EVENT) -> (Unit))
{
    onActivityResult(context = context!!, eventType = EVENT::class.java, eventListener = eventListener)
}

inline fun <reified EVENT> View.onActivityResult(noinline eventListener : (EVENT) -> (Unit))
{
    onActivityResult(context = context, eventType = EVENT::class.java, eventListener = eventListener)
}

inline fun <reified EVENT> onActivityResult(context : Context, noinline eventListener : (EVENT) -> Unit)
{
    onActivityResult(context = context, eventType = EVENT::class.java, eventListener = eventListener)
}

fun <EVENT> onActivityResult(context : Context, eventType : Class<EVENT>, eventListener : (EVENT) -> Unit)
{
    val activity=AppCompatActivityWithActivityResultEventBus.findFrom(context)
    activity.onActivityResult(eventType = eventType, eventListener = eventListener)
}
