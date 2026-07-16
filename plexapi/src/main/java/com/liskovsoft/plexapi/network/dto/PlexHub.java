package com.liskovsoft.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

/**
 * One hub from {@code GET hubs/sections/{id}} (or home hubs).
 */
public class PlexHub {
    @SerializedName("hubKey")
    private String mHubKey;

    @SerializedName("key")
    private String mKey;

    @SerializedName("title")
    private String mTitle;

    @SerializedName("type")
    private String mType;

    @SerializedName("hubIdentifier")
    private String mHubIdentifier;

    @SerializedName("size")
    private Integer mSize;

    @SerializedName("Metadata")
    private List<PlexMetadata> mMetadata;

    public String getHubKey() {
        return mHubKey;
    }

    public String getKey() {
        return mKey;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getType() {
        return mType;
    }

    public String getHubIdentifier() {
        return mHubIdentifier;
    }

    public Integer getSize() {
        return mSize;
    }

    public List<PlexMetadata> getMetadata() {
        return mMetadata != null ? mMetadata : Collections.emptyList();
    }
}
