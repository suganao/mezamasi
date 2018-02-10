package com.example.admin.mezamasi;

import java.io.Serializable;

/**
 * Created by admin on 2017/04/13.
 */

public class UrlDto implements Serializable{
    private String url;
    private String memo;

    public UrlDto(){}

    public UrlDto(String url, String memo)
    {
        this.url = url;
        this.memo = memo;
    }

    public String getUrl()
    {
        return this.url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getMemo() { return this.memo; }

    public void setMemo(String memo)
    {
        this.memo = memo;
    }
}
