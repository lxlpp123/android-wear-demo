package com.livefront.android_wear_demo.tasks;

import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Task that retrieves the current weather conditions in Minneapolis,
 * MN. After success it will pass back an array of Integers. Index 0 contains the current
 * temperature, 1 contains the high temperature for the day, and 2 contains the low temperature.
 */
public class RetrieveCityWeatherTask extends AsyncTask<String, Void, int[]> {
    private static final String REQUEST_URL_FORMAT = "http://api.openweathermap.org/data/2" +
            ".5/weather?q=%s";

    public interface TaskCaller {
        void onComplete(int[] temps);
    }

    private final TaskCaller mTaskCaller;

    public RetrieveCityWeatherTask(TaskCaller caller) {
        mTaskCaller = caller;
    }

    @Override
    protected int[] doInBackground(String... params) {
        String city = params[0];
        try {
            String url = String.format(REQUEST_URL_FORMAT, URLEncoder.encode(city, "UTF-8"));

            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();

                try {
                    JSONObject json = new JSONObject(responseString);
                    JSONObject main = json.getJSONObject("main");
                    int currentTemp = convertKelvinToFahrenheit(main.getDouble("temp"));
                    int highTemp = convertKelvinToFahrenheit(main.getDouble("temp_max"));
                    int lowTemp = convertKelvinToFahrenheit(main.getDouble("temp_min"));
                    return new int[] { currentTemp, highTemp, lowTemp };
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                out.close();
            } else{
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(int[] temps) {
        super.onPostExecute(temps);
        mTaskCaller.onComplete(temps);
    }

    private int convertKelvinToFahrenheit(double degreesKelvin) {
        double degreesFahrenheit = (degreesKelvin - 273.15d) * 9d / 5d + 32;
        return (int) Math.round(degreesFahrenheit);
    }
}
