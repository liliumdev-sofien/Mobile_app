package com.liliumpharma.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.getcapacitor.Bridge;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebChromeClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The Android back button has no default handling in Capacitor —
        // unhandled, it just finishes the activity (exits the app). Route
        // it through the WebView's own history first, since this app is a
        // real multi-page site rather than a single-page app.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getBridge().getWebView().canGoBack()) {
                    getBridge().getWebView().goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // @capawesome/capacitor-android-edge-to-edge-support stopped padding the
        // WebView away from the status/nav bars under Android 16 (API 36) — the
        // WebView's parent is a CoordinatorLayout, which re-lays-out children on
        // its own Behavior-driven pass and silently overrides a child's manually
        // set margins (confirmed on-device). Applying padding to the
        // CoordinatorLayout itself instead is respected at measurement time.
        View webView = getBridge().getWebView();
        View insetTarget = (webView.getParent() instanceof View) ? (View) webView.getParent() : webView;
        ViewCompat.setOnApplyWindowInsetsListener(insetTarget, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(insetTarget);

        setUpNewWindowAndDownloadHandling();
    }

    // Capacitor's WebView has no handling for window.open()/target="_blank" out of the
    // box: BridgeWebChromeClient (Capacitor's own chrome client, which we extend below
    // rather than replace, so file uploads/camera/geolocation prompts keep working)
    // doesn't override onCreateWindow, and setSupportMultipleWindows defaults to false.
    // Several web pages (e.g. deplacement/list.html's PDF/order/admin/media links) rely
    // on target="_blank"/window.open() — without this, tapping them silently does
    // nothing. Load the requested URL in the same WebView instead of a real new window,
    // since this is a single-WebView wrapper and doing it this way keeps the existing
    // Django session cookie, which a separate window/external browser wouldn't have —
    // but only for URLs the app's own allowNavigation config permits: view.loadUrl()
    // bypasses Capacitor's normal shouldOverrideUrlLoading gate entirely, so anything
    // off-domain is routed through bridge.launchIntent() (the same check regular link
    // taps go through) to send it to an external browser instead of loading it — with
    // the session cookie — inside this WebView.
    private void setUpNewWindowAndDownloadHandling() {
        WebView webView = getBridge().getWebView();
        Bridge bridge = getBridge();

        webView.getSettings().setSupportMultipleWindows(true);
        webView.setWebChromeClient(new BridgeWebChromeClient(bridge) {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView.HitTestResult hitTestResult = view.getHitTestResult();
                String targetUrl = hitTestResult != null ? hitTestResult.getExtra() : null;
                if (targetUrl != null) {
                    loadInWebViewOrExternally(view, targetUrl);
                    return false;
                }

                // window.open() is JS-driven (not a plain <a> click), so there's no
                // HitTestResult - use a throwaway, never-attached WebView purely to catch
                // the URL it was asked to open, then load that in the real WebView (or
                // hand it off externally, per the allowNavigation check above).
                WebView catcher = new WebView(view.getContext());
                catcher.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, String url) {
                        loadInWebViewOrExternally(view, url);
                        // Not attached to any view hierarchy, so catcher.post() may never
                        // run - post the cleanup to the main looper directly instead.
                        new Handler(Looper.getMainLooper()).post(catcher::destroy);
                        return true;
                    }
                });
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(catcher);
                resultMsg.sendToTarget();
                return true;
            }
        });

        // The WebView has no built-in PDF renderer (unlike a full browser), so the
        // rapport/plan PDF export links would otherwise just show a blank page. When the
        // WebView hits a response it can't display itself, download it with the current
        // session's cookies attached and hand it to the device's own PDF/file viewer.
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) ->
            downloadAndOpen(url, mimetype)
        );
    }

    // Loads url in the real WebView if it's within the app's own allowNavigation config,
    // otherwise hands it to an external app/browser via the same gate Capacitor's normal
    // link-tap handling uses (bridge.launchIntent) - so window.open()/target="_blank"
    // navigation can't be used to load off-domain content into the authenticated WebView.
    private void loadInWebViewOrExternally(WebView view, String url) {
        boolean handledExternally = getBridge().launchIntent(Uri.parse(url));
        if (!handledExternally) {
            view.loadUrl(url);
        }
    }

    private void downloadAndOpen(String url, String mimetype) {
        // Same allowNavigation gate as loadInWebViewOrExternally: an off-domain download
        // URL must not get the session cookie attached below, so hand it off externally
        // instead of fetching it ourselves.
        if (getBridge().launchIntent(Uri.parse(url))) {
            return;
        }

        String cookie = CookieManager.getInstance().getCookie(url);
        String resolvedMimetype = mimetype != null ? mimetype : "application/pdf";

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                if (cookie != null) {
                    conn.setRequestProperty("Cookie", cookie);
                }
                conn.connect();

                int status = conn.getResponseCode();
                if (status < 200 || status >= 300) {
                    throw new IOException("Download failed with HTTP " + status);
                }

                File outFile = new File(getCacheDir(), "download_" + System.currentTimeMillis() + ".pdf");
                try (
                    InputStream in = conn.getInputStream();
                    FileOutputStream out = new FileOutputStream(outFile)
                ) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }

                runOnUiThread(() -> openDownloadedFile(outFile, resolvedMimetype));
            } catch (Exception e) {
                runOnUiThread(() ->
                    Toast.makeText(this, "Échec du téléchargement du fichier", Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void openDownloadedFile(File file, String mimetype) {
        Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(fileUri, mimetype);
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(viewIntent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "Aucune application disponible pour ouvrir ce fichier", Toast.LENGTH_LONG).show();
        }
    }
}
