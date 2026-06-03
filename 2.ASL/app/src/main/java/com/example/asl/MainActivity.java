package com.example.asl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<BookSearchResult> {

    private static final int BOOK_LOADER_ID = 0;
    private static final String QUERY_KEY = "queryString";

    private EditText bookInput;
    private TextView titleText;
    private TextView authorText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bookInput = findViewById(R.id.bookInput);
        titleText = findViewById(R.id.titleText);
        authorText = findViewById(R.id.authorText);

        LoaderManager loaderManager = LoaderManager.getInstance(this);
        if (loaderManager.getLoader(BOOK_LOADER_ID) != null) {
            loaderManager.initLoader(BOOK_LOADER_ID, null, this);
        }
    }

    public void searchBooks(View view) {
        String queryString = bookInput.getText().toString().trim();
        hideKeyboard();

        if (queryString.isEmpty()) {
            showMessage(R.string.empty_search);
            return;
        }

        if (!hasNetworkConnection()) {
            showMessage(R.string.network_error);
            return;
        }

        Bundle queryBundle = new Bundle();
        queryBundle.putString(QUERY_KEY, queryString);
        LoaderManager.getInstance(this).restartLoader(BOOK_LOADER_ID, queryBundle, this);

        authorText.setText("");
        titleText.setText(R.string.loading);
    }

    private void hideKeyboard() {
        View focusedView = getCurrentFocus();
        if (focusedView == null) {
            return;
        }

        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(
                    focusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS
            );
        }
    }

    private boolean hasNetworkConnection() {
        ConnectivityManager connectionManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectionManager == null) {
            return false;
        }

        Network activeNetwork = connectionManager.getActiveNetwork();
        NetworkCapabilities capabilities =
                connectionManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void showMessage(int stringResource) {
        titleText.setText(stringResource);
        authorText.setText("");
    }

    @NonNull
    @Override
    public Loader<BookSearchResult> onCreateLoader(int id, @Nullable Bundle args) {
        String queryString = args == null ? "" : args.getString(QUERY_KEY, "");
        return new BookLoader(this, queryString);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<BookSearchResult> loader, BookSearchResult result) {
        if (result == null || result.getStatus() == BookSearchResult.Status.REQUEST_FAILED) {
            showMessage(R.string.service_error);
            return;
        }
        if (result.getStatus() == BookSearchResult.Status.RATE_LIMITED) {
            showMessage(R.string.quota_error);
            return;
        }
        if (result.getStatus() == BookSearchResult.Status.NETWORK_FAILED) {
            showMessage(R.string.network_error);
            return;
        }

        try {
            JSONObject response = new JSONObject(result.getJson());
            JSONArray items = response.optJSONArray("items");
            if (items == null) {
                showMessage(R.string.no_results);
                return;
            }

            for (int i = 0; i < items.length(); i++) {
                JSONObject volumeInfo = items.getJSONObject(i).optJSONObject("volumeInfo");
                if (volumeInfo == null) {
                    continue;
                }

                String title = volumeInfo.optString("title", "").trim();
                String authors = readAuthors(volumeInfo.optJSONArray("authors"));
                if (!title.isEmpty() && !authors.isEmpty()) {
                    titleText.setText(title);
                    authorText.setText(authors);
                    return;
                }
            }
        } catch (Exception ignored) {
            // Malformed or incomplete API responses are shown as no matching result.
        }

        showMessage(R.string.no_results);
    }

    private String readAuthors(@Nullable JSONArray authorArray) {
        if (authorArray == null) {
            return "";
        }

        StringBuilder authors = new StringBuilder();
        for (int i = 0; i < authorArray.length(); i++) {
            String author = authorArray.optString(i, "").trim();
            if (author.isEmpty()) {
                continue;
            }
            if (authors.length() > 0) {
                authors.append(", ");
            }
            authors.append(author);
        }
        return authors.toString();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<BookSearchResult> loader) {
        // No retained UI data needs to be cleared.
    }
}
