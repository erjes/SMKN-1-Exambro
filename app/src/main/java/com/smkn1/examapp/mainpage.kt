package com.smkn1.examapp

import android.Manifest
import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Patterns
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.budiyev.android.codescanner.*
import com.smkn1.examapp.retrofit.ApiInterface
import com.smkn1.examapp.retrofit.ResponseData
import com.smkn1.examapp.retrofit.UrlClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class mainpage : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private val mFragment = exampage()
    private lateinit var password: String
//    private var urlExam = "http://192.168.0.11:8081"
    private var urlExam = "http://203.77.246.222:8081"
//    private var urlExam = "https://docs.google.com/forms/d/e/1FAIpQLSenwSMLoeVe-o_K3UkP7pKq8PAQgGcZm3x8KR3hfC7MrrOJAA/viewform"
    private lateinit var submitbtn: Button
    private val OVERLAY_PERMISSION_REQUEST_CODE = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_mainpage)

        requestPermissions()

        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        val qrbtn: Button = findViewById(R.id.qrbutton)
        submitbtn = findViewById(R.id.submitbutton)
//        val urlinput: EditText = findViewById(R.id.inputurl)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
//        val scanningframe: FrameLayout = findViewById(R.id.scanningframe)
//        val logo: LinearLayout = findViewById(R.id.logo)

        val mFragmentManager = supportFragmentManager
        val mFragmentTransaction = mFragmentManager.beginTransaction()

        codeScanner = CodeScanner(this, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,

        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not
//        submitbtn.isEnabled = false
        getPassword()

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val url = it.toString()
                if(Patterns.WEB_URL.matcher(url).matches()){
//                    val mBundle = Bundle()
//                    mBundle.putString("URL",url)
                    if (checkOverlay()) {
                        Toast.makeText(applicationContext, "Izinkan Fitur Overlay", Toast.LENGTH_LONG).show()
                        requestPermissions()
                    }else{
                        mFragmentTransaction.add(R.id.fragmentopen, mFragment).commit()
                    }
                }else{
                    Toast.makeText(applicationContext, "Url/IP tidak valid", Toast.LENGTH_LONG).show()
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        qrbtn.setOnClickListener {
//            if (checkapperaontop()){
//                requestPermissions()
//            }
//            if (!Settings.canDrawOverlays(this)){
//                requestPermissions()
//            }
//            codeScanner.startPreview()
//            scanningframe.visibility = View.VISIBLE
//            logo.visibility = View.GONE
            if (checkOverlay()) {
                val intentOverlay = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                Toast.makeText(applicationContext, "Izinkan Fitur Overlay", Toast.LENGTH_LONG).show()
                startActivityForResult(intentOverlay, OVERLAY_PERMISSION_REQUEST_CODE)
            }else{
                isUrlAccessible(urlExam) { isAccessible ->
                    if (isAccessible) {
                        // The URL is accessible, open it in WebView
                        val mBundle = Bundle()
                        mBundle.putString("URL",urlExam)
                        mFragment.arguments = mBundle
                        mFragmentTransaction.add(R.id.fragmentopen, mFragment).commit()
                    } else {
                        Toast.makeText(applicationContext, "Gagal Mengakses IP Ujian", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


        submitbtn.setOnClickListener {
//            if (!Settings.canDrawOverlays(this)){
//                requestPermissions()
//            }
//            if (urlinput.text.toString().isEmpty()){
//                Toast.makeText(applicationContext, "Url/IP Kosong", Toast.LENGTH_LONG).show()
//            }else{
//                val url = urlinput.text.toString()
//                if(Patterns.WEB_URL.matcher(url).matches()){
//                    val mBundle = Bundle()
//                    mBundle.putString("URL",url)
//                    mFragment.arguments = mBundle
//                    if (checkOverlay()) {
//                        Toast.makeText(applicationContext, "Izinkan Fitur Overlay", Toast.LENGTH_LONG).show()
//                        requestPermissions()
//                    }else{
//                        mFragmentTransaction.add(R.id.fragmentopen, mFragment).commit()
//                    }
//                }else{
//                    Toast.makeText(applicationContext, "Url/IP tidak valid", Toast.LENGTH_LONG).show()
//                }
//            }
        }

    }

//private fun requestOverlay(){}

    private fun requestPermissions() {
        if (checkOverlay()) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }
    }

    private fun checkOverlay(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            !Settings.canDrawOverlays(this)
        } else if (Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.Q) {
            !Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            // Check if the overlay permission was granted after the user interaction.
            if (checkOverlay()) {
                Toast.makeText(applicationContext, "Izinkan Fitur Overlay", Toast.LENGTH_LONG).show()
            } else {
                // Permission granted, you can proceed with other tasks.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
            }
        }
    }

//private fun requestPermissions(){
////    val check :Boolean = ContextCompat.checkSelfPermission(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION) == PackageManager.PERMISSION_GRANTED
//    if (Build.VERSION.SDK_INT >= 30 ) {
//        if (!Settings.canDrawOverlays(this)){
//            startActivity(Intent(ACTION_MANAGE_OVERLAY_PERMISSION))
//        }
//    }else if(Build.VERSION.SDK_INT in 24..29 ){
//        if (ContextCompat.checkSelfPermission(this, ACTION_MANAGE_OVERLAY_PERMISSION) != PackageManager.PERMISSION_GRANTED){
////        if (ContextCompat.checkSelfPermission(this, ACTION_MANAGE_OVERLAY_PERMISSION) == PackageManager.PERMISSION_DENIED){
//            startActivity(Intent(ACTION_MANAGE_OVERLAY_PERMISSION))
//        }
//    }
//    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
//    }
//
//
//    private fun checkOverlay(): Boolean {
//        if (Build.VERSION.SDK_INT >= 30) {
//            return !Settings.canDrawOverlays(this)
//        } else if (Build.VERSION.SDK_INT in 24..29) {
////            return ContextCompat.checkSelfPermission(this, ACTION_MANAGE_OVERLAY_PERMISSION) == PackageManager.PERMISSION_DENIED
//            return (ContextCompat.checkSelfPermission(this, ACTION_MANAGE_OVERLAY_PERMISSION) != PackageManager.PERMISSION_GRANTED)
//        }
//        return true
//    }

//
//    private fun checkcamera(): Boolean {
//        return ContextCompat.checkSelfPermission(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION) ==
//                PackageManager.PERMISSION_DENIED
//    }
    private fun showAlertPause() {
    val builder = AlertDialog.Builder(this)
    val inputpassword = EditText(this)
    inputpassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    inputpassword.hint = "Password"
    builder.setTitle("Masukan Password untuk Kembali")
    builder.setView(inputpassword)
    val layoutName = LinearLayout(this)
    layoutName.orientation = LinearLayout.VERTICAL
    layoutName.addView(inputpassword)
    builder.setView(layoutName)

    builder.setPositiveButton("Yes"){ _, _ ->
        val pw = inputpassword.text.toString()
        if (pw == "exitex4m"||pw == password){
            Toast.makeText(this,"Berhasil", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"Password Salah", Toast.LENGTH_SHORT).show()
            showAlertPause()
        }

    }
    val alertDialog: AlertDialog = builder.create()
    alertDialog.setCancelable(false)
    alertDialog.show()
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (mFragment.isAdded){
            val builder = AlertDialog.Builder(this)
            val inputpassword = EditText(this)
            inputpassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            inputpassword.hint = "Password"
            builder.setTitle("Masukan Password untuk keluar")
            builder.setView(inputpassword)
            val layoutName = LinearLayout(this)
            layoutName.orientation = LinearLayout.VERTICAL
            layoutName.addView(inputpassword)
            builder.setView(layoutName)

            builder.setPositiveButton("Yes"){ _, _ ->
                val pw = inputpassword.text.toString()
                if (pw == "exitex4m"||pw == password){
                    supportFragmentManager.beginTransaction().remove(mFragment).commit()
                    val intent = Intent(this, mainpage::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this,"Password Salah", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNeutralButton("Cancel"){ _, _ ->
                Toast.makeText(this,"Batal", Toast.LENGTH_SHORT).show()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }else{
            finish()
        }
    }


    override fun onPause() {
        codeScanner.releaseResources()
        if (mFragment.isAdded){
            try{
                if (isApplicationSentToBackground(this)){
                    showAlertPause()
                }
                bringApplicationToFront()
            }catch(e: Exception){
              Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show()
          }
        }
        super.onPause()
    }

    override fun onResume() {
        codeScanner.startPreview()
        super.onResume()
    }

    private fun hideNav(h: Boolean) {
        val windowInsetsController = ViewCompat.getWindowInsetsController(
            window.decorView
        ) ?: return
        windowInsetsController.systemBarsBehavior
        if (h) {
            try{
                windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
            }catch (e: Exception){
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }
        } else {
            try{
                windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
            }catch (e: Exception){
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onTouchEvent(motionEvent: MotionEvent?): Boolean {
        try {
            if (mFragment.isAdded) {
                hideNav(true)
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
        return super.onTouchEvent(motionEvent)
    }

    private fun bringApplicationToFront() {
        val myKeyManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && myKeyManager.isKeyguardLocked) {
            return
        }

        val bringToForegroundIntent = Intent(this, mainpage::class.java)
        bringToForegroundIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(bringToForegroundIntent)
    }

//    private fun bringApplicationToFront() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
//            if (keyguardManager.isDeviceLocked) {
//                // Device is locked, do not bring to foreground
//                return
//            }
//        }
//
//        val bringToForegroundIntent = Intent(this, mainpage::class.java)
//        bringToForegroundIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        startActivity(bringToForegroundIntent)
//    }


//    override fun onTouchEvent(motionEvent: MotionEvent?): Boolean {
//        if (mFragment.isAdded){
//            try{
//                hideNav(true)
//                return super.onTouchEvent(motionEvent)
//            }catch(e: Exception){
//                Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }
//        return super.onTouchEvent(motionEvent)
//    }
//
//    private fun bringApplicationToFront() {
//        val myKeyManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//        if (myKeyManager.isKeyguardLocked) {
//            return
//        }
//            val bringToForegroundIntent = Intent(this, mainpage::class.java)
//            bringToForegroundIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            startActivity(bringToForegroundIntent)
//    }

    private fun getPassword(){
            val retrofit = UrlClient.getInstance()
            val apiInterface = retrofit.create(ApiInterface::class.java)
            val call = apiInterface.getPass()

            call.enqueue(object : Callback<List<ResponseData>> {
                override fun onResponse(call: Call<List<ResponseData>>, responseData: Response<List<ResponseData>>) {
                    if (responseData.isSuccessful) {
                        try {
                            val exitPassword = responseData.body()
                            if (exitPassword != null) {
                                for (pw in exitPassword) {
                                    // Access JSON values using the properties of the Post object
                                    password = pw.password
                                }
                            }
                            Toast.makeText(this@mainpage, "Berhasil Terhubung Ke Server", Toast.LENGTH_SHORT).show()
                            submitbtn.isEnabled = true

                        } catch (e: Exception) {
                            Toast.makeText(this@mainpage, "Gagal Terhubung Ke Server", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onFailure(call: Call<List<ResponseData>>, t: Throwable) {
                    Toast.makeText(this@mainpage, "Gagal Terhubung Ke Server", Toast.LENGTH_SHORT).show()
                }
            })


    }

    private fun isUrlAccessible(url: String, callback: (Boolean) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val result = try {
                val urlObj = URL(url)
                val connection = urlObj.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connect()
                connection.responseCode == HttpURLConnection.HTTP_OK
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }

    fun isApplicationSentToBackground(context: Context): Boolean {
        val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.getRunningTasks(1)
        if (tasks.isNotEmpty()) {
            val topActivity = tasks[0].topActivity
            if (topActivity!!.packageName != context.packageName) {
                return true
            }
        }
        return false
    }

}

