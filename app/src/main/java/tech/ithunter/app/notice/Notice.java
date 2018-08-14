package tech.ithunter.app.notice;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;

import com.androidadvance.topsnackbar.TSnackbar;
import com.valdesekamdem.library.mdtoast.MDToast;

public class Notice {

    public static void showErrorInTSnackBar(Activity activity, View view, int resourceId) {
        String content = activity.getString(resourceId);
        TSnackbar.make(view, content, TSnackbar.LENGTH_LONG).show();
    }

    public static void showErrorInTSnackBar(Activity activity, View view, String message) {
        TSnackbar.make(view, message, TSnackbar.LENGTH_LONG).show();
        copyTextClipboard(activity, message);
    }

    public static void showSuccessToast(Activity activity, int resourceId) {
        String message = activity.getString(resourceId);
        MDToast mdToast = MDToast.makeText(activity.getApplicationContext(), message, MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS);
        mdToast.show();
    }

    public static void showWarningToast(Activity activity, int resourceId) {
        String message = activity.getString(resourceId);
        MDToast mdToast = MDToast.makeText(activity.getApplicationContext(), message, MDToast.LENGTH_LONG, MDToast.TYPE_WARNING);
        mdToast.show();
    }

    public static void showErrorToast(Activity activity, int resourceId) {
        String message = activity.getString(resourceId);
        MDToast mdToast = MDToast.makeText(activity.getApplicationContext(), message, MDToast.LENGTH_LONG, MDToast.TYPE_ERROR);
        mdToast.show();
    }

    public static void copyTextClipboard(Activity activity, String text) {
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", text);
        clipboard.setPrimaryClip(clip);
    }
}
