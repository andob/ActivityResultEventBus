package ro.andreidobrescu.activityresulteventbus;

//Java compatibility layer for Kotlin's (EVENT) -> (Unit)
public interface JActivityResultEventListener<EVENT>
{
    void notify(EVENT event);
}
