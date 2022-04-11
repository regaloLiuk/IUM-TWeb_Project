package com.example.progetto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.net.ssl.HttpsURLConnection;

import static java.util.Collections.sort;


public class StoricoHttp extends AsyncTask<URL, Integer, Long> {

    String response = "";
    String utente;
    Context context;
    StoricoRecyclerViewAdapter arrayAdapter;



    StoricoHttp(String utente, Context context, StoricoRecyclerViewAdapter arrayAdapter) {
        this.context=context;
        this.utente = utente;
        this.arrayAdapter=arrayAdapter;

    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder feedback = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                feedback.append("&");

            feedback.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            feedback.append("=");
            feedback.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return feedback.toString();
    }

    public void getData() throws IOException {

        HashMap<String, String> params = new HashMap<>();
        params.put("var", "storico");
        params.put("ute",utente );



        URL url = new URL("http://10.0.2.2:8088/ProgettoIUM/Servlet");
        HttpURLConnection client = null;
        try {
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Authorization", "http://10.0.2.2:8088/ProgettoIUM/Servlet");
            client.setDoInput(true);
            client.setDoOutput(true);

            OutputStream os = client.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(params));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = client.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            }
            else {
                response = "#ERR#";
            }
        }
        catch (MalformedURLException e){
            e.printStackTrace();
        }
        finally {
            if(client != null) // Make sure the connection is not null.
                client.disconnect();
        }
    }

    @Override
    protected Long doInBackground(URL... params) {
        try {
            getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // This counts how many bytes were downloaded
        final byte[] result = response.getBytes();
        Long numOfBytes = Long.valueOf(result.length);
        return numOfBytes;
    }

    protected void onPostExecute(Long result) {
        System.out.println("Downloaded " + result + " bytes");

        arrayAdapter.getmData().clear();
        try{

            final JSONObject obj = new JSONObject(response);
            final JSONArray storico = obj.getJSONArray("Storico");
            final int n = storico.length();
            System.out.println("STORICO----------------"+n);
            for (int i = 0; i < n; ++i) {
                final JSONObject person = storico.getJSONObject(i);

                String utente=person.getString("UTENTE");
                String idprof=person.getString("PROF");
                String materia=person.getString("MATERIA");
                String giorno=person.getString("GIORNO");
                String ora=person.getString("ORA");
                String stato=person.getString("STATO");
                arrayAdapter.getmData().add(new Storico(utente,idprof,materia,giorno,ora,stato));

            }
            arrayAdapter.notifyDataSetChanged();
            sort(arrayAdapter.getmData());

        }catch(JSONException e){System.out.println(e);}



    }

}