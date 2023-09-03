package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.CHANGELOG_URL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.ActivityChangelogBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.TaskExecutor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Changelog extends BaseActivity {

    private ActivityChangelogBinding binding;
    private Changelog.GrabChangelog grabChangelog = null;

    public static String usernameToLink(String str) {
        String regexPattern = "@([A-Za-z\\d_-]+)";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(str);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String username = matcher.group(1);
            String link = "<a href=\"https://github.com/" + username + "\">@" + username + "</a>";
            matcher.appendReplacement(sb, link);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangelogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.toolbar, R.string.activity_title_changelog);

        grabChangelog = new Changelog.GrabChangelog();
        grabChangelog.execute();
    }

    @Override
    public void onPause() {
        if (grabChangelog != null) grabChangelog.cancel(true);
        super.onPause();
    }

    @Override
    public void onStop() {
        if (grabChangelog != null) grabChangelog.cancel(true);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (grabChangelog != null) grabChangelog.cancel(true);
        super.onDestroy();
    }

    public void onBackPressed() {
        if (grabChangelog != null) grabChangelog.cancel(true);
        super.onBackPressed();
    }

    @SuppressLint("StaticFieldLeak")
    private class GrabChangelog extends TaskExecutor<Integer, Integer, JSONObject> {

        LoadingDialog loadingDialog = new LoadingDialog(Changelog.this);
        boolean connectionAvailable = false;

        @Override
        protected void onPreExecute() {
            loadingDialog.show(getApplicationContext().getResources().getString(R.string.loading_dialog_wait), true);
        }

        @Override
        protected JSONObject doInBackground(Integer... integers) {
            try {
                URL myUrl = new URL(CHANGELOG_URL + BuildConfig.VERSION_NAME);
                URLConnection connection = myUrl.openConnection();
                connection.setConnectTimeout(5000);
                connection.connect();
                connectionAvailable = true;
            } catch (Exception e) {
                connectionAvailable = false;
            }

            StringBuilder release_note = null;

            if (connectionAvailable) {
                String parseChangelog = CHANGELOG_URL + BuildConfig.VERSION_NAME;
                HttpURLConnection urlConnection = null;
                BufferedReader bufferedReader = null;

                try {
                    URL url = new URL(parseChangelog);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();

                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    release_note = new StringBuilder();

                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        release_note.append(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    release_note = null;
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

            if (release_note == null)
                release_note = new StringBuilder(getResources().getString(R.string.individual_changelog_not_found));

            try {
                return new JSONObject(release_note.toString());
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject release_note) {
            loadingDialog.hide();
            String title, changes;

            if (connectionAvailable) {
                if (release_note == null || release_note.toString().length() == 0) {
                    title = getResources().getString(R.string.changelog_not_found);
                    changes = "";
                } else {
                    try {
                        String data = release_note.getString("body");
                        title = data.substring(0, data.indexOf("\r\n\r\n"));
                        changes = data.substring(data.indexOf("\n##")).substring(1);

                        title = title.replace("### ", "");
                        changes = usernameToLink(changes.replace("## ", "<b>").replace(":\r\n", ":</b><br>").replace("- __", "<br><b>• ").replace("__\r\n", "</b><br>").replace("    - ", "&emsp;◦ ").replace("- ", "• ").replace("\r\n", "<br>"));
                    } catch (JSONException e) {
                        title = getResources().getString(R.string.changelog_not_found);
                        changes = "";
                    }
                }
            } else {
                title = getResources().getString(R.string.no_internet_connection);
                changes = "";
            }

            binding.changelogTitle.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY));
            binding.changelogText.setText(HtmlCompat.fromHtml(changes, HtmlCompat.FROM_HTML_MODE_LEGACY));

            if (Objects.equals(changes, "")) binding.changelogText.setVisibility(View.GONE);
            else {
                SpannableString spannableString = new SpannableString(HtmlCompat.fromHtml(changes, HtmlCompat.FROM_HTML_MODE_LEGACY));
                URLSpan[] urls = spannableString.getSpans(0, spannableString.length(), URLSpan.class);

                for (URLSpan urlSpan : urls) {
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlSpan.getURL()));
                            startActivity(intent);
                        }
                    };

                    int start = spannableString.getSpanStart(urlSpan);
                    int end = spannableString.getSpanEnd(urlSpan);
                    spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.removeSpan(urlSpan);
                }

                binding.changelogText.setText(spannableString);
                binding.changelogText.setMovementMethod(LinkMovementMethod.getInstance());
            }

            binding.changelog.setVisibility(View.VISIBLE);
        }
    }
}