package com.example.conveniar_coordenador;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.conveniar_coordenador.databinding.ActivityWebviewBinding;

public class WebViewActivity extends BaseActivity {

    private ActivityWebviewBinding binding;
    private String numPedidoParaPesquisa;
    private boolean automacaoJaExecutadaNestaInstancia = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWebviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String url = getIntent().getStringExtra("URL");
        numPedidoParaPesquisa = getIntent().getStringExtra("NUM_PEDIDO");

        if (url == null || url.isEmpty()) {
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupDrawer();
        configurarWebView();
        binding.webview.loadUrl(url);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.webview.canGoBack()) {
                    binding.webview.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    private void configurarWebView() {
        WebSettings settings = binding.webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        binding.webview.setInitialScale(180);

        binding.webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!url.toLowerCase().contains("login.aspx")) {
                    injetarScriptAutomacao(view);
                }
            }
        });

        binding.webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                binding.progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("WebViewJS", consoleMessage.message());
                return true;
            }
        });
    }

    private void injetarScriptAutomacao(WebView view) {
        final String pedido = (numPedidoParaPesquisa != null && !automacaoJaExecutadaNestaInstancia) ? numPedidoParaPesquisa : "";

        String script = "javascript:(function() { " +
                "   var numPedido = '" + pedido + "'; " +
                "   var path = window.location.pathname; " +
                "   var storageKey = 'auto_v22_' + path; " +
                "   " +
                "   if (sessionStorage.getItem(storageKey)) return; " +
                "   " +
                "   var attempts = 0; " +
                "   var checkExist = setInterval(function() { " +
                "       attempts++; " +
                "       var btn = document.querySelector('input[id*=\"btnAplicarFiltro\"], input[value*=\"Aplicar\"]'); " +
                "       var sel = document.querySelector('select[id*=\"ddlSituacao\"]'); " +
                "       " +
                "       if (btn && sel) { " +
                "           clearInterval(checkExist); " +
                "           " +
                "           var forcarSelecao = function() { " +
                "               var optTodos = Array.from(sel.options).find(o => o.text.trim().toLowerCase() === 'todos'); " +
                "               if (optTodos) { " +
                "                   if (sel.value !== optTodos.value) { " +
                "                       sel.value = optTodos.value; " +
                "                       sel.selectedIndex = optTodos.index; " +
                "                       if (typeof sel.onchange === 'function') sel.onchange(); " +
                "                       sel.dispatchEvent(new Event('change', { bubbles: true })); " +
                "                       sel.dispatchEvent(new Event('input', { bubbles: true })); " +
                "                   } " +
                "               } " +
                "           }; " +
                "           " +
                "           forcarSelecao(); " +
                "           " +
                "           if (numPedido !== '') { " +
                "               var inp = document.querySelector('input[id*=\"txtCriterio\"]'); " +
                "               if (inp) { inp.value = numPedido; inp.dispatchEvent(new Event('input', { bubbles: true })); } " +
                "           } " +
                "           " +
                "           /* Mantém a trava por 1.5s antes do clique final para evitar resets do ASP.NET */ " +
                "           var lock = setInterval(forcarSelecao, 100); " +
                "           " +
                "           setTimeout(function() { " +
                "               clearInterval(lock); " +
                "               sessionStorage.setItem(storageKey, 'true'); " +
                "               console.log('Automação Final: Aplicando Filtro...'); " +
                "               if(btn.click) btn.click(); " +
                "               else btn.dispatchEvent(new Event('click', { bubbles: true })); " +
                "           }, 1500); " +
                "       } " +
                "       if (attempts > 20) clearInterval(checkExist); " +
                "   }, 500); " +
                "})()";
        
        view.evaluateJavascript(script, value -> {
            if (!pedido.isEmpty()) automacaoJaExecutadaNestaInstancia = true;
        });
    }
}
