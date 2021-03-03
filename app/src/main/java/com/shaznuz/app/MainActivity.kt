package com.shaznuz.app

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ll.view.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var mUploadMessage: ValueCallback<Uri>? = null
    val REQUEST_SELECT_FILE = 100
    private val FILECHOOSER_RESULTCODE = 1

    private val file_type = "image/*" // file types to be allowed for upload

    private val multiple_files = true // allowing multiple file upload


    /*-- MAIN VARIABLES --*/
    var webView: WebView? = null

    private val TAG = MainActivity::class.java.simpleName

    private var cam_file_data: String? = null // for storing camera file information

    private var file_data // data/header received after file selection
            : ValueCallback<Uri?>? = null
    private var file_path // received file(s) temp. location
            : ValueCallback<Array<Uri?>?>? = null

    private val file_req_code = 1




    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CookieManager.getInstance().setAcceptCookie(true)
        mwebview.webChromeClient = object : WebChromeClient() {
            protected fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String?) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)
            }

            // For Lollipop 5.0+ Devices
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(mWebView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams): Boolean {
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                    uploadMessage = null
                }
                uploadMessage = filePathCallback
                val intent = fileChooserParams.createIntent()
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE)
                } catch (e: ActivityNotFoundException) {
                    uploadMessage = null
                    Toast.makeText(this@MainActivity, "Cannot Open File Chooser", Toast.LENGTH_LONG).show()
                    return false
                }
                return true
            }

            //For Android 4.1 only
            protected fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String?, capture: String?) {
                mUploadMessage = uploadMsg
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILECHOOSER_RESULTCODE)
            }

            protected fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)
            }

        }
        //     mwebview.setCookiesEnabled(true)
        mwebview.webViewClient = object : WebViewClient() {


            var url: String = "https://www.shaznuz.com"
            override fun onPageFinished(view: WebView?, url: String?) {

                CookieManager.getInstance().setAcceptCookie(true)
                CookieSyncManager.getInstance().sync();
                super.onPageFinished(view, url)
                spin_kit.visibility = View.GONE

            }

            override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
            ) {
                spin_kit.visibility = View.GONE
                view?.goBack()
                run {
                    if (view!!.canGoBack()) {

                    }
                    spin_kit.visibility = View.GONE

                    Snackbar.make(main, "No Internet Connection!", Snackbar.LENGTH_SHORT).show()
                }

            }


            override fun shouldOverrideUrlLoading(view: WebView, url0: String): Boolean {
                url = url0
                spin_kit.visibility = View.VISIBLE

                if (isOnline()) view.loadUrl(url)
                else {
                    spin_kit.visibility = View.GONE
                    Snackbar.make(main, "No Internet Connection!", Snackbar.LENGTH_SHORT).show()
                }
                return true
            }


        }

        mwebview.loadUrl("https://www.shaznuz.com");
        mwebview.setVerticalScrollBarEnabled(false);
        val wSettings: WebSettings = mwebview.getSettings()
        wSettings.javaScriptEnabled = true
        wSettings.allowFileAccess = true
        wSettings.setAppCacheEnabled(true)
        wSettings.setAppCachePath(getBaseContext().getCacheDir().getAbsolutePath())
        wSettings.javaScriptCanOpenWindowsAutomatically = true
        wSettings.savePassword = true
        wSettings.saveFormData = true
        wSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        if (!isOnline()) {
            Snackbar.make(main, "No Internet Connection!", Snackbar.LENGTH_SHORT).show()

        }


    }

    override fun onBackPressed() {
        if (mwebview.canGoBack()) mwebview.goBack()
        else super.onBackPressed()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun isOnline(): Boolean {

        val connectivityMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val allNetworks: Array<out android.net.Network> = connectivityMgr.allNetworks // added in API 21 (Lollipop)

        for (network in allNetworks) {
            val networkCapabilities = connectivityMgr!!.getNetworkCapabilities(network)
            return (networkCapabilities!!.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)))
        }


        return false
    }




    /*-- checking and asking for required file permissions --*/
    fun file_permission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 1)
            false
        } else {
            true
        }
    }

    /*-- creating new image file here --*/
    @Throws(IOException::class)
    private fun create_image(): File? {
        @SuppressLint("SimpleDateFormat") val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    /*-- creating new video file here --*/
    @Throws(IOException::class)
    private fun create_video(): File? {
        @SuppressLint("SimpleDateFormat") val file_name: String = SimpleDateFormat("yyyy_mm_ss").format(Date())
        val new_name = "file_" + file_name + "_"
        val sd_directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(new_name, ".3gp", sd_directory)
    }

    /*-- back/down key handling --*/
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.getAction() === KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (webView!!.canGoBack()) {
                    webView!!.goBack()
                } else {
                    finish()
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }













    // Create an image file
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat") val timeStamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir: File =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    fun openFileChooser(uploadMsg: ValueCallback<Uri?>?) {
        this.openFileChooser(uploadMsg, "*/*")
    }

    fun openFileChooser(
            uploadMsg: ValueCallback<Uri?>?,
            acceptType: String?
    ) {
        this.openFileChooser(uploadMsg, acceptType, null)
    }

    fun openFileChooser(
            uploadMsg: ValueCallback<Uri?>?,
            acceptType: String?,
            capture: String?
    ) {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "*/*"
        this@MainActivity.startActivityForResult(
                Intent.createChooser(i, "File Browser"),
                FILECHOOSER_RESULTCODE
        )
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            intent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= 21) {
            var results: Array<Uri>? = null
            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == Companion.FCR) {
                    if (null == mUMA) {
                        return
                    }
                    if (intent == null) { //Capture Photo if no image available
                        if (mCM != null) {
                            results = arrayOf(Uri.parse(mCM))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
            }
            val result = if (intent == null || resultCode !== Activity.RESULT_OK) null else intent.data
Log.i("123321", ("332:"+mUMA==null).toString())
            mUMA!!.onReceiveValue(Uri.parse("dat=content://com.android.providers.media.documents/document/image:110347 flg=0x1 "))
            mUMA = null
        } else {
            if (requestCode == Companion.FCR) {
                if (null == mUM) return
                val result =
                        if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
                mUM!!.onReceiveValue(Uri.parse("content://com.android.providers.media.documents/document/image:110478"))
                mUM = null
            }
        }
    }

 private var mCM: String? = null
 private var mUM: ValueCallback<Uri>? = null
 private var mUMA: ValueCallback<Uri>? = null

    companion object {
        private const val FCR = 1
    }

}