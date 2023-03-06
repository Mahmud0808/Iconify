package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.OLDER_CHANGELOGS;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.ChangelogAdapter;
import com.drdisagree.iconify.ui.models.ChangelogModel;
import com.drdisagree.iconify.ui.views.LoadingDialog;
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

        grabChangelog = new Changelog.GrabChangelog();
        grabChangelog.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class GrabChangelog extends AsyncTask<Integer, Integer, ArrayList<ChangelogModel>> {

        LoadingDialog loadingDialog = new LoadingDialog(Changelog.this);
        boolean connectionAvailable = false;
        ArrayList<ChangelogModel> changelog_list = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            loadingDialog.show(Iconify.getAppContext().getResources().getString(R.string.loading_dialog_wait), true);
        }

        @Override
        protected ArrayList<ChangelogModel> doInBackground(Integer... integers) {
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


                            changelog_list.add(new ChangelogModel(title.toString(), changes.toString()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                        if (title == null || changes == null)
                            changelog_list.add(new ChangelogModel(getResources().getString(R.string.individual_changelog_not_found), ""));
                        else
                            changelog_list.add(new ChangelogModel(title.toString(), changes + "..."));
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
            return changelog_list;
        }

        @Override
        protected void onPostExecute(ArrayList<ChangelogModel> string) {
            loadingDialog.hide();

            if (connectionAvailable) {
                if (changelog_list.isEmpty()) {
                    changelog_list.add(new ChangelogModel(getResources().getString(R.string.changelog_not_found), ""));
                }
            } else {
                changelog_list.add(new ChangelogModel(getResources().getString(R.string.no_internet_connection), ""));
            }

            // RecyclerView
            RecyclerView container = findViewById(R.id.changelog_container);
            container.setLayoutManager(new LinearLayoutManager(Changelog.this));
            ChangelogAdapter adapter = new ChangelogAdapter(Changelog.this, changelog_list);
            container.setAdapter(adapter);
            container.setHasFixedSize(false);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onPause() {
        if (grabChangelog != null)
            grabChangelog.cancel(true);
        super.onPause();
    }

    @Override
    public void onStop() {
        if (grabChangelog != null)
            grabChangelog.cancel(true);
        super.onStop();
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
}