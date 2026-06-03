package com.example.asl;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

final class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    private static final String BOOK_BASE_URL =
            "https://www.googleapis.com/books/v1/volumes";
    private static final String QUERY_PARAM = "q";
    private static final String MAX_RESULTS = "maxResults";
    private static final String PRINT_TYPE = "printType";
    private static final String API_KEY = "key";

    private NetworkUtils() {
    }

    static BookSearchResult getBookInfo(String queryString) {
        HttpURLConnection urlConnection = null;

        try {
            Uri.Builder uriBuilder = Uri.parse(BOOK_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, queryString)
                    .appendQueryParameter(MAX_RESULTS, "10")
                    .appendQueryParameter(PRINT_TYPE, "books");
            if (!BuildConfig.GOOGLE_BOOKS_API_KEY.isEmpty()) {
                uriBuilder.appendQueryParameter(API_KEY, BuildConfig.GOOGLE_BOOKS_API_KEY);
            }

            Uri builtUri = uriBuilder.build();

            URL requestUrl = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) requestUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 429) {
                Log.w(LOG_TAG, "Google Books quota was exceeded.");
                return BookSearchResult.rateLimited();
            }
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(LOG_TAG, "Google Books request failed: " + responseCode);
                return BookSearchResult.requestFailed();
            }

            try (InputStream inputStream = urlConnection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append('\n');
                }
                return buffer.length() == 0
                        ? BookSearchResult.requestFailed()
                        : BookSearchResult.success(buffer.toString());
            }
        } catch (IOException exception) {
            Log.e(LOG_TAG, "Unable to retrieve books.", exception);
            return BookSearchResult.networkFailed();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
