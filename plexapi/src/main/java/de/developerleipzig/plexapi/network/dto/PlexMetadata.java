package de.developerleipzig.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metadata item (movie, show, episode, …).
 */
public class PlexMetadata {
    @SerializedName("ratingKey")
    private String mRatingKey;

    @SerializedName("key")
    private String mKey;

    @SerializedName("type")
    private String mType;

    @SerializedName("title")
    private String mTitle;

    /** Episode → show title; season → show title. */
    @SerializedName("grandparentTitle")
    private String mGrandparentTitle;

    /** Episode → season title; season → show title. */
    @SerializedName("parentTitle")
    private String mParentTitle;

    @SerializedName("summary")
    private String mSummary;

    @SerializedName("year")
    private int mYear;

    @SerializedName("duration")
    private long mDuration;

    /** Resume offset in milliseconds (PMS {@code viewOffset}). */
    @SerializedName("viewOffset")
    private long mViewOffset;

    @SerializedName("thumb")
    private String mThumb;

    @SerializedName("art")
    private String mArt;

    /** Episode/season: show or season poster when item thumb is missing. */
    @SerializedName("parentThumb")
    private String mParentThumb;

    @SerializedName("grandparentThumb")
    private String mGrandparentThumb;

    /** Episode → season; season → show. */
    @SerializedName("parentRatingKey")
    private String mParentRatingKey;

    /** Episode → show. */
    @SerializedName("grandparentRatingKey")
    private String mGrandparentRatingKey;

    /** Episode number within season, or season number within show. */
    @SerializedName("index")
    private int mIndex;

    /** Episode → season number (PMS {@code parentIndex}). */
    @SerializedName("parentIndex")
    private int mParentIndex;

    /** Show → season count; season → episode count. */
    @SerializedName("childCount")
    private int mChildCount;

    /** Show/season → total episode count. */
    @SerializedName("leafCount")
    private int mLeafCount;

    @SerializedName("Media")
    private List<PlexMedia> mMedia;

    /** Movie/show genre tags; absent on episodes/seasons. */
    @SerializedName("Genre")
    private List<PlexTag> mGenre;

    public String getRatingKey() {
        return mRatingKey;
    }

    public String getKey() {
        return mKey;
    }

    public String getType() {
        return mType;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getGrandparentTitle() {
        return mGrandparentTitle;
    }

    public String getParentTitle() {
        return mParentTitle;
    }

    public String getSummary() {
        return mSummary;
    }

    public int getYear() {
        return mYear;
    }

    public long getDuration() {
        return mDuration;
    }

    public long getViewOffset() {
        return mViewOffset;
    }

    public String getThumb() {
        return mThumb;
    }

    public String getArt() {
        return mArt;
    }

    public String getParentThumb() {
        return mParentThumb;
    }

    public String getGrandparentThumb() {
        return mGrandparentThumb;
    }

    public String getParentRatingKey() {
        return mParentRatingKey;
    }

    public String getGrandparentRatingKey() {
        return mGrandparentRatingKey;
    }

    public int getIndex() {
        return mIndex;
    }

    public int getParentIndex() {
        return mParentIndex;
    }

    public int getChildCount() {
        return mChildCount;
    }

    public int getLeafCount() {
        return mLeafCount;
    }

    public List<PlexMedia> getMedia() {
        return mMedia != null ? mMedia : Collections.emptyList();
    }

    public List<String> getGenres() {
        if (mGenre == null || mGenre.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> genres = new ArrayList<>(mGenre.size());
        for (PlexTag tag : mGenre) {
            String value = tag != null ? tag.getTag() : null;
            if (value != null && !value.isEmpty()) {
                genres.add(value);
            }
        }
        return genres;
    }
}
