package com.onesttech.stockmaneger.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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

public class DetailListActivity extends AppCompatActivity implements View.OnFocusChangeListener, AdapterView.OnItemSelectedListener {
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private String TAG = "DetailListActivity";
    private List<String> mCanNameList;
    private List<String> mTotalDeliverList = new ArrayList<>();
    private List<String> mTotalDamageList = new ArrayList<>();
    private List<String> mTotalLossList = new ArrayList<>();
    private List<String> mTotalCollectList = new ArrayList<>();
    private List<String> mCanIdList = new ArrayList<>();
    private List<View> mCanViewList = new ArrayList<>();
    private List<String> mRateList = new ArrayList<>();
    private List<String> mLossRateList = new ArrayList<>();
    private List<String> mDamageRateList = new ArrayList<>();
    private String mExpRateList;

    private MySharedPrefrences sp;
    private ProgressDialog pd;
    String pos;
    private String mCustomerName, mCustomerId, mOrderStatus, mAddress, mOrderstatus,
            mCanId, mCanName, mTotalCanCollect, mTotalCanDeliver, mDamaeCan, mLostCan;
    private RequestQueue mRequestQue;
    @BindView(R.id.linear)
    LinearLayout containerLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.name)
    TextView mNameTv;
    @BindView(R.id.mobile)
    TextView mMobileTv;
    @BindView(R.id.address)
    TextView mAddressTv;
    @BindView(R.id.status)
    TextView mStatusTv;
    @BindView(R.id.updateBtn)
    Button mUpdateBtn;
    @BindView(R.id.amount)
    EditText mAmountEdit;
    @BindView(R.id.paymentSpinner)
    Spinner mPaymentSpinner;
    @BindView(R.id.check_no)
    EditText mCheckEdit;
    @BindView(R.id.skipBtn)
    Button mSkipBtn;
    private int mCheckDamage;
    private LayoutInflater layoutInflater;
    private int rate;
    private List<String> mPaymentList = new ArrayList<>();
    private String paymentMode, checkNo,finalAmnt;
    private ArrayAdapter<String> mSkipAdapter;
    private List<String> mSkipList = new ArrayList<>();
    private Database db;
    private GPSTracker mGPSTracker;
    private List<String> mOneTimeReturn = new ArrayList<>();
    private List<String> mReasonList = new ArrayList<>();
    private List<String> mReasonIDList = new ArrayList<>();
    private List<String> mModeId;
    private String checkDamageToUpload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ButterKnife.bind(this);
        sp = MySharedPrefrences.getInstance(this);
        mCanNameList = new ArrayList<>();
        mCanViewList = new ArrayList<>();
        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Product Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = new Database(this);
        layoutInflater = (LayoutInflater) (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        mCheckEdit.setVisibility(View.GONE);
        pd = new ProgressDialog(this);
        pd.setMessage("Processing..");
        mGPSTracker = new GPSTracker(this);
        mPaymentList.add("Cash");
        mPaymentSpinner.setOnItemSelectedListener(this);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mPaymentList);
        mPaymentSpinner.setAdapter(adapter);


        if (!getIntent().hasExtra(Constants.POSITION)) {
            finish();
        } else {
            pos = getIntent().getStringExtra(Constants.POSITION);
            mRequestQue = MyVolley.getInstance().getRequestQueue();

            checkNetworkParseList();

        }
        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               checkDataAndUpdate();

            }
        });

        mSkipAdapter = new ArrayAdapter<>(DetailListActivity.this, R.layout.skip_root_textview,R.id.reason, mReasonList);

        mSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showSkipDialoge();
            }
        });


    }

    private void checkNetworkParseList() {
        if (Util.isNetworkAvailable()) {
            pd.show();
            StringRequest request = new StringRequest(Request.Method.POST, ENDPOINTS.SUB_LIST, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    pd.dismiss();

                    parseData(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(), "Some Error Occurred", Toast.LENGTH_LONG).show();
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
            if (!response.equals("") && !response.isEmpty()){
                parseData(response);
            }
            else {
                Toast.makeText(getApplicationContext(),"No Internet Available",Toast.LENGTH_LONG).show();

            }

        }
    }

    private void checkDataAndUpdate() {
        mCanNameList.clear();
        mTotalCollectList.clear();
        mTotalDeliverList.clear();
        mTotalDamageList.clear();
        mTotalLossList.clear();
        List<String> compareList = new ArrayList<>();
        checkNo = mCheckEdit.getText().toString();
        finalAmnt = String.valueOf(calculate());
        boolean fillCheck;
        for (int i = 0; i < mCanViewList.size(); i++) {
            View view = mCanViewList.get(i);
            String collectStr,deliverStr, damageStr,lostStr;
            checkDamageToUpload =  mOneTimeReturn.get(i);
            TextView canName = (TextView) view.findViewById(R.id.name);
            EditText totalCollect = (EditText) view.findViewById(R.id.can_collectValue);
            EditText totalDeliver = (EditText) view.findViewById(R.id.can_deliverValue);
            EditText totalDamged = (EditText) view.findViewById(R.id.damaged_canValue);
            EditText totalLost = (EditText) view.findViewById(R.id.lost_canValue);
            Log.d("TAG", "MUR can collect" + totalCollect.getText().toString());
            collectStr = totalCollect.getText().toString();
            deliverStr = totalDeliver.getText().toString().trim();
            damageStr = totalDamged.getText().toString().trim();
            lostStr = totalLost.getText().toString().trim();
            if (!collectStr.equals("")||!collectStr.isEmpty() && !deliverStr.isEmpty() || !deliverStr.equals("")){
                compareList.add("mur");

            }

            if (collectStr.isEmpty()||collectStr.equals("")){
                collectStr = "0";
            }
            if (deliverStr.isEmpty()||deliverStr.equals("")){
                deliverStr = "0";
            }
            if (damageStr.isEmpty()||deliverStr.equals("")){
                damageStr = "0";
            }
            if (lostStr.equals("")||lostStr.isEmpty()){
                lostStr = "0";
            }

            mCanNameList.add(canName.getText().toString());
            mTotalCollectList.add(collectStr);
            mTotalDeliverList.add(deliverStr);
            mTotalDamageList.add(damageStr);
            mTotalLossList.add(lostStr);
        }
        Log.d(TAG, "MUR check onClick: comapreList "+compareList.size()+" / canId"+mCanIdList.size());

        if (compareList.size() != mCanIdList.size() ) {
            Toast.makeText(getApplicationContext(), "Can Cant Be Blank", Toast.LENGTH_LONG).show();
        }
        else
        {

            if (mCheckEdit.getVisibility() == View.VISIBLE) {
                if (checkNo.isEmpty() || checkNo == "") {
                    Toast.makeText(getApplicationContext(), "Please enter Check No.", Toast.LENGTH_LONG).show();
                } else {
                    updateCan();
                }
            } else {
                updateCan();
            }
        }

    }

    private void showSkipDialoge() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailListActivity.this);
        builder.setTitle("Select Reason to skip");

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog d = builder.create();
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.skip_reason_layout, null, false);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(mSkipAdapter);
        d.setView(view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 final String reasonId = mReasonIDList.get(position);
                Toast.makeText(getApplicationContext(),reasonId,Toast.LENGTH_LONG).show();

                StringRequest request = new StringRequest(Request.Method.POST, ENDPOINTS.NO_ACTION, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "MUR SKIP onResponse: "+response);
                        if (response.contains("1")){
                            Toast.makeText(getApplicationContext(),"No Action!",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(DetailListActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Something went wrong! try again later",Toast.LENGTH_LONG).show();

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"Something went wrong! try again later",Toast.LENGTH_LONG).show();

                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("reasion_id", reasonId);
                        params.put("customer_id",mCustomerId);
                        params.put("logid",sp.getKey(Constants.LOGID));
                        params.put("emp_id",sp.getKey(Constants.UID));
                        params.put("company_id",sp.getKey(Constants.COMPANY_ID));
                        params.put("order_status","No Action");
                        params.put(Constants.COMPANY_ID, sp.getKey(Constants.COMPANY_ID));

                        return params;
                    }
                };
                mRequestQue.add(request);


            }
        });

        d.show();
        d.getButton(d.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#004D40"));

    }


    private void addView(String name, String total, String totalDel, String damahed, String lost, String id) {
        View view = layoutInflater.inflate(R.layout.detail_can_layout, null, false);
        view.setTag(mCanViewList.size());
        TextView canName = (TextView) view.findViewById(R.id.name);
        EditText totalCollect = (EditText) view.findViewById(R.id.can_collect);
        EditText totalDeliver = (EditText) view.findViewById(R.id.can_deliver);
        EditText totalDamged = (EditText) view.findViewById(R.id.damaged_can);
        EditText totalLost = (EditText) view.findViewById(R.id.lost_can);
        EditText DamgedValue = (EditText) view.findViewById(R.id.damaged_canValue);
        EditText LostValue = (EditText) view.findViewById(R.id.lost_canValue);
        EditText totalCollectValue = (EditText) view.findViewById(R.id.can_collectValue);
        EditText totalDeliverValue = (EditText) view.findViewById(R.id.can_deliverValue);
        TextView totalDamgedTitle = (TextView) view.findViewById(R.id.damage_title);
        TextView totalLostTitle = (TextView) view.findViewById(R.id.lost_title);

        totalCollectValue.setOnFocusChangeListener(this);
        totalDeliverValue.setOnFocusChangeListener(this);
        DamgedValue.setOnFocusChangeListener(this);
        LostValue.setOnFocusChangeListener(this);

        if (mCheckDamage == 1) {
            totalLost.setVisibility(View.VISIBLE);
            totalDamgedTitle.setVisibility(View.VISIBLE);
            totalLostTitle.setVisibility(View.VISIBLE);
            DamgedValue.setVisibility(View.VISIBLE);
            LostValue.setVisibility(View.VISIBLE);
            totalDamged.setVisibility(View.VISIBLE);
        } else {
            totalDamged.setVisibility(View.GONE);
            totalLost.setVisibility(View.GONE);
            totalDamgedTitle.setVisibility(View.GONE);
            totalLostTitle.setVisibility(View.GONE);
            DamgedValue.setVisibility(View.GONE);
            LostValue.setVisibility(View.GONE);
        }
        totalCollect.setEnabled(false);
        totalDeliver.setEnabled(false);
        totalDamged.setEnabled(false);
        totalLost.setEnabled(false);
        canName.setText(name);
        totalCollect.setText(total);
        totalDeliver.setText(totalDel);
        totalDamged.setText(damahed);
        totalLost.setText(lost);
        mCanIdList.add(id);
        mCanViewList.add(view);
        containerLayout.addView(view);
    }


    private void parseData(String response) {
        Log.d("TAG", "MUR parseData: " + pos);
        Log.d("TAG", "MUR parseData: " + response);
        try {
            JSONArray jsonArray = new JSONArray(response);

            JSONObject object = jsonArray.getJSONObject(Integer.valueOf(pos));
            Log.d("TAG", "MUR :DETAIL LIST" + object);
            mNameTv.setText(object.getString("customer_name"));
            mMobileTv.setText(object.getString("mobile_no_1"));
            mAddressTv.setText(object.getString("address"));
            mCustomerId = object.getString("customer_id");
            mStatusTv.setText(object.getString("order_status"));
            mAmountEdit.setText(calculate() + "");
            mAmountEdit.setEnabled(false);
            mExpRateList = object.getInt("exp_amount_collect") + "";

            JSONArray array = object.getJSONArray(Constants.CAN_TYPE);
            for (int i = 0; i < array.length(); i++) {

                Log.d("TAG", "MUR array:");
                JSONObject canObj = array.getJSONObject(i);
                Log.d("TAG", "MUR array:" + canObj.toString());
                mCheckDamage = canObj.getInt("onetime_return");
                mOneTimeReturn.add(canObj.getString("onetime_return"));
                mRateList.add(canObj.getString("rate_per_cane"));
                mLossRateList.add(canObj.getString("lost_cane_fine"));
                mDamageRateList.add(canObj.getString("damaged_cane_fine"));
                addView(canObj.getString("cane_type"), canObj.getString("total_cane_collect"),
                        canObj.getString("total_cane_deliver"), canObj.getString("damaged_cane"),
                        canObj.getString("lost_cane"), canObj.getString("cane_stock_master_id"));

//                View view = mCanViewList.get(i);
//                TextView canName = (TextView) view.findViewById(R.id.name);
//                EditText totalCan = (EditText) view.findViewById(R.id.can_collect);
//                EditText totalDeliver = (EditText) view.findViewById(R.id.can_deliver);
//                EditText totalDamged = (EditText) view.findViewById(R.id.damaged_can);
//                EditText totalLost = (EditText) view.findViewById(R.id.lost_can);
//                EditText DamgedValue = (EditText) view.findViewById(R.id.damaged_canValue);
//                EditText LostValue = (EditText) view.findViewById(R.id.lost_canValue);
//
//                TextView totalDamgedTitle = (TextView) view.findViewById(R.id.damage_title);
//                TextView totalLostTitle = (TextView) view.findViewById(R.id.lost_title);
//
//                mCheckDamage = canObj.getString("exp_amount_collect");
//                if (mCheckDamage.equals("0")) {
//                    totalLost.setVisibility(View.VISIBLE);
//                    totalDamgedTitle.setVisibility(View.VISIBLE);
//                    totalLostTitle.setVisibility(View.VISIBLE);
//                    DamgedValue.setVisibility(View.VISIBLE);
//                    LostValue.setVisibility(View.VISIBLE);
//                }
//                else {
//                    totalLost.setVisibility(View.GONE);
//                    totalDamgedTitle.setVisibility(View.GONE);
//                    totalLostTitle.setVisibility(View.GONE);
//                    DamgedValue.setVisibility(View.GONE);
//                    LostValue.setVisibility(View.GONE);
//                }
//                totalCan.setEnabled(false);
//                totalDeliver.setEnabled(false);
//                totalDamged.setEnabled(false);
//                totalLost.setEnabled(false);
//                canName.setText(canObj.getString("cane_type"));
//                totalCan.setText(canObj.getString("total_cane_collect"));
//                totalDeliver.setText(canObj.getString("total_cane_deliver"));
//                totalDamged.setText(canObj.getString("damaged_cane"));
//                totalLost.setText(canObj.getString("lost_cane"));
//                mCanIdList.add(canObj.getString("cane_stock_master_id"));

            }
            JSONArray paymentArray = object.getJSONArray("paymentmode");
            if (paymentArray.length() > 0) {
                for (int i = 1; i < paymentArray.length(); i++) {
                    JSONObject obj = paymentArray.getJSONObject(i);
                    mPaymentList.add(obj.getString("mode"));
                }
            }
            JSONArray reasonArray = object.getJSONArray("canclation_reasion");
            for (int i = 0; i < reasonArray.length(); i++) {
                JSONObject reasonObj = reasonArray.getJSONObject(i);
                mReasonList.add(reasonObj.getString("reasion"));
                mReasonIDList.add(reasonObj.getString("reasion_id"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();

        }

    }

    private int calculate() {
        int amnt = 0;
        for (int i = 0; i < mCanViewList.size(); i++) {
            try {
                String deliverStr,damageStr,lostStr;
                View view = mCanViewList.get(i);
                rate = Integer.parseInt(mRateList.get(i));
                int damageRate = Integer.parseInt(mDamageRateList.get(i));
                int lossRate = Integer.parseInt(mLossRateList.get(i));
                int expRate = Integer.parseInt(mExpRateList);
                EditText totalDeliver = (EditText) view.findViewById(R.id.can_deliverValue);
                EditText totalDamged = (EditText) view.findViewById(R.id.damaged_canValue);
                EditText totalLost = (EditText) view.findViewById(R.id.lost_canValue);
                deliverStr = totalDeliver.getText().toString().trim();
                damageStr = totalDamged.getText().toString().trim();
                lostStr = totalLost.getText().toString().trim();

                if (deliverStr.isEmpty()||deliverStr.equals("")){
                    deliverStr = "0";
                }
                if (damageStr.isEmpty()||deliverStr.equals("")){
                    damageStr = "0";
                }
                if (lostStr.equals("")||lostStr.isEmpty()){
                    lostStr = "0";
                }

                int deliver = Integer.parseInt(deliverStr);
                int damaged = Integer.parseInt(damageStr);
                int lost = Integer.parseInt(lostStr);
                amnt += (rate * deliver) + (lossRate * lost) + (damageRate * damaged) + expRate;
            } catch (NumberFormatException e) {

            }
        }
        mAmountEdit.setText(String.valueOf(amnt));
        return amnt;
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        calculate();
    }


    private void updateCan() {
        if (Util.isNetworkAvailable()) {
            pd.show();
            final double longitude, latitude;
            boolean permision;
            permision = mGPSTracker.canGetLocation();
            if (permision) {
                longitude = mGPSTracker.getLongitude();
                latitude = mGPSTracker.getLatitude();
                Log.d("TAG", "MUR updateCan: loc : " + latitude + " // " + longitude + "");


                StringRequest request = new StringRequest(Request.Method.POST, "http://emphasiscorp.org/aquaroster/app/update_customer_order.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TAG", "MUR update can Res" + response);
                        if (response.contains("1")){
                            Toast.makeText(getApplicationContext(),"Product Details Updated.",Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(DetailListActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();

                        }

                        pd.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();
                        Log.d("TAG", "MUR update can error");
                        String errorMessage = MyVolley.handleVolleyError(error);
                        error.printStackTrace();
                        Log.d(TAG, "MUR onErrorResponse: "+error.getMessage());
                        pd.dismiss();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();


                        params.put("customer_id", mCustomerId);

                        Log.d(TAG, "MUR getParams Check: CustomerId" +mCustomerId);
                        params.put("latitude", latitude + "");
                        params.put("longitude", longitude + "");
                        Log.d(TAG, "MUR getParams Check: lat Lon" +latitude+"/"+longitude+"");

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
                        Log.d(TAG, "MUR getParams Check: Time" +ts);


                        params.put("order_status", "Deliver & Collect");
                       //
                       params.put("amount", finalAmnt );
                        //Log.d(TAG, "MUR getParams Check: amnt" +amnt+"");

                        params.put(Constants.COMPANY_ID, sp.getKey(Constants.COMPANY_ID));
                        Log.d(TAG, "MUR getParams Check: compId" +sp.getKey(Constants.COMPANY_ID));

                        params.put("payment_mode", paymentMode);
                        Log.d(TAG, "MUR getParams Check: PMode" +paymentMode);
                        params.put(Constants.UID,sp.getKey(Constants.UID));
                        params.put(Constants.LOGID,sp.getKey(Constants.LOGID));

                        if (paymentMode.equals("1")) {
                            params.put("cheque_no", "0");
                        } else {
                            params.put("cheque_no", checkNo);
                            Log.d(TAG, "MUR getParams Check: PNo" +checkNo);
                        }
                        Log.d("TAG", "MUR getParams: " + mTotalLossList.toString());
                        Log.d("TAG", "MUR getParams: " + mTotalLossList.get(0));
                        return params;
                    }
                };
                mRequestQue.add(request);
            } else {
                Toast.makeText(getApplicationContext(),"Cant get Location",Toast.LENGTH_LONG).show();
                updateCan();
            }
        } else {
            //to Store arrayLists in Sql
            JSONObject canIdJson = new JSONObject();
            JSONObject canNameJson = new JSONObject();
            JSONObject collectJson = new JSONObject();
            JSONObject deliverJson = new JSONObject();
            JSONObject damageJson = new JSONObject();
            JSONObject lostJson = new JSONObject();
            JSONObject oneTimeReturnJson = new JSONObject();

            String canIdarray, canNameArray, collectArray, deliverArray, damageArray, lostArray,oneTimeReturnArray;
            try {
//
                canIdJson.put("mCanIdList", new JSONArray(mCanIdList));
                canNameJson.put("mCanNameList", new JSONArray(mCanNameList));
                collectJson.put("mTotalCollectList", new JSONArray(mTotalCollectList));
                deliverJson.put("mTotalDeliverList", new JSONArray(mTotalDeliverList));
                damageJson.put("mTotalDamageList", new JSONArray(mTotalDamageList));
                lostJson.put("mTotalLossList", new JSONArray(mTotalLossList));
                oneTimeReturnJson.put("mOneTimeReturn",new JSONArray(mOneTimeReturn));
                Log.d(TAG, "MUR Json updateCan: " + canIdJson);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("TAG", "MUR updateCan: Json nai chala ");
            }
            canIdarray = canIdJson.toString();
            canNameArray = canNameJson.toString();
            collectArray = collectJson.toString();
            deliverArray = deliverJson.toString();
            damageArray = damageJson.toString();
            lostArray = lostJson.toString();
            oneTimeReturnArray = oneTimeReturnJson.toString();

            Double time = (double) (System.currentTimeMillis());

            String ts = time.toString();
            long result = db.saveCanData(sp.getKey(Constants.LOGID), mCustomerId, canIdarray, canNameArray, collectArray, deliverArray, damageArray, lostArray, "delivered", String.valueOf(calculate()), paymentMode, checkNo, ts, oneTimeReturnArray);
            if (result == -1) {
                Toast.makeText(getApplicationContext(), "Cant Save Data Please try again later..", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(DetailListActivity.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "No Internet Available! Data Saved Connect to a network to upload.", Toast.LENGTH_LONG).show();

            }
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("TAG", "MUR :onItemSelected: " + position);
        paymentMode = (position + 1) + "";
        if (position > 0) {
            mCheckEdit.setVisibility(View.VISIBLE);
        } else {
            mCheckEdit.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d("MUR", "MUR onNothingSelected: ");
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

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(DetailListActivity.this, REQUEST_CHECK_SETTINGS);
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