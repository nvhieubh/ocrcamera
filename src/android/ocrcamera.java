package cordova.plugin.ocrcamera;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import cordova.plugin.ocrcamera.ItemScan;

/**
 * This class echoes a string called from JavaScript.
 */
public class ocrcamera extends CordovaPlugin {

    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("openCameraOCR")) {

            Intent intent = new Intent(cordova.getActivity(), MainActivity.class);
            if (this.cordova != null) {
                this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
            }

            return true;
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");

            ItemScan itemScan = new ItemScan(fileNames.get(0), fileNames.get(1), fileNames.get(2), fileNames.get(3));

            this.callbackContext.success(itemScan.toJSONObject());
            
        } else if (resultCode == Activity.RESULT_CANCELED){
            this.callbackContext.error("Failed");
        }
    }

}
