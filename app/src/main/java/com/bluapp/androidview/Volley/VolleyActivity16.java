package com.bluapp.androidview.Volley;

import android.app.ProgressDialog;
import android.content.Context;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bluapp.androidview.R;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class VolleyActivity16 extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private Button login;
    private ProgressDialog progressDialog;
    private String baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volley16);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        baseUrl = "https://postman-echo.com/basic-auth";

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                submitData();
            }
        });
    }

    private void submitData() {
        final String enteredusername = username.getText().toString();
        final String enteredpassword = password.getText().toString();

        progressDialog = new ProgressDialog(VolleyActivity16.this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        //Defining api service
        RequestQueue requestQueue = Volley.newRequestQueue(VolleyActivity16.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, baseUrl, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyerror) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), volleyerror.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers =  new HashMap<String, String>();
                String credentials = enteredusername + ":" + enteredpassword;
                String encoded = "Basic "+ Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", encoded);
                return headers;
            }
        };
        requestQueue.add(stringRequest);

    }
}
