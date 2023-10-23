package com.smkn1.examapp

import android.Manifest
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.text.InputType
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.budiyev.android.codescanner.*
import com.smkn1.examapp.retrofit.ApiInterface
import com.smkn1.examapp.retrofit.ResponseData
import com.smkn1.examapp.retrofit.UrlClient
import exampage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class mainpage : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private val mFragment = exampage()
    private lateinit var password: String
    private lateinit var submitbtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_mainpage)

        requestPermissions()

        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        val qrbtn: Button = findViewById(R.id.qrbutton)
        submitbtn = findViewById(R.id.submitbutton)
        val urlinput: EditText = findViewById(R.id.inputurl)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        val scanningframe: FrameLayout = findViewById(R.id.scanningframe)
        val logo: LinearLayout = findViewById(R.id.logo)

        val mFragmentManager = supportFragmentManager
        val mFragmentTransaction = mFragmentManager.beginTransaction()

        codeScanner = CodeScanner(this, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,

        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not
        submitbtn.isEnabled = false
        getPassword()

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val url = it.toString()
                if(Patterns.WEB_URL.matcher(url).matches()){
                    val mBundle = Bundle()
                    mBundle.putString("URL",url)
                    mFragment.arguments = mBundle
                    if (!Settings.canDrawOverlays(this)) {
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
            if (!Settings.canDrawOverlays(this)){
                requestPermissions()
            }
            codeScanner.startPreview()
            scanningframe.visibility = View.VISIBLE
            logo.visibility = View.GONE
        }


        submitbtn.setOnClickListener {
            if (!Settings.canDrawOverlays(this)){
                requestPermissions()
            }
            if (urlinput.text.toString().isEmpty()){
                Toast.makeText(applicationContext, "Url/IP Kosong", Toast.LENGTH_LONG).show()
            }else{
                val url = urlinput.text.toString()
                if(Patterns.WEB_URL.matcher(url).matches()){
                    val mBundle = Bundle()
                    mBundle.putString("URL",url)
                    mFragment.arguments = mBundle
                    if (!Settings.canDrawOverlays(this)) {
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

    }

//private fun requestOverlay(){}

private fun requestPermissions(){
//    val check :Boolean = ContextCompat.checkSelfPermission(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION) == PackageManager.PERMISSION_GRANTED
    if (android.os.Build.VERSION.SDK_INT >= 24 && !Settings.canDrawOverlays(this)) {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(ACTION_MANAGE_OVERLAY_PERMISSION))
        }
    }
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
    }


//    private fun checkapperaontop(): Boolean {
//        return ContextCompat.checkSelfPermission(this, ACTION_MANAGE_OVERLAY_PERMISSION) ==
//                PackageManager.PERMISSION_DENIED
//    }
//
//    private fun checkcamera(): Boolean {
//        return ContextCompat.checkSelfPermission(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION) ==
//                PackageManager.PERMISSION_DENIED
//    }

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
        if (mFragment.isAdded){
            try{
                hideNav(true)
                return super.onTouchEvent(motionEvent)
            }catch(e: Exception){
                Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        return super.onTouchEvent(motionEvent)
    }

    private fun bringApplicationToFront() {
        val myKeyManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (myKeyManager.isKeyguardLocked) {
            return
        }
            val bringToForegroundIntent = Intent(this, mainpage::class.java)
            bringToForegroundIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(bringToForegroundIntent)
    }

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
                            Toast.makeText(this@mainpage, e.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onFailure(call: Call<List<ResponseData>>, t: Throwable) {
                    Toast.makeText(this@mainpage, t.toString(), Toast.LENGTH_LONG).show()
                }
            })


    }


}

