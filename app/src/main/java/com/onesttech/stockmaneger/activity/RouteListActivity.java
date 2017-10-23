package com.onesttech.stockmaneger.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.onesttech.stockmaneger.R;
import com.onesttech.stockmaneger.adapter.RouteListAdapter;
import com.onesttech.stockmaneger.network.MyVolley;
import com.onesttech.stockmaneger.pojo.RoutePojo;
import com.onesttech.stockmaneger.storage.Constants;
import com.onesttech.stockmaneger.storage.ENDPOINTS;
import com.onesttech.stockmaneger.storage.MySharedPrefrences;
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


public class RouteListActivity extends AppCompatActivity {
    private static final String TAG = "RouteListActivity";
    @BindView(R.id.toolbar)Toolbar mToolbar;
    @BindView(R.id.recycleView)RecyclerView mRecycleView;
    private MySharedPrefrences sp;
    private ProgressDialog pd;
    private RequestQueue mRequestQue;
    private List<RoutePojo>mList = new ArrayList<>();
    private RouteListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Routes");
        sp = MySharedPrefrences.getInstance(this);
        pd = new ProgressDialog(this);
        mRequestQue = MyVolley.getInstance().getRequestQueue();
        mRecycleView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(manager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());


        if (Util.isNetworkAvailable()){
            StringRequest request = new StringRequest(Request.Method.POST, ENDPOINTS.ROUTE_LIST, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray array = new JSONArray(response);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            RoutePojo pojo = new RoutePojo();
                            pojo.setRouteName(obj.getString("route_name"));
                            pojo.setChalanNo(obj.getString("chalan_no"));
                            pojo.setDriverId(obj.getString("driver_id"));
                            pojo.setLogiId(obj.getString("logid"));
                            mList.add(pojo);
                        }
                        adapter = new RouteListAdapter(mList,RouteListActivity.this);
                        mRecycleView.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();

                    }
                    Log.d(TAG, "onResponse: "+response);
                 }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "onErrorResponse");
                    Toast.makeText(getApplicationContext(),"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();

                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put(Constants.UID,sp.getKey(Constants.UID));
                    params.put(Constants.COMPANY_ID,sp.getKey(Constants.COMPANY_ID));
                    return params;
                }
            };
            mRequestQue.add(request);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.rout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                showLogOutAlertDialoge();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
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

                Intent intent = new Intent(RouteListActivity.this,LoginActivity.class);
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
    }

