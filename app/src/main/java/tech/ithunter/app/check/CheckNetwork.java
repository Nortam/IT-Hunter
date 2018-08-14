package tech.ithunter.app.check;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckNetwork {
    private ConnectivityManager cm;
    private NetworkInfo activeNetwork;

    public CheckNetwork(Context context) throws Exception {
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
    }

    public boolean isConnected() throws Exception {
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public boolean isWifi() throws Exception {
        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
