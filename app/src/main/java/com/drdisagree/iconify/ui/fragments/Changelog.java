package com.drdisagree.iconify.ui.fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.FragmentChangelogBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.extension.TaskExecutor;

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

public class Changelog extends BaseFragment {

    private FragmentChangelogBinding binding;
    private GrabChangelog grabChangelog = null;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChangelogBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_changelog);

        try {
            grabChangelog = new GrabChangelog();
            grabChangelog.execute();
        } catch (Exception ignored) {
        }

        return view;
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

    @SuppressLint("StaticFieldLeak")
    private class GrabChangelog extends TaskExecutor<Integer, Integer, JSONObject> {

        LoadingDialog loadingDialog = new LoadingDialog(requireContext());
        boolean connectionAvailable = false;

        @Override
        protected void onPreExecute() {
            loadingDialog.show(Iconify.Companion.getAppContextLocale().getResources().getString(R.string.loading_dialog_wait), true);
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