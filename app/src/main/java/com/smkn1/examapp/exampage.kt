import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.smkn1.examapp.R


class exampage: Fragment() {
    private lateinit var formview: WebView

    @SuppressLint("SuspiciousIndentation", "SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view: View = inflater.inflate(R.layout.fragment_exampage, container, false)
        formview = view.findViewById(R.id.webview)

        val bundle = arguments
        val URL = bundle!!.getString("URL")

        formview.webViewClient = WebViewClient()
            formview.loadUrl(URL.toString())
            formview.settings.javaScriptEnabled = true
            formview.settings.loadWithOverviewMode = true
            formview.settings.useWideViewPort = true
            formview.settings.builtInZoomControls = true
//            formview.settings.pluginState = WebSettings.PluginState.ON
        return view

    }
    @Override
    override fun onDetach() {
        super.onDetach()
        //
    }
}