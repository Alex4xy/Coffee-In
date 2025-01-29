package com.thezec.coffeein.Models;

/**
 * Created by Aleksandar on 11/24/2014.
 */
public class QuotesModel {

    private int id;
    private String by;
    private String quote;

    public QuotesModel(int id, String by, String quote) {
        this.id = id;
        this.by = by;
        this.quote = quote;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }
}

