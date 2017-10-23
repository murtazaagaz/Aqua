package com.onesttech.stockmaneger.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onesttech.stockmaneger.R;
import com.onesttech.stockmaneger.activity.DetailListActivity;
import com.onesttech.stockmaneger.pojo.StockListPojo;
import com.onesttech.stockmaneger.storage.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Murtaza on 8/19/2017.
 */

public class StockListAdapter extends RecyclerView.Adapter<StockListAdapter.MyViewHolder> implements Filterable {

    private Activity mConetext;

    private List<StockListPojo> list;
    private List<StockListPojo> arraylist1;


    public StockListAdapter(List<StockListPojo>list,Activity context){
        this.list = list;
        mConetext = context;
        this.arraylist1 = list;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mConetext).inflate(R.layout.list_row_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        StockListPojo pojo = arraylist1.get(position);
        holder.name.setText(pojo.getName());
        holder.address.setText(pojo.getAddress());
        holder.status.setText(pojo.getStatus());
        holder.tv2.setText("Order Status");
        holder.tv1.setText("Address");
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase();
                //arraylist1.clear();
                FilterResults results = new FilterResults();
                int count = list.size();
                final List<StockListPojo> mList = new ArrayList<>();

                StockListPojo filterableString ;

                for (int i = 0; i < count; i++) {
                    filterableString = list.get(i);
                    if (list.get(i).getName().toLowerCase().contains(filterString)){
                        mList.add(filterableString);
                    }

//                    else if (list.get(i).getClientId().toLowerCase().contains(filterString)) {
//                        mList.add(filterableString);
//                    }

                }

                results.values = mList;
                results.count = mList.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                arraylist1  = (List<StockListPojo>) results.values;
                notifyDataSetChanged();
            }
        };
    }
    @Override
    public int getItemCount() {
        return arraylist1.size();

    }



    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name,address,status , tv1,tv2;
        RelativeLayout relative;
        public MyViewHolder(View itemView) {
            super(itemView);
            relative = (RelativeLayout) itemView.findViewById(R.id.relative);
            name = (TextView) itemView.findViewById(R.id.name);
            address = (TextView) itemView.findViewById(R.id.address);
            status = (TextView) itemView.findViewById(R.id.st);
            tv1 = (TextView) itemView.findViewById(R.id.tv1);
            tv2 = (TextView) itemView.findViewById(R.id.tv2);

            relative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mConetext,DetailListActivity.class);
                    intent.putExtra(Constants.POSITION,getAdapterPosition()+"");
                    mConetext.startActivity(intent);
                }
            });
        }
    }
}
