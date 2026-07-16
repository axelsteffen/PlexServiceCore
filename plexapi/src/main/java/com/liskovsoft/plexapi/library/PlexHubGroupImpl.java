package com.liskovsoft.plexapi.library;

import androidx.annotation.Nullable;

import com.liskovsoft.plexserviceinterfaces.data.PlexHubGroup;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;

import java.util.Collections;
import java.util.List;

/** Concrete hub row from {@code hubs/sections/{id}}. */
public final class PlexHubGroupImpl implements PlexHubGroup {
    private final String mTitle;
    private final String mHubIdentifier;
    private final String mKey;
    private final List<PlexMediaItem> mItems;

    public PlexHubGroupImpl(@Nullable String title,
                            @Nullable String hubIdentifier,
                            @Nullable String key,
                            @Nullable List<PlexMediaItem> items) {
        mTitle = title;
        mHubIdentifier = hubIdentifier;
        mKey = key;
        mItems = items != null ? items : Collections.emptyList();
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getHubIdentifier() {
        return mHubIdentifier;
    }

    @Override
    public String getKey() {
        return mKey;
    }

    @Override
    public List<PlexMediaItem> getItems() {
        return mItems;
    }
}
