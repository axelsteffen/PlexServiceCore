package com.liskovsoft.plexserviceinterfaces.data;

import java.util.List;

/**
 * One Plex section hub (title + identifier + metadata items).
 */
public interface PlexHubGroup {
    String getTitle();

    String getHubIdentifier();

    /** Hub key for follow-up paging when present. */
    String getKey();

    List<PlexMediaItem> getItems();
}
