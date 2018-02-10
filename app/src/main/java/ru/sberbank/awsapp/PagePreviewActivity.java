package ru.sberbank.awsapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    ProgressBar pvProgress;

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
        pvProgress = findViewById(R.id.pv_progress);

        urlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(urlField.getWindowToken(), 0);
                urlAddress = urlField.getText().toString();
                new ParseTask().execute(urlAddress);
            }
        });

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlField.getText().toString())));
            }
        });
    }

    private class ParseTask extends AsyncTask<String, Void, PreviewData> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultHtml = "";
        String imageUrl = "";
        PreviewData pd = new PreviewData("", "", null);

        @Override
        protected void onPreExecute() {
            pvProgress.setVisibility(View.VISIBLE);
            urlBtn.setEnabled(false);
        }

        @Override
        protected PreviewData doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                Log.d("123", String.valueOf(urlConnection.getResponseCode()));

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultHtml = buffer.toString();

                Pattern patternTitle = Pattern.compile("<title>(.*?)</title>");
                Pattern patternMetaDesc = Pattern.compile("<meta.*?name=\"description\".*?content=\"(.*?)\".*?>");
                Pattern patternBodyDesc = Pattern.compile("<h1|h2|h3|span|p.*>(.*?)</h1|/h2|/h3|/span|/p>");
                Pattern patternMetaImg = Pattern.compile("<meta.*?image\".*?content=\"(.*?)\".*?>");
                Pattern patternBodyImg = Pattern.compile("img.*?src=\"(.*?)\".*");
                Matcher matcherTitle = patternTitle.matcher(resultHtml);
                Matcher matcherMetaDesc = patternMetaDesc.matcher(resultHtml);
                Matcher matcherBodyDesc = patternBodyDesc.matcher(resultHtml);
                Matcher matcherMetaImg = patternMetaImg.matcher(resultHtml);
                Matcher matcherBodyImg = patternBodyImg.matcher(resultHtml);

                if (matcherTitle.find()) {
                    pd.setName(matcherTitle.group(1));
                }

                if (matcherMetaDesc.find()) {
                    pd.setDescription(matcherMetaDesc.group(1));
                } else if (matcherBodyDesc.find()) {
                    pd.setDescription(matcherBodyDesc.group(1));
                }

                if (matcherMetaImg.find()) {
                    imageUrl = matcherMetaImg.group(1);
                } else if (matcherBodyImg.find()) {
                    imageUrl = matcherBodyImg.group(1);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                URL url = new URL(imageUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                pd.setImage(BitmapFactory.decodeStream(inputStream));
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return pd;
        }

        @Override
        protected void onPostExecute(PreviewData pd) {
            super.onPostExecute(pd);

            pvProgress.setVisibility(View.INVISIBLE);
            urlBtn.setEnabled(true);

            if (pd.name.length() > 0) {
                card.setVisibility(View.VISIBLE);
                cvName.setText(pd.getName());
                cvDesc.setText(Html.fromHtml(pd.getDescription()));
                cvImg.setImageBitmap(pd.getImage());
            } else {
                Toast.makeText(PagePreviewActivity.this, "not loaded, checkout url and try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
