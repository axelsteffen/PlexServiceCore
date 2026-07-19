package de.developerleipzig.plexserviceinterfaces.data;

public interface PlexMediaItem {
    String getRatingKey();

    String getKey();

    String getTitle();

    String getType();

    long getDurationMs();

    /** Playback resume offset from PMS ({@code viewOffset}), in milliseconds. */
    long getViewOffsetMs();

    String getThumbUrl();

    int getYear();

    /** Season ratingKey for episodes; null otherwise. */
    String getParentRatingKey();

    /** Show ratingKey for episodes/seasons; null otherwise. */
    String getGrandparentRatingKey();

    /** Episode or season index within parent (PMS {@code index}); 0 if unknown. */
    int getIndex();
}
