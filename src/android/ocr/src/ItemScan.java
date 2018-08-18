package cordova.plugin.ocrcamera;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by namnt on 8/18/2018.
 */

public class ItemScan {

    private String MerchantName;
    private String Date;
    private String Amount;
    private String image;

    public ItemScan(String merchantName, String date, String amount, String image) {
        MerchantName = merchantName;
        Date = date;
        Amount = amount;
        this.image = image;
    }

    public String getMerchantName() {
        return MerchantName;
    }

    public void setMerchantName(String merchantName) {
        MerchantName = merchantName;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public JSONObject toJSONObject(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("MerchantName", getMerchantName());
            jsonObject.put("Date", getDate());
            jsonObject.put("Amount", getAmount());
            jsonObject.put("image", getImage());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonObject;
    }
}