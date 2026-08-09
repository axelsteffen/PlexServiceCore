package de.developerleipzig.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Tag object used by PMS for genres, directors, writers, etc. ({@code {"tag": "Action"}}).
 */
public class PlexTag {
    @SerializedName("tag")
    private String mTag;

    public String getTag() {
        return mTag;
    }
}
