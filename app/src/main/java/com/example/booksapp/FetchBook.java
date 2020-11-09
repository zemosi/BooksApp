package com.example.booksapp;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;

public class FetchBook extends AsyncTask<String,Void,String> {

    private ArrayList<ItemData> values;
    private ItemAdapter itemAdapter;
    private RecyclerView recyclerView;
    Context context;

    public FetchBook(Context context, ArrayList<ItemData> values,
                     ItemAdapter itemAdapter, RecyclerView recyclerView){
        this.values = values;
        this.itemAdapter = itemAdapter;
        this.context = context;
        this.recyclerView = recyclerView;
    }


    @Override
    protected String doInBackground(String... strings) {
        String queryString = strings[0];

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJSONString = null;
        String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
        String QUERY_PARAM = "q";
        Uri builtURI = Uri.parse(BOOK_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM,queryString).build();

        try {
            URL requestURL = new URL(builtURI.toString());
            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line+"\n");
            }
            if(builder.length() == 0) {
                return null;
            }
            bookJSONString = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bookJSONString;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        values = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray itemsArray = jsonObject.getJSONArray("items");
            String title = null;
            String authors = null;
            String image = null;
            String desc = null;
            int i = 0;
            while (i<itemsArray.length()) {
                JSONObject book = itemsArray.getJSONObject(i);
                JSONObject volumeinfo = book.getJSONObject("volumeInfo");

                try {
                    title = volumeinfo.getString("title");
                    if (volumeinfo.has("authors")) {
                        authors = volumeinfo.getString("authors");
                    } else {
                        authors = "";
                    }
                    if (volumeinfo.has("description")) {
                        desc = volumeinfo.getString("description");
                    } else {
                        desc = "";
                    }
                    if (volumeinfo.has("imageLinks")) {
                        image = volumeinfo.getJSONObject("imageLinks").getString("thumbnail");
                    } else {
                        image = "";
                    }

                    ItemData itemData = new ItemData();
                    itemData.itemTitle = title;
                    itemData.itemAuthor = authors;
                    itemData.itemDescription = desc;
                    itemData.itemImage = image;

                    values.add(itemData);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                i++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.itemAdapter = new ItemAdapter(context,values);
        this.recyclerView.setAdapter(this.itemAdapter);

    }
}
