package com.onesttech.stockmaneger.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
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
import com.onesttech.stockmaneger.adapter.StockListAdapter;
import com.onesttech.stockmaneger.network.MyVolley;
import com.onesttech.stockmaneger.pojo.OfflineDataPojo;
import com.onesttech.stockmaneger.pojo.StockListPojo;
import com.onesttech.stockmaneger.storage.Constants;
import com.onesttech.stockmaneger.storage.Database;
import com.onesttech.stockmaneger.storage.ENDPOINTS;
import com.onesttech.stockmaneger.storage.MySharedPrefrences;
import com.onesttech.stockmaneger.util.GPSTracker;
import com.onesttech.stockmaneger.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CHECK_SETTINGS = 1;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recycleView)
    RecyclerView mRecycleView;
    @BindView(R.id.search)
    EditText mSearch;
    private RequestQueue mRequestQue;
    private List<StockListPojo> mList = new ArrayList<>();
    private MySharedPrefrences sp;
    private ProgressDialog pd;
    private StockListAdapter adapter;



    private Database database;
    private GPSTracker mGPSTracker;
    private List<String> mCanNameList = new ArrayList<>();
    private List<String> mTotalDeliverList = new ArrayList<>();
    private List<String> mTotalDamageList = new ArrayList<>();
    private List<String> mTotalLossList = new ArrayList<>();
    private List<String> mTotalCollectList = new ArrayList<>();
    private List<String> mCanIdList = new ArrayList<>();
    private List<String> mOneTimeReturn = new ArrayList<>();


    private String amount;
    private String orderstatus;
    private String time;
    private String lat;
    private String lon;
    private String paymentMode;
    private String checkNo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //this.deleteDatabase("can.db");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Stock Manager");
        pd = new ProgressDialog(this);
        pd.setTitle("Processing..");
        mRequestQue = MyVolley.getInstance().getRequestQueue();
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        sp = MySharedPrefrences.getInstance(this);
        adapter = new StockListAdapter(mList, this);
        database = new Database(this);

        mGPSTracker = new GPSTracker(this);

        uploadOfflineData();


        if (Util.isNetworkAvailable()) {
            pd.show();
            final StringRequest request = new StringRequest(Request.Method.POST, ENDPOINTS.SUB_LIST, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    pd.dismiss();

                    try {
                        JSONArray object = new JSONArray(response);
                        for (int i = 0; i < object.length(); i++) {
                            sp.setKey("res", response);
                            JSONObject data = new JSONObject(String.valueOf(object.get(i)));
                            StockListPojo pojo = new StockListPojo();
                            pojo.setAddress(data.getString("address"));
                            pojo.setName(data.getString("customer_name"));
                            pojo.setStatus(data.getString("order_status"));
                            mList.add(pojo);
                        }
                        Log.d("TAG", "MUR" + mList.get(0).getStatus());
                        adapter = new StockListAdapter(mList, MainActivity.this);
                        adapter.notifyDataSetChanged();
                        mRecycleView.setAdapter(adapter);
                        Log.d(TAG, "MUR :onResponse: " + response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();

                    }
                    Log.d(TAG, "MUR :onResponse: " + response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pd.dismiss();
                    Log.d(TAG, "onErrorResponse: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();

                    error.getStackTrace();

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put(Constants.LOGID, sp.getKey(Constants.LOGID));
                    params.put(Constants.COMPANY_ID, sp.getKey(Constants.COMPANY_ID));
                    return params;
                }
            };
            mRequestQue.add(request);
        } else {
            String response = sp.getKey("res");
            if (response != null || !response.isEmpty()) {
                try {
                    JSONArray object = new JSONArray(response);
                    for (int i = 0; i < object.length(); i++) {
                        sp.setKey("res", response);
                        JSONObject data = new JSONObject(String.valueOf(object.get(i)));
                        StockListPojo pojo = new StockListPojo();
                        pojo.setAddress(data.getString("address"));
                        pojo.setName(data.getString("customer_name"));
                        pojo.setStatus(data.getString("order_status"));
                        mList.add(pojo);
                    }
                    adapter = new StockListAdapter(mList, MainActivity.this);
                    Log.d("TAG", "MUR" + mList.get(0).getStatus());
                    mRecycleView.setAdapter(adapter);
                    Log.d(TAG, "MUR :onResponse: " + response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getApplicationContext(), "No Internet Available", Toast.LENGTH_LONG).show();
            }
        }
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString().toLowerCase());

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        finish();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }

    private void uploadOfflineData() {
        Log.d(TAG, "MUR uploadOfflineData: chal gaya");
        List<OfflineDataPojo> list = database.getOfflineData(sp.getKey(Constants.LOGID));


        for (int i = 0; i < list.size(); i++) {
            mCanNameList.clear();
            mCanIdList.clear();
            mTotalCollectList.clear();
            mTotalDeliverList.clear();
            mTotalDamageList.clear();
            mTotalLossList.clear();

            OfflineDataPojo pojo = list.get(i);
            Log.d(TAG, "MUR uploadOfflineData: loop chal gaya");
            try {
                Log.d(TAG, "MUR uploadOfflineData: can name" + pojo.getCanName());

                JSONObject canName = new JSONObject(pojo.getCanName());
                JSONArray canNameArray = canName.getJSONArray("mCanNameList");


                JSONObject canCollect = new JSONObject(pojo.getCanCollect());
                JSONArray canCollectArray = canCollect.getJSONArray("mTotalCollectList");

                JSONObject canDeliver = new JSONObject(pojo.getCanDeliver());
                JSONArray canDeliverArray = canDeliver.getJSONArray("mTotalDeliverList");

                JSONObject canDamage = new JSONObject(pojo.getCanDamage());
                JSONArray canDamageArray = canDamage.getJSONArray("mTotalDamageList");

                JSONObject canLost = new JSONObject(pojo.getCanLost());
                JSONArray canLostArray = canLost.getJSONArray("mTotalLossList");

                JSONObject canId = new JSONObject(pojo.getCanId());
                JSONArray canIdArray = canId.getJSONArray("mCanIdList");

                JSONObject oneTimeReturn = new JSONObject(pojo.getOneTimeReturn());
                JSONArray oneTImeReturnArray = oneTimeReturn.getJSONArray("mOneTimeReturn");

                String customerId = pojo.getCustomerId();
                orderstatus = pojo.getOrderStatus();
                amount = pojo.getAmount();
                checkNo = pojo.getCheck();
                paymentMode = pojo.getPaymentMode();
                time = pojo.getTime();

                for (int j = 0; j < canNameArray.length(); j++) {
                    Log.d(TAG, "MUR uploadOfflineData: loop 2 ");
                    Log.d(TAG, "MUR uploadOfflineData: " + canNameArray.getString(j));
                    mCanNameList.add(canNameArray.getString(j));
                    mCanIdList.add(canIdArray.getString(j));
                    mTotalCollectList.add(canCollectArray.getString(j));
                    mTotalDeliverList.add(canDeliverArray.getString(j));
                    mTotalDamageList.add(canDamageArray.getString(j));
                    mTotalLossList.add(canLostArray.getString(j));
                    mOneTimeReturn.add(oneTImeReturnArray.getString(j));
                }
                Log.d(TAG, "MUR uploadOfflineData: canName " + mCanNameList.toString());
                if (Util.isNetworkAvailable()) {
                    upDateData(customerId);
                }

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(),"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();

            }


        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.end_trip:
                showEndTripAlertDialoge();
                break;
            case R.id.logout:
                if (Util.isNetworkAvailable()) {
                    showLogOutAlertDialoge();
                }
                else {
                    Toast.makeText(this,"No Internet! Try again Later",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);


    }



    private void upDateData(final String customerID) {
        if (Util.isNetworkAvailable()) {
            pd.show();
            final double longitude, latitude;
            boolean permision;
            permision = mGPSTracker.canGetLocation();
                longitude = mGPSTracker.getLongitude();
                latitude = mGPSTracker.getLatitude();
                Log.d("TAG", "MUR updateCan: loc : " + latitude + " // " + longitude + "");


                StringRequest request = new StringRequest(Request.Method.POST, "http://emphasiscorp.org/aquaroster/app/update_customer_order.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TAG", "MUR update Offline can Res" + response);
                        boolean deleteBool = database.deleteData(customerID);
                        Log.d(TAG, "MUR OFF UPDATE RETURN : " + deleteBool);
                        pd.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TAG", "MUR update Offline can error");
                        String errorMessage = MyVolley.handleVolleyError(error);
                        Toast.makeText(getApplicationContext(),"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();

                        error.printStackTrace();
                        Log.d(TAG, "MUR onErrorResponse: " + error.getMessage());
                        pd.dismiss();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();


                        params.put("customer_id", customerID);

                        Log.d(TAG, "MUR getParams Check: CustomerId" + customerID);
                        params.put("latitude", latitude + "");
                        params.put("longitude", longitude + "");
                        Log.d(TAG, "MUR getParams Check: lat Lon" + latitude + "/" + longitude + "");

                        params.put("cane_stock_master_id", mCanIdList.toString());
                        Log.d(TAG, "MUR getParams:= " + "cane_stock_master_id" + ":" + mCanIdList.toString());
                        // params.put("cane_type", mCanNameList.toString());
                        Log.d(TAG, "MUR getParams Check: canName" + mCanNameList.toString());

                        params.put("collect", mTotalCollectList.toString());
                        Log.d(TAG, "MUR getParams Check: cancollect" + mTotalCollectList.toString());
                        params.put("deliver", mTotalDeliverList.toString());
                        Log.d(TAG, "MUR getParams Check: canDel" + mTotalDeliverList.toString());
                        params.put("damage", mTotalDamageList.toString());
                        Log.d(TAG, "MUR getParams Check: canDam" + mTotalDamageList.toString());
                        params.put("lost", mTotalLossList.toString());
                        Log.d(TAG, "MUR getParams Check: canLos" + mTotalLossList.toString());

                        params.put("onetime_return", mOneTimeReturn.toString());
                        Double time = (double) (System.currentTimeMillis());
                        String ts = time.toString();

                        params.put("supply_time", ts);
                        Log.d(TAG, "MUR getParams Check: Time" + ts);


                        params.put("order_status", "Deliver & Collect");
                        //
                        params.put("amount", amount);
                        //Log.d(TAG, "MUR getParams Check: amnt" +amnt+"");

                        params.put(Constants.COMPANY_ID, sp.getKey(Constants.COMPANY_ID));
                        Log.d(TAG, "MUR getParams Check: compId" + sp.getKey(Constants.COMPANY_ID));

                        params.put("payment_mode", paymentMode);
                        Log.d(TAG, "MUR getParams Check: PMode" + paymentMode);
                        params.put(Constants.UID, sp.getKey(Constants.UID));
                        params.put(Constants.LOGID, sp.getKey(Constants.LOGID));


                        params.put("cheque_no", checkNo);
                        Log.d(TAG, "MUR getParams Check: PNo" + checkNo);
                        Log.d("TAG", "MUR getParams: " + mTotalLossList.toString());
                        Log.d("TAG", "MUR getParams: " + mTotalLossList.get(0));
                        return params;
                    }
                };
                mRequestQue.add(request);

        }
    }

    private void showEndTripAlertDialoge() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("End Trip");
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.alertdialge_layout, null, false);
        final EditText editText = (EditText) view.findViewById(R.id.meter);
        //Vie w view = new View(mContext);

        builder.setView(view);
        builder.setPositiveButton("Close Trip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String reading = editText.getText().toString();
                if (reading.isEmpty() || reading.equals("")) {
                    Toast.makeText(MainActivity.this, "Please enter Metre Reading to Proceed ", Toast.LENGTH_LONG).show();
                } else {
                    if (Util.isNetworkAvailable()) {
                        pd.show();
                        StringRequest request = new StringRequest(Request.Method.POST, ENDPOINTS.END_TRIP, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.contains("1")){
                                    Toast.makeText(getApplicationContext(),"Trip Closed.",Toast.LENGTH_LONG).show();
                                    sp.setKey(Constants.STEPS,"step2");
                                    sp.setKey(Constants.LOGID,"");
                                    sp.setKey(Constants.CHALAN_NO,"");
                                    sp.setKey("res","");
                                    database.deleteTable();
                                    pd.dismiss();
                                    Intent intent = new Intent(MainActivity.this,RouteListActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                }
                                else {
                                    pd.dismiss();
                                    Toast.makeText(MainActivity.this, "Something went wrong try again later.", Toast.LENGTH_LONG).show();
                                }
                                Log.d(TAG, "MUR End Trip onResponse: "+response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                pd.dismiss();
                                Toast.makeText(getApplicationContext(),"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();

                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params = new HashMap<>();
                                params.put(Constants.CHALAN_NO,sp.getKey(Constants.CHALAN_NO));
                                params.put(Constants.COMPANY_ID,sp.getKey(Constants.COMPANY_ID));
                                params.put(Constants.UID,sp.getKey(Constants.LOGID));
                                params.put(Constants.LOGID,sp.getKey(Constants.LOGID));
                                params.put("end_meter_reading",reading);
                                return params;
                            }
                        };
                        mRequestQue.add(request);

                    } else {
                        Toast.makeText(MainActivity.this, "No Internet!", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog d = builder.create();
        d.show();
        d.getButton(d.BUTTON_POSITIVE).setTextColor(Color.parseColor("#004D40"));
        d.getButton(d.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#004D40"));

    }
    private void showLogOutAlertDialoge() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you Sure?");
        builder.setPositiveButton("Yes! I am Sure", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sp.setKey(Constants.STEPS,"step1");
                sp.setKey(Constants.UID,"");
                sp.setKey(Constants.CHALAN_NO,"");
                sp.setKey(Constants.LOGID,"");
                sp.setKey(Constants.COMPANY_ID,"");
                sp.setKey("res","");
                database.deleteTable();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
        builder.setNegativeButton("No!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog d = builder.create();
        d.show();
        d.getButton(d.BUTTON_POSITIVE).setTextColor(Color.parseColor("#004D40"));
        d.getButton(d.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#004D40"));

    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        final PendingResult<LocationSettingsResult> result1 = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result1.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;


                }
            }
        });


    }



}
