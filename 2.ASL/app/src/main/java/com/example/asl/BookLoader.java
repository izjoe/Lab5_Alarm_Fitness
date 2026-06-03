package com.example.asl;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.loader.content.AsyncTaskLoader;

public class BookLoader extends AsyncTaskLoader<BookSearchResult> {

    private final String queryString;
    private BookSearchResult cachedResult;

    public BookLoader(@NonNull Context context, String queryString) {
        super(context);
        this.queryString = queryString;
    }

    @Override
    protected void onStartLoading() {
        if (cachedResult != null) {
            deliverResult(cachedResult);
        } else {
            forceLoad();
        }
    }

    @Override
    public BookSearchResult loadInBackground() {
        return NetworkUtils.getBookInfo(queryString);
    }

    @Override
    public void deliverResult(BookSearchResult data) {
        cachedResult = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        cachedResult = null;
    }
}
