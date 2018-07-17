package com.phoenix.kspt.profile


import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.text.format.DateFormat
import android.widget.Toast
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources
import com.phoenix.kspt.Application
import com.phoenix.kspt.Application.Companion.firebase
import com.phoenix.kspt.R
import com.phoenix.kspt.utils.HelpFunctions
import com.phoenix.kspt.utils.IMAGE_CAMERA_REQUEST
import com.phoenix.kspt.utils.USER_AVATAR
import com.phoenix.kspt.firebase.FirebaseFragment
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.yalantis.ucrop.UCrop
import java.io.File
import java.net.URL
import java.util.*

/**
 * Fragment for uploading user's avatar
 */
open class PicturesFragment : FirebaseFragment() {
    private val DESTINATION_FILE_NAME = "picture_from_tab.jpg"
    private var filePathImageCamera: File? = null
    protected lateinit var avatarImg: SimpleDraweeView
    private var resultUri: Uri? = null
    protected lateinit var powerMenu: PowerMenu                      // popup menu
    private val CAMERA_REQUEST_CODE = 100

    /**
     * Init a popup menu
     */
    fun initPopupTransformationMenu() {
        powerMenu = PowerMenu.Builder(context!!)
                .addItem(PowerMenuItem(getString(R.string.I_want_a_fresh_photo), true))
                .addItem(PowerMenuItem(getString(R.string.I_will_find_something_old), true))
                .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT) // Animation start point (TOP | LEFT)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .setWidth(750) // set popup width size
                .setHeight(600) // set popup height size
                .setTextColor(ContextCompat.getColor(context!!, R.color.textColor))
                .setSelectedTextColor(Color.WHITE)
                .setMenuColor(Color.WHITE)
                .setSelectedMenuColor(ContextCompat.getColor(context!!, R.color.button_pressed))
                .setOnMenuItemClickListener(onMenuItemClickListener)
                .build()
    }

    /**
     * Click listener for popup menu
     */
    private val onMenuItemClickListener = OnMenuItemClickListener<PowerMenuItem> { position, _ ->

        powerMenu.selectedPosition = position // change selected item
        powerMenu.dismiss()

        when (position) {
            0 -> photoCameraIntent()
            1 -> photoGalleryIntent()
        }
    }

    /**
     * Request the permissions or start camera
     */
    private fun photoCameraIntent() {
        if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(context!!,
                        android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE)
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startCamera()
            else {
                Toast.makeText(context, getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Show a camera's activity
     */
    private fun startCamera() {
        val photoName = DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString()
        filePathImageCamera = File(activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), photoName + "camera.jpg")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val context = context
        val options = ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out)
        val photoURI = FileProvider.getUriForFile(context!!,
                "com.phoenix.kspt.fileprovider",
                filePathImageCamera!!)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI) //Uri.fromFile(filePathImageCamera));
        startActivityForResult(intent, IMAGE_CAMERA_REQUEST, options.toBundle())
    }

    /**
     * Show a gallery
     */
    private fun photoGalleryIntent() {
        RxImagePicker.with(activity)
                .requestImage(Sources.GALLERY).subscribe { uri ->
                    val destinationUri = Uri.fromFile(File(context!!.cacheDir, Date().time.toString() + DESTINATION_FILE_NAME))
                    UCrop.of(uri, destinationUri)
                            .withAspectRatio(2f, 1f)
                            .withMaxResultSize(1000, 500)
                            .start(context!!, this)
                }
    }

    /**
     * Handle response from activities
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            resultUri = UCrop.getOutput(data!!)
            avatarImg.setImageURI(resultUri.toString())

        } else if (resultCode == UCrop.RESULT_ERROR) {
            UCrop.getError(data!!)!!.printStackTrace()
        }

        if (requestCode == IMAGE_CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (filePathImageCamera != null && filePathImageCamera!!.exists()) {
                    resultUri = Uri.fromFile(filePathImageCamera)
                    avatarImg.setImageURI(resultUri.toString())

                } else {
                    //IS NULL
                }
            }
        }
    }

    /**
     * Upload avatar to firebase storage. Return link to this file and save it url to the firebase database
     */
    protected fun saveAvatar() {
        if (resultUri != null) {
            val name = String.format("%s-%s.jpg", auth.currentUser?.uid, DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString())
            val imageGalleryRef = firebase.gerStorageRef().child(name)

            imageGalleryRef.putFile(resultUri!!).continueWithTask {
                firebase.setUserAvatarInProfile(auth.currentUser!!.uid, imageGalleryRef.downloadUrl.toString())

                imageGalleryRef.downloadUrl
            }.addOnCompleteListener {
                if (it.isSuccessful) {
                    val downloadUri = it.result
                    val downloadUrl = URL(downloadUri.toString()).toString()
                    firebase.setUserAvatarInProfile(auth.currentUser!!.uid, downloadUrl)
                    val editor = HelpFunctions.getSharedPrefEditor(context!!)
                    editor.putString(USER_AVATAR, downloadUrl)
                    editor.apply()
                }
            }
        }
    }
}
