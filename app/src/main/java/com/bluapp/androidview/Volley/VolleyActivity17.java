package com.bluapp.androidview.Volley;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bluapp.androidview.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class VolleyActivity17 extends AppCompatActivity {
    private RecyclerView list;
    private ProgressDialog progressDialog;
    private String baseUrl;
    private List<VolleyGetResponse> responseList;
    private adapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volley17);
        list = (RecyclerView)findViewById(R.id.list);
        baseUrl = "https://dummy.restapiexample.com/api/v1/employees";
        responseList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(layoutManager);
        recyclerAdapter = new adapter(VolleyActivity17.this, responseList);
        list.addItemDecoration(new DividerItemDecoration(VolleyActivity17.this, DividerItemDecoration.VERTICAL));
        list.setAdapter(recyclerAdapter);
        fetchData();
    }

    HurlStack hurlStack = new HurlStack() {
        @Override
        protected HttpURLConnection createConnection(URL url) throws IOException {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
            try {
                httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory());
                httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return httpsURLConnection;
        }
    };

    private void fetchData(){
        progressDialog = new ProgressDialog(VolleyActivity17.this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        //Defining api service
        RequestQueue requestQueue = Volley.newRequestQueue(VolleyActivity17.this, hurlStack);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, baseUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for(int i=0; i<response.length(); i++){
                    try{
                        JSONObject jsonObject = response.getJSONObject(i);
                        VolleyGetResponse volleyGetResponse = new VolleyGetResponse();
                        volleyGetResponse.setId(jsonObject.getString("id"));
                        volleyGetResponse.setEmployeeName(jsonObject.getString("employee_name"));
                        volleyGetResponse.setEmployeeSalary(jsonObject.getString("employee_salary"));
                        volleyGetResponse.setEmployeeAge(jsonObject.getString("employee_age"));
                        volleyGetResponse.setProfileImage(jsonObject.getString("profile_image"));
                        progressDialog.dismiss();
                        responseList.add(volleyGetResponse);
                        recyclerAdapter.setEmployeeList(responseList);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyerror){
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), volleyerror.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify("localhost", session);
            }
        };
    }

    private TrustManager[] getWrappedTrustManagers(TrustManager[] trustManagers) {
        final X509TrustManager originalTrustManager = (X509TrustManager) trustManagers[0];
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return originalTrustManager.getAcceptedIssuers();
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkClientTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkClientTrusted", e.toString());
                        }
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkServerTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkServerTrusted", e.toString());
                        }
                    }
                }
        };
    }

    private SSLSocketFactory getSSLSocketFactory()
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = getResources().openRawResource(R.raw.my_cert); // this cert file stored in \app\src\main\res\raw folder path
        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();
        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);
        TrustManager[] wrappedTrustManagers = getWrappedTrustManagers(tmf.getTrustManagers());
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, wrappedTrustManagers, null);

        return sslContext.getSocketFactory();
    }

    private class VolleyGetResponse{
        private String profileImage;
        private String employeeName;
        private String employeeSalary;
        private String id;
        private String employeeAge;

        public void setProfileImage(String profileImage){
            this.profileImage = profileImage;
        }
        public String getProfileImage(){
            return profileImage;
        }
        public void setEmployeeName(String employeeName){
            this.employeeName = employeeName;
        }
        public String getEmployeeName(){
            return employeeName;
        }
        public void setEmployeeSalary(String employeeSalary){
            this.employeeSalary = employeeSalary;
        }
        public String getEmployeeSalary(){
            return employeeSalary;
        }
        public void setId(String id){
            this.id = id;
        }
        public String getId(){
            return id;
        }
        public void setEmployeeAge(String employeeAge){
            this.employeeAge = employeeAge;
        }
        public String getEmployeeAge(){
            return employeeAge;
        }
    }

    private class adapter extends RecyclerView.Adapter<adapter.myViewHolder> {
        Context context;
        List<VolleyGetResponse> responseList;

        public adapter(Context context, List<VolleyGetResponse> data) {
            this.context = context;
            this.responseList = data;
        }

        public void setEmployeeList(List<VolleyGetResponse> resList) {
            this.responseList = resList;
            notifyDataSetChanged();
        }

        @Override
        public adapter.myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.volley_adapter1, parent, false);
            return new myViewHolder(view);
        }

        @Override
        public void onBindViewHolder(adapter.myViewHolder holder, int position) {
            holder.name.setText(responseList.get(position).getEmployeeName());
            holder.salary.setText(responseList.get(position).getEmployeeSalary());
            holder.age.setText(responseList.get(position).getEmployeeAge());
        }

        @Override
        public int getItemCount() {
            if(responseList != null){
                return responseList.size();
            }
            return 0;

        }

        public class myViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView salary;
            TextView age;

            public myViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.Name);
                salary = (TextView) itemView.findViewById(R.id.Salary);
                age = (TextView) itemView.findViewById(R.id.Age);
            }
        }
    }
}
