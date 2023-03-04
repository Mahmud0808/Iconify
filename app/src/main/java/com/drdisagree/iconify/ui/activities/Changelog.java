package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.OLDER_CHANGELOGS;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;

public class Changelog extends AppCompatActivity {

    Changelog.GrabChangelog grabChangelog = null;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changelog);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_changelog));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // List of changelog
        container = findViewById(R.id.changelog_list);

        grabChangelog = new Changelog.GrabChangelog();
        grabChangelog.execute();
    }

    // Function to add new changelog in list
    private void addChangelog(ArrayList<String[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_list_changelog, container, false);

            TextView title = list.findViewById(R.id.changelog_title);
            title.setText(Html.fromHtml(pack.get(i)[0]));

            TextView changes = list.findViewById(R.id.changelog_text);
            changes.setText(Html.fromHtml(pack.get(i)[1]));

            if (!Objects.equals(pack.get(i)[1], ""))
                changes.setVisibility(View.VISIBLE);

            container.addView(list);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDestroy() {
        if (grabChangelog != null)
            grabChangelog.cancel(true);
        super.onDestroy();
    }

    public void onBackPressed() {
        if (grabChangelog != null)
            grabChangelog.cancel(true);
    }

    @SuppressLint("StaticFieldLeak")
    private class GrabChangelog extends AsyncTask<Integer, Integer, ArrayList<String[]>> {

        boolean connectionAvailable = false;
        ArrayList<String[]> changelogs = null;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected ArrayList<String[]> doInBackground(Integer... integers) {
            try {
                URL myUrl = new URL(OLDER_CHANGELOGS.replace("{VersionCode}", String.valueOf(BuildConfig.VERSION_CODE)));
                URLConnection connection = myUrl.openConnection();
                connection.setConnectTimeout(5000);
                connection.connect();
                connectionAvailable = true;
            } catch (Exception e) {
                connectionAvailable = false;
            }

            if (connectionAvailable) {
                changelogs = new ArrayList<>();

                for (int i = BuildConfig.VERSION_CODE; i >= 1; i--) {
                    String parseChangelog = OLDER_CHANGELOGS.replace("{VersionCode}", String.valueOf(i));
                    HttpURLConnection urlConnection = null;
                    BufferedReader bufferedReader = null;
                    StringBuilder title = null;
                    StringBuilder changes = null;

                    try {
                        URL url = new URL(parseChangelog);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.connect();

                        InputStream inputStream = urlConnection.getInputStream();

                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                        title = new StringBuilder();
                        changes = new StringBuilder();

                        String line;
                        boolean firstLine = true;

                        while ((line = bufferedReader.readLine()) != null) {
                            if (firstLine) {
                                title.append(line);
                                firstLine = false;
                            } else {
                                if (line.contains(":"))
                                    changes.append("<b>").append(line).append("</b><br>");
                                else
                                    changes.append(line.replace(">>", "&emsp;•")).append("<br>");
                            }
                        }

                        if (title.length() != 0 && changes.length() != 0) {
                            if (changes.toString().indexOf("<br>") == 0)
                                changes = new StringBuilder(changes.substring(4, changes.toString().length()));

                            if (changes.toString().lastIndexOf("<br>") == changes.toString().length() - 4)
                                changes = new StringBuilder(changes.substring(0, changes.toString().length() - 4));

                            changelogs.add(new String[]{title.toString(), changes.toString()});
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                        if (title == null || changes == null)
                            changelogs.add(new String[]{getResources().getString(R.string.individual_changelog_not_found), ""});
                        else
                            changelogs.add(new String[]{title.toString(), changes + "..."});
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            return changelogs;
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> string) {
            findViewById(R.id.grabbing_changelogs).setVisibility(View.GONE);
            if (connectionAvailable) {
                if (changelogs != null && !changelogs.isEmpty()) {
                    addChangelog(changelogs);
                } else {
                    changelogs = new ArrayList<>();
                    changelogs.add(new String[]{getResources().getString(R.string.changelog_not_found), ""});
                    addChangelog(changelogs);
                }
            } else {
                changelogs = new ArrayList<>();
                changelogs.add(new String[]{getResources().getString(R.string.no_internet_connection), ""});
                addChangelog(changelogs);
            }
        }
    }
}