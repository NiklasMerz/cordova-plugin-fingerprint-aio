package de.niklasmerz.cordova.biometric;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Args {
    private static final String TAG = "ARGS";
    private JSONArray jsonArray;
    private JSONObject argsObject;

    Args(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public Boolean getBoolean(String name, Boolean defaultValue) {
        try {
            if (getArgsObject().has(name)){
                return getArgsObject().getBoolean(name);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Can't parse '" + name + "'. Default will be used.", e);
        }
        return defaultValue;
    }

    public String getString(String name, String defaultValue) {
        try {
            if (getArgsObject().optString(name) != null
                && !getArgsObject().optString(name).isEmpty()){
                return getArgsObject().getString(name);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Can't parse '" + name + "'. Default will be used.", e);
        }
        return defaultValue;
    }

    private JSONObject getArgsObject() throws JSONException {
        if (this.argsObject != null) {
            return this.argsObject;
        }
        this.argsObject = jsonArray.getJSONObject(0);
        return this.argsObject;
    }
}
