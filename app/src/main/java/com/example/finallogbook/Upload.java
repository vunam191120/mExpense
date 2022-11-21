package com.example.finallogbook;

public class Upload {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Upload() {
    }

    public Upload(String url) {
        this.url = url;
    }

    private String url;
}
