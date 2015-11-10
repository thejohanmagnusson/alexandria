package it.jaschke.alexandria;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utility {

    //Check if EAN is a valid EAN10 or EAN13
    public static boolean isEanFormatValid(String ean){
        //isbn10 numbers
        if(ean.length()==10 && !ean.startsWith("978")){
            //changed since ean10 don´t start with 978 so it should not be appended either.
            return true;
        }
        //isbn13 numbers
        else return ean.length() == 13 && ean.startsWith("978");
    }

    //Check if network is available
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
