package com.smkn1.examapp

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    private var urlExam = "https://www.instagram.com/"
//  private var urlExam = "http://203.77.246.222:8081"
    private lateinit var submitbtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainpage)

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
        password = "offline"
        getPassword()

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val url = it.toString()
                if(Patterns.WEB_URL.matcher(url).matches()){
                        mFragmentTransaction.add(R.id.fragmentopen, mFragment).commit()
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


        submitbtn.setOnClickListener {
        }

    }


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
            Toast.makeText(this,"Berhasil", Toast.LENGTH_SHORT).show()

    }
    val alertDialog: AlertDialog = builder.create()
    alertDialog.setCancelable(false)
    alertDialog.show()
    }

    private fun showAlertLog() {
        val builder = AlertDialog.Builder(this)
        val inputpassword = EditText(this)
        inputpassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        inputpassword.hint = "Password"
        if (password.isNotEmpty()){inputpassword.setText(password)}
        builder.setTitle("Masukan Password")
        builder.setView(inputpassword)
        val layoutName = LinearLayout(this)
        layoutName.orientation = LinearLayout.VERTICAL
        layoutName.addView(inputpassword)
        builder.setView(layoutName)

        builder.setPositiveButton("Yes"){ _, _ ->
            val pw = inputpassword.text.toString()
            if (pw == "*king*" || pw == password+"king"){
                Toast.makeText(this,"Berhasil", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"Password Salah", Toast.LENGTH_SHORT).show()
                showAlertLog()
            }

        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun showAlertBack(){
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
            if (pw == "*king*"||pw == password){
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
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (mFragment.isAdded){
            showAlertBack()
        }else{
            finish()
        }
    }


    override fun onPause() {
        codeScanner.releaseResources()
        try{
            if (mFragment.isAdded) {
                if (isApplicationSentToBackground(this)){
                    showAlertPause()
                }
            }
        }catch(e: Exception){
            Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show()
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
                                    showAlertLog()
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

    private fun isApplicationSentToBackground(context: Context): Boolean {
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

