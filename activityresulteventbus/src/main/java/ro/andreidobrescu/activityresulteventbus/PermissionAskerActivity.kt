package ro.andreidobrescu.activityresulteventbus

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class OnPermissionsGrantedEvent
class OnPermissionsDeniedEvent

class PermissionAskerActivity : Activity()
{
    private lateinit var permissions : Array<out String>

    companion object
    {
        private const val INTENT_KEY_PERMISSIONS = "permissions"

        @JvmStatic
        fun ask(context : Context, vararg permissions : String)
        {
            val intent=Intent(context, PermissionAskerActivity::class.java)
            intent.putExtra(INTENT_KEY_PERMISSIONS, permissions)
            context.startActivity(intent)
        }

        @JvmStatic
        fun arePermissionsAccepted(context : Context, vararg permissions : String) : Boolean
        {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S)
            {
                if (permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    return permissions.toSet().minus(Manifest.permission.ACCESS_FINE_LOCATION).all { permission ->
                        ContextCompat.checkSelfPermission(context, permission)==PackageManager.PERMISSION_GRANTED
                    }
                }
            }

            return permissions.all { permission ->
                ContextCompat.checkSelfPermission(context, permission)==PackageManager.PERMISSION_GRANTED
            }
        }
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        permissions=intent.getStringArrayExtra(INTENT_KEY_PERMISSIONS)!!

        checkAndAskPermissions()
    }

    private fun checkAndAskPermissions()
    {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if (!arePermissionsAccepted(context = this, permissions = permissions))
            {
                if (permissions.any { permission -> ActivityCompat.shouldShowRequestPermissionRationale(this, permission) })
                {
                    Toast.makeText(applicationContext, getString(R.string.please_accept_permissions), Toast.LENGTH_LONG).show()

                    val intent=Intent()
                    intent.action=Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data=Uri.fromParts("package", applicationContext.packageName, null)
                    intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK
                    applicationContext.startActivity(intent)

                    ActivityResultEventBus.post(OnPermissionsDeniedEvent())
                    finish()
                }
                else
                {
                    ActivityCompat.requestPermissions(this, permissions, 0)
                }
            }
            else
            {
                ActivityResultEventBus.post(OnPermissionsGrantedEvent())
                finish()
            }
        }
        else
        {
            ActivityResultEventBus.post(OnPermissionsGrantedEvent())
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode : Int, permissions : Array<out String>, grantResults : IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (arePermissionsAccepted(context = this, permissions = permissions))
        {
            ActivityResultEventBus.post(OnPermissionsGrantedEvent())
            finish()
        }
        else
        {
            ActivityResultEventBus.post(OnPermissionsDeniedEvent())
            finish()
        }
    }

    override fun onBackPressed() {}
}
