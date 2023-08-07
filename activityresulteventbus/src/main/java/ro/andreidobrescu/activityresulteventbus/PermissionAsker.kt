package ro.andreidobrescu.activityresulteventbus

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionAsker
private constructor()
{
    private var onGranted : FunctionalInterfaces.Procedure? = null
    fun onGranted(value : FunctionalInterfaces.Procedure) = also { onGranted = value }

    private var onDenied : FunctionalInterfaces.Procedure? = null
    fun onDenied(value : FunctionalInterfaces.Procedure) = also { onDenied = value }

    companion object
    {
        @JvmStatic
        fun arePermissionsAccepted(context : Context, vararg permissions : String) : Boolean
        {
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S)
            {
                if (permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    return permissions.toSet().minus(Manifest.permission.ACCESS_FINE_LOCATION).all { permission ->
                        ContextCompat.checkSelfPermission(context, permission)== PackageManager.PERMISSION_GRANTED
                    }
                }
            }

            return permissions.all { permission ->
                ContextCompat.checkSelfPermission(context, permission)== PackageManager.PERMISSION_GRANTED
            }
        }

        @JvmStatic
        fun ask(context : Context, vararg permissions : String) : PermissionAsker
        {
            val activity = AppCompatActivityWithActivityResultEventBus.findFrom(context)
            val permissionAsker = PermissionAsker()

            Handler(Looper.getMainLooper()).post {

                if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M)
                {
                    permissionAsker.onGranted?.invoke()
                }
                else if (arePermissionsAccepted(context = context, permissions = permissions))
                {
                    permissionAsker.onGranted?.invoke()
                }
                else
                {
                    if (permissions.any { permission -> ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) })
                    {
                        Toast.makeText(activity, context.getString(R.string.please_accept_permissions), Toast.LENGTH_LONG).show()

                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.data = Uri.fromParts("package", context.applicationContext.packageName, null)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)

                        permissionAsker.onDenied?.invoke()
                    }
                    else
                    {
                        ActivityCompat.requestPermissions(activity, permissions, 0)
                        activity.actionsToDoOnRequestPermissionsResult.add {

                            if (arePermissionsAccepted(context = context, permissions = permissions))
                            {
                                permissionAsker.onGranted?.invoke()
                            }
                            else
                            {
                                permissionAsker.onDenied?.invoke()
                            }
                        }
                    }
                }
            }

            return permissionAsker
        }
    }
}
