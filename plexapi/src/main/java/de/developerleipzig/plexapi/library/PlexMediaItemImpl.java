package de.developerleipzig.plexapi.library;

import de.developerleipzig.plexapi.network.PlexUrlHelper;
import de.developerleipzig.plexapi.network.dto.PlexMetadata;
import de.developerleipzig.plexserviceinterfaces.data.PlexMediaItem;

/**
 * Immutable {@link PlexMediaItem} from PMS metadata.
 */
public final class PlexMediaItemImpl implements PlexMediaItem {
    private final String mRatingKey;
    private final String mKey;
    private final String mTitle;
    private final String mType;
    private final long mDurationMs;
    private final long mViewOffsetMs;
    private final String mThumbUrl;
    private final int mYear;
    private final String mParentRatingKey;
    private final String mGrandparentRatingKey;
    private final int mIndex;
    private final String mParentTitle;
    private final String mGrandparentTitle;
    private final int mParentIndex;

    public PlexMediaItemImpl(String ratingKey, String key, String title, String type,
                             long durationMs, String thumbUrl, int year) {
        this(ratingKey, key, title, type, durationMs, thumbUrl, year, 0L, null, null, 0,
                null, null, 0);
    }

    public PlexMediaItemImpl(String ratingKey, String key, String title, String type,
                             long durationMs, String thumbUrl, int year, long viewOffsetMs) {
        this(ratingKey, key, title, type, durationMs, thumbUrl, year, viewOffsetMs, null, null, 0,
                null, null, 0);
    }

    public PlexMediaItemImpl(String ratingKey, String key, String title, String type,
                             long durationMs, String thumbUrl, int year, long viewOffsetMs,
                             String parentRatingKey, String grandparentRatingKey, int index) {
        this(ratingKey, key, title, type, durationMs, thumbUrl, year, viewOffsetMs,
                parentRatingKey, grandparentRatingKey, index, null, null, 0);
    }

    public PlexMediaItemImpl(String ratingKey, String key, String title, String type,
                             long durationMs, String thumbUrl, int year, long viewOffsetMs,
                             String parentRatingKey, String grandparentRatingKey, int index,
                             String parentTitle, String grandparentTitle, int parentIndex) {
        mRatingKey = ratingKey;
        mKey = key;
        mTitle = title;
        mType = type;
        mDurationMs = durationMs;
        mThumbUrl = thumbUrl;
        mYear = year;
        mViewOffsetMs = Math.max(0L, viewOffsetMs);
        mParentRatingKey = parentRatingKey;
        mGrandparentRatingKey = grandparentRatingKey;
        mIndex = Math.max(0, index);
        mParentTitle = parentTitle;
        mGrandparentTitle = grandparentTitle;
        mParentIndex = Math.max(0, parentIndex);
    }

    public static PlexMediaItemImpl fromMetadata(PlexMetadata metadata, String baseUrl, String token) {
        if (metadata == null || metadata.getRatingKey() == null || metadata.getRatingKey().isEmpty()) {
            return null;
        }
        String thumbUrl = PlexUrlHelper.absoluteUrl(baseUrl, resolveThumbPath(metadata), token);
        return new PlexMediaItemImpl(
                metadata.getRatingKey(),
                metadata.getKey(),
                metadata.getTitle(),
                metadata.getType(),
                metadata.getDuration(),
                thumbUrl,
                metadata.getYear(),
                metadata.getViewOffset(),
                metadata.getParentRatingKey(),
                metadata.getGrandparentRatingKey(),
                metadata.getIndex(),
                metadata.getParentTitle(),
                metadata.getGrandparentTitle(),
                metadata.getParentIndex());
    }

    /**
     * PMS often omits {@code thumb} on some items; fall back to art / parent art,
     * then the stable metadata thumb endpoint.
     */
    static String resolveThumbPath(PlexMetadata metadata) {
        String path = firstNonEmpty(
                metadata.getThumb(),
                metadata.getParentThumb(),
                metadata.getGrandparentThumb(),
                metadata.getArt());
        if (path != null) {
            return path;
        }
        return "/library/metadata/" + metadata.getRatingKey() + "/thumb";
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String getRatingKey() {
        return mRatingKey;
    }

    @Override
    public String getKey() {
        return mKey;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getType() {
        return mType;
    }

    @Override
    public long getDurationMs() {
        return mDurationMs;
    }

    @Override
    public long getViewOffsetMs() {
        return mViewOffsetMs;
    }

    @Override
    public String getThumbUrl() {
        return mThumbUrl;
    }

    @Override
    public int getYear() {
        return mYear;
    }

    @Override
    public String getParentRatingKey() {
        return mParentRatingKey;
    }

    @Override
    public String getGrandparentRatingKey() {
        return mGrandparentRatingKey;
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public String getGrandparentTitle() {
        return mGrandparentTitle;
    }

    @Override
    public String getParentTitle() {
        return mParentTitle;
    }

    @Override
    public int getParentIndex() {
        return mParentIndex;
    }
}
