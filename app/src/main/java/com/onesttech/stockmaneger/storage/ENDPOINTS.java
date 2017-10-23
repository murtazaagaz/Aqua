package com.onesttech.stockmaneger.storage;

/**
 * Created by Murtaza on 8/27/2017.
 */

public class ENDPOINTS {
    //"http://aquaroster.com/app/supplyappdata.php?logid="
    public static  final String BASE_URL = "http://emphasiscorp.org/aquaroster/app/";
    public static final String SUB_LIST = BASE_URL + "customerlist.php",
                                CAN_UPLOAD_DATA = BASE_URL+"update_customer_order.php";

    public static String LOGIN = "http://emphasiscorp.org/aquaroster/app/applogin.php";
    public static String ROUTE_LIST = "http://emphasiscorp.org/aquaroster/app/ichalanlist.php";
    public static String SEND_METER_READING =BASE_URL+"start_trip.php";

    public static String NO_ACTION = BASE_URL+"noaction_customer.php";
    public static String END_TRIP  = BASE_URL+"close_trip.php";
    public static String CUSTOMER_DATA = BASE_URL+"customer_form.php";
}
