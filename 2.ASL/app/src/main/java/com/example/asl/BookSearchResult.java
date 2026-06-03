package com.example.asl;

final class BookSearchResult {

    enum Status {
        SUCCESS,
        RATE_LIMITED,
        REQUEST_FAILED,
        NETWORK_FAILED
    }

    private final Status status;
    private final String json;

    private BookSearchResult(Status status, String json) {
        this.status = status;
        this.json = json;
    }

    static BookSearchResult success(String json) {
        return new BookSearchResult(Status.SUCCESS, json);
    }

    static BookSearchResult rateLimited() {
        return new BookSearchResult(Status.RATE_LIMITED, null);
    }

    static BookSearchResult requestFailed() {
        return new BookSearchResult(Status.REQUEST_FAILED, null);
    }

    static BookSearchResult networkFailed() {
        return new BookSearchResult(Status.NETWORK_FAILED, null);
    }

    Status getStatus() {
        return status;
    }

    String getJson() {
        return json;
    }
}
