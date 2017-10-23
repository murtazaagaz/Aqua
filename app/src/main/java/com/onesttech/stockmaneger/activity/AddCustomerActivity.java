package com.onesttech.stockmaneger.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.onesttech.stockmaneger.R;
import com.onesttech.stockmaneger.network.MyVolley;
import com.onesttech.stockmaneger.storage.ENDPOINTS;
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


public class AddCustomerActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.name)
    EditText mNameEdit;
    @BindView(R.id.mobile)
    EditText mMobileEdit;
    @BindView(R.id.area)
    EditText mAreaEdit;
    @BindView(R.id.city)
    EditText mCityEdit;
    @BindView(R.id.product)
    EditText mProductEdit;
    @BindView(R.id.rate)
    EditText mRateEdit;
    @BindView(R.id.security_depo)
    EditText mSecurityEdit;
    @BindView(R.id.gst)
    EditText mGstEdit;
    @BindView(R.id.address)
    EditText mAddressEdit;
    @BindView(R.id.amount)
    EditText mAmountPaidEdit;
    @BindView(R.id.check_no)
    EditText mChequeEdit;
    @BindView(R.id.paymentSpinner)
    Spinner mPaySpinner;
    @BindView(R.id.add)
    Button mAddBtn;

    private ArrayAdapter<String> productAdapter;

    private String name, mobile, area, city, product, rate,
    address,security,gst,amountPaid, cheque,paymentMode;
    private List<String> idList,nameList,returnList;
    private List<String> areaNameList = new ArrayList<>();
    private List<String> areaIdList = new ArrayList<>();
    private List<String> payNamelist = new ArrayList<>();
    private List<String> payIdList = new ArrayList<>();
    private RequestQueue mRequestQue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Add Customer");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        idList = new ArrayList<>();
        nameList= new ArrayList<>();
        returnList = new ArrayList<>();

        mRequestQue = MyVolley.getInstance().getRequestQueue();
        checkNetworkAndCallData();

        mProductEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    if (Util.isNetworkAvailable()) {
                        if (!nameList.isEmpty()) {
                            initiateAdapter(nameList);
                            selectDialoge("Select Product",nameList);
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Empty",Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"No Internet!",Toast.LENGTH_LONG).show();

                    }
                }
            }
        });
        mProductEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isNetworkAvailable()) {
                    if (!nameList.isEmpty()) {
                        initiateAdapter(nameList);
                        selectDialoge("Select Product",nameList);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Empty",Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"No Internet!",Toast.LENGTH_LONG).show();

                }
            }
        });
        mAreaEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    if (Util.isNetworkAvailable()) {
                        if (!areaNameList.isEmpty()) {
                            initiateAdapter(areaNameList);
                            selectDialoge("Select Area",areaNameList);
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Empty",Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"No Internet!",Toast.LENGTH_LONG).show();

                    }
                }
            }
        });

        mAreaEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isNetworkAvailable()) {
                    if (!areaNameList.isEmpty()) {
                        initiateAdapter(areaNameList);
                        selectDialoge("Select Area",areaNameList);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"No Internet!",Toast.LENGTH_LONG).show();

                }
            }
        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, payNamelist);
        mPaySpinner.setAdapter(adapter);


        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDataAndAddCustomer();
            }
        });

    }

    private void checkNetworkAndCallData() {
        if (Util.isNetworkAvailable()){
            final StringRequest request = new StringRequest(Request.Method.POST, ENDPOINTS.CUSTOMER_DATA, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {

                        JSONArray mainArray = new JSONArray(response);
                        JSONObject object = mainArray.getJSONObject(0);

                        JSONArray canArray = object.getJSONArray("cantype");
                        idList.clear();
                        returnList.clear();
                        for (int i = 0; i < canArray.length(); i++) {
                            JSONObject canObj = canArray.getJSONObject(i);
                            idList.add(canObj.getString("cane_stock_master_id"));
                            nameList.add(canObj.getString("cane_type"));
                            returnList.add(canObj.getString("onetime_return"));
                        }
                        JSONArray areaArray = object.getJSONArray("area");
                        for (int i = 0; i < areaArray.length(); i++) {
                            JSONObject areaObj = areaArray.getJSONObject(i);
                            areaNameList.add(areaObj.getString("area_name"));
                            areaIdList.add(areaObj.getString("customer_area"));
                        }

                        JSONArray payArray = object.getJSONArray("payment_mode");
                        for (int i = 0; i < payArray.length(); i++) {
                            JSONObject payObj = payArray.getJSONObject(i);
                            payNamelist.add(payObj.getString("payment_mode"));
                            payIdList.add(payObj.getString("paymentmode"));
                        }





                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("emp_id","5");
                    params.put("company_id","2");
                    return params;
                }
            };
            mRequestQue.add(request);
        }
        else {
            Toast.makeText(getApplicationContext(),"No Internet!",Toast.LENGTH_LONG).show();
        }
    }

    private void checkDataAndAddCustomer() {

        name = mNameEdit.getText().toString();
        mobile = mMobileEdit.getText().toString();
        area = mAreaEdit.getText().toString();
        city = mCityEdit.getText().toString();
        product = mProductEdit.getText().toString();
        rate = mRateEdit.getText().toString();
        address = mAddressEdit.getText().toString();
        security = mSecurityEdit.getText().toString();
        gst = mGstEdit.getText().toString();
        amountPaid = mAmountPaidEdit.getText().toString();
        cheque = mChequeEdit.getText().toString();


        //check data

        if (name.equals("") || name.isEmpty() || area.equals("") ||
                area.isEmpty() || mobile.isEmpty() || mobile.equals("")
                || city.equals("") || city.isEmpty() || product.isEmpty() ||
                product.equals("") || rate.equals("") || rate.isEmpty()||
                address.isEmpty()||address.equals("")||security.equals("")||security.isEmpty()
                ||gst.equals("")||gst.isEmpty()||amountPaid.isEmpty()||amountPaid.equals("")) {
            Toast.makeText(getApplicationContext(), "All Fields are Mandatory", Toast.LENGTH_LONG).show();
        } else {
            addCustomer();
        }
    }

    private void addCustomer() {


    }
    private void initiateAdapter(List<String>list){
        productAdapter = new ArrayAdapter<>(AddCustomerActivity.this,R.layout.skip_root_textview,R.id.reason,list);

    }
    private void selectDialoge(String title , final List<String>list){
        final AlertDialog.Builder builder = new AlertDialog.Builder(AddCustomerActivity.this);
        builder.setTitle(title);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog d = builder.create();
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.skip_reason_layout, null, false);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(productAdapter);
        d.setView(view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mProductEdit.setText(list.get(position));
                d.dismiss();
            }
        });
        d.show();
        d.getButton(d.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#004D40"));

    }
}
