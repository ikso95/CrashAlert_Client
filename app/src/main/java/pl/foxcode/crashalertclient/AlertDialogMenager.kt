package pl.foxcode.crashalertclient

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import android.provider.Settings.Global.getString
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import pl.foxcode.crashalertclient.R

class AlertDialogMenager (context: Context){

    val context = context

    fun showDialogMarkerAddedSuccess(){
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.success_title))
            .setMessage(context.getString(R.string.success_data_sent))
            .setPositiveButton(
                context.getString(R.string.ok),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, p1: Int) {
                    }
                })
            .setIcon(R.drawable.ic_check)
            .show()
    }

    fun showDialogChangeEmail(mAuth : FirebaseAuth){
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.dialog_change_email_title))
            .setMessage(context.getString(R.string.dialog_chane_email_message))
            .setPositiveButton(
                context.getString(R.string.ok),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, p1: Int) {
                        val user = mAuth.currentUser
                        if (user != null)
                            user.updateEmail(mAuth.currentUser?.email.toString())
                        Toast.makeText(
                            context,
                            context.getString(R.string.reset_email),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            .setNegativeButton(
                context.getString(R.string.cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                    }
                })
            .setIcon(R.drawable.ic_action_warning)
            .show()
    }


    fun showDialogChangePassword(mAuth: FirebaseAuth){
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.dialog_change_password_title))
            .setMessage(context.getString(R.string.dialog_chane_password_message))
            .setPositiveButton(
                context.getString(R.string.ok),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, p1: Int) {
                        mAuth.sendPasswordResetEmail(mAuth.currentUser!!.email.toString())
                        Toast.makeText(
                            context,
                            context.getString(R.string.reset_password),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            .setNegativeButton(
                context.getString(R.string.cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                    }
                })
            .setIcon(R.drawable.ic_action_warning)
            .show()
    }


    fun showDialogGPSNotEnabled(){
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.dialog_no_GPS_title))
            .setMessage(context.getString(R.string.dialog_no_GPS_message))
            .setPositiveButton(
                context.getString(R.string.yes),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, p1: Int) {
                        startActivity(context,Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), null)
                    }
                })
            .setNegativeButton(
                context.getString(R.string.no),
                object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                    }
                })
            .setIcon(R.drawable.ic_action_warning)
            .show()
    }




}