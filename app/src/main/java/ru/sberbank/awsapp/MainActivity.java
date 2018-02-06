package ru.sberbank.awsapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new ParseTask().execute("https://fb.com");
    }

    private class ParseTask extends AsyncTask<String, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(String... urls) {
            // получаем данные с внешнего ресурса
            try {
                URL url;
                Log.d("123", urls[0]);
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            Pattern patternTitle = Pattern.compile("<title>(.*)<\\/title>");
            Pattern patternMetaDesc = Pattern.compile("<meta.*?name=\"description\".*?content=\"(.*?)\".*?>|<meta.*?content=\"(.*?)\".*?name=\"description\".*?>");
            Pattern patternBodyDesc = Pattern.compile("<h1.*?>(.*)<\\/h1>");
            Pattern patternMetaImg = Pattern.compile("<meta.*?image\".*?content=\"(.*?)\".*?>|<meta.*?content=\"(.*?)\".*?image\".*?>");
            Pattern patternBodyImg = Pattern.compile("img.*?src=\"(.*?)\".*");
            Matcher matcherTitle = patternTitle.matcher(strJson);
            Matcher matcherMetaDesc = patternMetaDesc.matcher(strJson);
            Matcher matcherBodyDesc = patternBodyDesc.matcher(strJson);
            Matcher matcherMetaImg = patternMetaImg.matcher(strJson);
            Matcher matcherBodyImg = patternBodyImg.matcher(strJson);

            // выводим целиком полученную json-строку
            if (matcherTitle.find()) {
                Log.d("123", matcherTitle.group(1));
            } else {
                Log.d("123", "nothing");
            }

            if (matcherMetaDesc.find()) {
                Log.d("123", matcherMetaDesc.group(1));
            } else if (matcherBodyDesc.find()) {
                Log.d("123", matcherBodyDesc.group(1));
            } else {
                Log.d("123", "nothing");
            }

            if (matcherBodyImg.find()){
                Log.d("123", matcherBodyImg.group(1));
            } else if (matcherMetaImg.find()) {
                Log.d("123", matcherMetaImg.group(1));
            }

        }
    }
}
