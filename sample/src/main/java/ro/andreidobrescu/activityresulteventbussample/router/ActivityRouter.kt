package ro.andreidobrescu.activityresulteventbussample.router

import android.content.Context
import android.content.Intent
import ro.andreidobrescu.activityresulteventbussample.CatListActivity

object ActivityRouter
{
    fun startCatListActivity(from : Context)
    {
        val i = Intent(from, CatListActivity::class.java)
        from.startActivity(i)
    }
}
