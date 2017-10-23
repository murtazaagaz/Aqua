package com.onesttech.stockmaneger.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.onesttech.stockmaneger.R;
import com.onesttech.stockmaneger.network.MyVolley;
import com.onesttech.stockmaneger.storage.Constants;
import com.onesttech.stockmaneger.storage.ENDPOINTS;
import com.onesttech.stockmaneger.storage.MySharedPrefrences;
import com.onesttech.stockmaneger.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final String TAG = "MUR";
    @BindView(R.id.email)
    EditText mEmailEdit;
    @BindView(R.id.password)
    EditText mPasswordEdit;
    @BindView(R.id.login)
    Button mLoginBtn;
    @BindView(R.id.murtaza)
    TextView mMyName;

    private ProgressDialog pd;
    private RequestQueue mRequestQue;
    private MySharedPrefrences sp;

    private String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        sp = MySharedPrefrences.getInstance(this);
        String steps = sp.getStep(Constants.STEPS);
//        mMyName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String url = "https://www.facebook.com/murtaza.agaz";
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                startActivity(i);
//            }
//        });
        if (steps.equals("step2")) {
            Intent intent = new Intent(LoginActivity.this, RouteListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (steps.equals("step3")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            pd = new ProgressDialog(this);
            pd.setMessage("Processing..");

            mRequestQue = MyVolley.getInstance().getRequestQueue();
            mLoginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = mEmailEdit.getText().toString().trim();
                    String password = mPasswordEdit.getText().toString().trim();

                    if (email.isEmpty() || email.equals("") || password.isEmpty() || password.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please Fill Username and Password", Toast.LENGTH_LONG).show();
                    } else {
                        checkNetworkAndLogin(email, password);
                    }
                }
            });
        }
    }

    private void checkNetworkAndLogin(final String email, final String password) {
        if (Util.isNetworkAvailable()) {
            pd.show();
            StringRequest request = new StringRequest(Request.Method.POST, ENDPOINTS.LOGIN, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    pd.dismiss();
                    Log.d("TAG", "MUR: " + response);
                    // int flag = Integer.parseInt(response);
                    try {
                        JSONObject object = new JSONObject(response);
                        String message = object.getString("message");
                        if (message.contains("1")) {
                            sp.setKey(Constants.UID, object.getString("emp_id"));
                            sp.setKey(Constants.COMPANY_ID, object.getString("company_id"));
                            sp.setStep(Constants.STEPS, "step2");
                            Toast.makeText(LoginActivity.this, "Logged In..", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, RouteListActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid Username/Password", Toast.LENGTH_LONG).show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("TAG", "MUR: Error " + error.getMessage());
                    Toast.makeText(getApplicationContext(), "Something Went Wrong Please Try Again Later.", Toast.LENGTH_LONG).show();
                    pd.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put(Constants.USERNAME, email);
                    params.put(Constants.PASSWORD, password);
                    return params;
                }
            };
            mRequestQue.add(request);
        } else {
            Toast.makeText(this, "No Internet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermistions() {
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[0])
                    ) {
                //Show Information about why you need the permission
                ActivityCompat.requestPermissions(LoginActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);

            } else {
                //just request the permission
                ActivityCompat.requestPermissions(LoginActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }


            sp.setBoolean(permissionsRequired[0], true);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;

                }
            }

            if (allgranted) {

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[0])) {
                ActivityCompat.requestPermissions(LoginActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
            } else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }
}

