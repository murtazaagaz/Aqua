package com.onesttech.stockmaneger.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.onesttech.stockmaneger.R;
import com.onesttech.stockmaneger.activity.MainActivity;
import com.onesttech.stockmaneger.network.MyVolley;
import com.onesttech.stockmaneger.pojo.RoutePojo;
import com.onesttech.stockmaneger.storage.Constants;
import com.onesttech.stockmaneger.storage.ENDPOINTS;
import com.onesttech.stockmaneger.storage.MySharedPrefrences;
import com.onesttech.stockmaneger.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Murtaza on 9/8/2017.
 */

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.MyViewHolder> {
    private List<RoutePojo> mList;
    private Activity mContext;
    private RequestQueue mRequestQue;
    private String TAG = "TAG";
    private MySharedPrefrences sp;
    private ProgressDialog pd;

    public RouteListAdapter(List<RoutePojo> list, Activity context) {
        mList = list;
        mContext = context;
        mRequestQue = MyVolley.getInstance().getRequestQueue();
        sp = MySharedPrefrences.getInstance(context);
        pd = new ProgressDialog(context);
        pd.setMessage("Processing..");
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.route_list_layout, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RoutePojo pojo = mList.get(position);
        holder.routename.setText(pojo.getRouteName());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView routename;
        RelativeLayout linear;

        public MyViewHolder(View itemView) {
            super(itemView);
            routename = (TextView) itemView.findViewById(R.id.route_name);
            linear = (RelativeLayout) itemView.findViewById(R.id.linear);
            linear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlertDialoge();
                   }
            });
        }

        private void showAlertDialoge() {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mList.get(getAdapterPosition()).getRouteName());
            View view = LayoutInflater.from(mContext).inflate(R.layout.alertdialge_layout,null,false);
            final EditText editText = (EditText) view.findViewById(R.id.meter);
            //Vie w view = new View(mContext);

            builder.setView(view);
            builder.setPositiveButton(" Select Route", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String reading = editText.getText().toString();
                    if (reading.isEmpty() || reading.equals("")) {
                        Toast.makeText(mContext, "Please enter Metre Reading to Proceed ", Toast.LENGTH_LONG).show();
                    } else {
                        if (Util.isNetworkAvailable()) {
                            pd.show();
                            final int pos = getAdapterPosition();
                            selectRoute(pos, reading);

                        } else {
                            Toast.makeText(mContext, "No Internet!", Toast.LENGTH_LONG).show();
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

        private void selectRoute(final int pos, final String reading) {
            StringRequest request = new StringRequest(Request.Method.POST, ENDPOINTS.SEND_METER_READING, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    pd.dismiss();


                    if (response.equals("0")) {
                        Toast.makeText(mContext,"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();



                    }
                    else {
                        Toast.makeText(mContext, "Trip Started", Toast.LENGTH_LONG).show();
                        sp.setKey(Constants.CHALAN_NO, mList.get(pos).getChalanNo());
                        sp.setKey(Constants.LOGID, mList.get(pos).getLogiId());
                        sp.setKey(Constants.DRIVER_ID, mList.get(pos).getDriverId());
                        sp.setStep(Constants.STEPS,"step3");
                        Intent i = new Intent(mContext, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK & Intent.FLAG_ACTIVITY_CLEAR_TOP & Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(i);
                    }
                    Log.d(TAG, "onResponse: " + response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(mContext,"Something Went Wrong Please Try Again Later.",Toast.LENGTH_LONG).show();

                    pd.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("logid", mList.get(pos).getLogiId());
                    params.put("chalan_no", mList.get(pos).getChalanNo());
                    sp.setKey(Constants.CHALAN_NO,mList.get(pos).getChalanNo());
                    // params.put("driver_id", mList.get(pos).getDriverId());
                    params.put("start_meter_reading", reading);
                    params.put(Constants.COMPANY_ID,sp.getKey(Constants.COMPANY_ID));

                    return params;
                }
            };
            mRequestQue.add(request);
        }
    }
}
