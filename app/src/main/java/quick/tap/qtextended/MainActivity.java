/**
 * QTExtended:
 * A simple and lightweight activity to add new functionality to the
 * quick tap feature provided with the Pixel 6 (and potentially newer)
 * models. Feel free to make any pull requests with additional
 * functionality.
 *
 * @author Aidan Nixon
 */

package quick.tap.qtextended;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase DB;
    boolean TOAST;
    boolean toggle;
    int TOGGLE;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DB != null) DB.close();
    }

    /**
     * onCreate method opens up the toggle DB and checks for selected action
     * in the preferences, calling the respective methods.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
            DB = databaseHelper.getWritableDatabase();
            TOGGLE = DatabaseHelper.toggle(DB);
            toggle = (TOGGLE == 1);
        } catch (SQLiteException e) {
            Toast.makeText(this, "Error: database unavailable", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        TOAST = pref.getBoolean("TOAST", true);
        String ACTION = pref.getString("ACTION", "");

        switch (ACTION) {
            case "dnd":
                toggleDoNotDisturb();
                break;
            case "flash":
                toggleTorch();
                break;
        }

        finish();
    }

    /**
     * Check if the notification policy has been granted to the app, toggle
     * as required (doesn't require the toggle DB).
     */
    protected void toggleDoNotDisturb() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            Toast.makeText(this, "Please enable do not disturb permissions access", Toast.LENGTH_LONG).show();
            startActivity(intent);
        }

        if (notificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_ALL) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
            if (TOAST) Toast.makeText(this, "Do not disturb enabled", Toast.LENGTH_SHORT).show();
        } else {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            if (TOAST) Toast.makeText(this, "Do not disturb disabled", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check if the torch is available on the device (it should be...) and
     * then toggle the torch according to the last toggle entry in the
     * toggle DB.
     */
    protected void toggleTorch() {
        boolean isFlashAvailable = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        CameraManager cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);

        if (!isFlashAvailable) {
            Toast.makeText(this, "Flashlight is unavailable", Toast.LENGTH_LONG).show();
        }

        try {
            String camID = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(camID, toggle);
            if (TOAST) Toast.makeText(this, "Torch toggled", Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}