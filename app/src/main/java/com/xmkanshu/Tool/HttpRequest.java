package com.xmkanshu.Tool;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.xmkanshu.Cache.VolleyRequestQueueManager;

import org.json.JSONArray;
import org.json.JSONObject;


public class HttpRequest {
    public static void postJSONArray(String links, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, links, null, listener, errorListener);
        VolleyRequestQueueManager.mRequestQueue.add(request);
    }

    public static void postJSONObject(String links, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, links, null, listener, errorListener);
        VolleyRequestQueueManager.mRequestQueue.add(request);
    }

    public static void getString(String str, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(str, listener, errorListener);
        VolleyRequestQueueManager.mRequestQueue.add(request);
    }
}
