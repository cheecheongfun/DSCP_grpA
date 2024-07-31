package sg.edu.np.mad.greencycle.SolarInsight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;
import sg.edu.np.mad.greencycle.StartUp.MainActivity;
import sg.edu.np.mad.greencycle.TankSelection.TankSelection;

public class Insight extends AppCompatActivity {

    User user;
    WebView webView;
    TextView backBtn, refreshBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.solar_insights);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");

        webView = findViewById(R.id.webView);
//        backBtn = findViewById(R.id.backButton);
//        backBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Insight.this, MainActivity.class);
//                intent.putExtra("user", user);
//                intent.putExtra("tab", "home_tab");
//                startActivity(intent);
//            }
//        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setUseWideViewPort(true); // Enable wide viewport
        webSettings.setLoadWithOverviewMode(true); // Adjust content to fit the screen

        webView.setInitialScale(1); // Set initial scale to fit screen width
        webView.getSettings().setBuiltInZoomControls(true); // Enable zoom controls
        webView.getSettings().setDisplayZoomControls(false); // Disable on-screen zoom controls

        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new CustomWebChromeClient());

        String embedUrl = "https://app.powerbi.com/reportEmbed?reportId=dda73211-945f-4e9a-b2ad-721f3e8234be&autoAuth=true&ctid=cba9e115-3016-4462-a1ab-a565cba0cdf1&filterPaneEnabled=false";
        webView.loadUrl(embedUrl);
    }
    private static class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }
    private static class CustomWebChromeClient extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
            WebView newWebView = new WebView(view.getContext());
            WebSettings webSettings = newWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setSupportMultipleWindows(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

            newWebView.setWebViewClient(new CustomWebViewClient());
            newWebView.setWebChromeClient(this);

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
        }
    }
}