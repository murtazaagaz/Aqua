package com.onesttech.stockmaneger.pojo;

import android.widget.TextView;

/**
 * Created by Murtaza on 9/8/2017.
 */

public class RoutePojo {
    private String RouteName, driverId,LogiId,ChalanNo;

    public String getRouteName() {
        return RouteName;
    }

    public void setRouteName(String routeName) {
        RouteName = routeName;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getLogiId() {
        return LogiId;
    }

    public void setLogiId(String logiId) {
        LogiId = logiId;
    }

    public String getChalanNo() {
        return ChalanNo;
    }

    public void setChalanNo(String chalanNo) {
        ChalanNo = chalanNo;
    }
}
