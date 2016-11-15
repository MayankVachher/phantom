package com.mayank13059.phantom;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private RetainedFragment mRetainedFragment = null;
    private Button trigger, trigger_clear;

    public static class RetainedFragment extends Fragment {
        private String codePage;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setRetainInstance(true);
        }

        public String getData() {
            return this.codePage;
        }

        public void setData(String result) {
            this.codePage = result;
        }
    }

    private class LoadPageFromNetworkAsyncTask extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                return downloadUrl("https://www.iiitd.ac.in/about");
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            mRetainedFragment.setData(s);
            textView.setText(s);
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                String contentAsString = readIt(is);
                Log.d("downloadURL", "Content: " + contentAsString);
                return contentAsString;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String readIt(InputStream stream)
                throws IOException, UnsupportedEncodingException {
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();
            Reader in = new InputStreamReader(stream, "UTF-8");
            for (;;) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            return out.toString();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String retainedFragmentTag = "RetainedFragmentTag";

        textView = (TextView) findViewById(R.id.aboutSectionData);
        trigger = (Button) findViewById(R.id.trigger);
        trigger_clear = (Button) findViewById(R.id.trigger_clear);

        FragmentManager fm = getFragmentManager();

        mRetainedFragment = (RetainedFragment) fm.findFragmentByTag(retainedFragmentTag);

        if(mRetainedFragment == null) {
            mRetainedFragment = new RetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, retainedFragmentTag).commit();
        }
        else {
            textView.setText(mRetainedFragment.getData());
        }

        trigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getIIITDData();
            }
        });

        trigger_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRetainedFragment.setData(null);
                textView.setText(null);
            }
        });
    }

    public void getIIITDData() {
        new LoadPageFromNetworkAsyncTask().execute();
    }
}