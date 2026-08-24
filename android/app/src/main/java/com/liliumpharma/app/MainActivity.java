package com.liliumpharma.app;

import android.os.Bundle;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.getcapacitor.BridgeActivity;

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
    }
}
