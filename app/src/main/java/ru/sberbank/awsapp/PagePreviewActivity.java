package ru.sberbank.awsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PagePreviewActivity extends AppCompatActivity {
    Button urlBtn;
    EditText urlField;
    String urlAddress;
    CardView card;
    TextView cvName;
    TextView cvDesc;
    ImageView cvImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_preview);
        urlBtn = findViewById(R.id.url_btn);
        urlField = findViewById(R.id.url_field);
        card = findViewById(R.id.card_view);
        cvName = findViewById(R.id.pv_name_text);
        cvDesc = findViewById(R.id.pv_desc_text);
        cvImg = findViewById(R.id.pv_image);

        urlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                urlAddress = urlField.getText().toString();
                new ParseTask().execute(urlAddress);
                Log.d("123", urlAddress);
            }
        });

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlField.getText().toString())));
            }
        });
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

            Pattern patternTitle = Pattern.compile("<title>(.*?)</title>");
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
                card.setVisibility(View.VISIBLE);
                cvName.setText(matcherTitle.group(1));
            } else {
                Log.d("123", "nothing");
            }

            if (matcherMetaDesc.find()) {
                cvDesc.setText(Html.fromHtml(matcherMetaDesc.group(1)));
            } else if (matcherBodyDesc.find()) {
                cvDesc.setText(Html.fromHtml(matcherBodyDesc.group(1)));
            } else {
                Log.d("123", "nothing");
            }

            if (matcherBodyImg.find()) {
                Log.d("123", matcherBodyImg.group(1));
            } else if (matcherMetaImg.find()) {
                Log.d("123", matcherMetaImg.group(1));
            }
        }
    }
}
