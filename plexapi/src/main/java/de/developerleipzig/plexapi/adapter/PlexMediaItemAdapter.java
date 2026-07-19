package de.developerleipzig.plexapi.adapter;

import android.media.Rating;

import android.os.Build;
import androidx.annotation.Nullable;

import androidx.annotation.RequiresApi;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import de.developerleipzig.plexapi.library.PlexMediaItemImpl;
import de.developerleipzig.plexserviceinterfaces.data.PlexBackedMediaItem;
import de.developerleipzig.plexserviceinterfaces.data.PlexLibrary;
import de.developerleipzig.plexserviceinterfaces.data.PlexMediaItem;

/**
 * Fork-only adapter: wraps {@link PlexMediaItem} as MSC {@link MediaItem}
 * so existing UI ({@code Video.from(MediaItem)}) can consume Plex items.
 * <p>
 * {@link #getVideoId()} maps to Plex {@code ratingKey} for later stream/playback routing.
 * Implements {@link PlexBackedMediaItem} so {@code Video.from} tags {@code mediaSource = PLEX}.
 */
public final class PlexMediaItemAdapter implements MediaItem, PlexBackedMediaItem {
    private static final String TYPE_MOVIE = "movie";
    private static final String TYPE_SHOW = "show";
    private static final String TYPE_SEASON = "season";
    private static final String TYPE_EPISODE = "episode";
    /** Opens full library grid via {@link #getReloadPageKey()} (Phase 3.4). */
    private static final String TYPE_LIBRARY = "library";
    private static final String TITLE_SEP = " · ";

    /**
     * Wrapped APK package id (upstream SmartTube). App-module {@code R.drawable} is not merged.
     * Assets are copied into the decoded APK by {@code packageWrapperApk}.
     */
    private static final String WRAPPER_PACKAGE = "org.smarttube.beta";
    private static final String DRAWABLE_ALL_MOVIES =
            "android.resource://" + WRAPPER_PACKAGE + "/drawable/all_movies";
    private static final String DRAWABLE_ALL_TV_SHOWS =
            "android.resource://" + WRAPPER_PACKAGE + "/drawable/all_tv_shows";

    private final PlexMediaItem mItem;
    private final int mId;
    @Nullable
    private final String mLibraryType;

    private PlexMediaItemAdapter(PlexMediaItem item, @Nullable String libraryType) {
        mItem = item;
        mLibraryType = libraryType;
        String ratingKey = item.getRatingKey();
        mId = ratingKey != null ? Math.abs(ratingKey.hashCode()) : 0;
    }

    @Nullable
    public static PlexMediaItemAdapter from(@Nullable PlexMediaItem item) {
        if (item == null || item.getRatingKey() == null || item.getRatingKey().isEmpty()) {
            return null;
        }
        return new PlexMediaItemAdapter(item, null);
    }

    /**
     * First card in a Plex library row — opens the full paginated library grid.
     */
    @Nullable
    public static PlexMediaItemAdapter fromLibraryBrowse(@Nullable PlexLibrary library) {
        return fromLibraryBrowse(library, null);
    }

    /**
     * @param displayTitle when non-empty, overrides {@link PlexLibrary#getTitle()} on the stub card
     *                     (e.g. "Alle Filme").
     */
    @Nullable
    public static PlexMediaItemAdapter fromLibraryBrowse(@Nullable PlexLibrary library,
                                                         @Nullable String displayTitle) {
        if (library == null || library.getKey() == null || library.getKey().isEmpty()) {
            return null;
        }
        String title = displayTitle != null && !displayTitle.isEmpty()
                ? displayTitle
                : library.getTitle();
        PlexMediaItem stub = new PlexMediaItemImpl(
                library.getKey(),
                library.getKey(),
                title,
                TYPE_LIBRARY,
                0L,
                null,
                0);
        return new PlexMediaItemAdapter(stub, library.getType());
    }

    /** Underlying Plex domain item (for stream lookup by ratingKey). */
    public PlexMediaItem getPlexItem() {
        return mItem;
    }

    @Override
    public int getType() {
        return isContainer() ? TYPE_PLAYLIST : TYPE_VIDEO;
    }

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public boolean isUpcoming() {
        return false;
    }

    @Override
    public boolean isShorts() {
        return false;
    }

    @Override
    public int getPercentWatched() {
        long durationMs = mItem.getDurationMs();
        long viewOffsetMs = mItem.getViewOffsetMs();
        if (durationMs <= 0L || viewOffsetMs <= 0L) {
            return -1;
        }
        int percent = (int) Math.min(100, (viewOffsetMs * 100L) / durationMs);
        return percent > 0 ? percent : -1;
    }

    @Override
    public int getStartTimeSeconds() {
        long viewOffsetMs = mItem.getViewOffsetMs();
        if (viewOffsetMs <= 0L) {
            return -1;
        }
        return (int) (viewOffsetMs / 1000L);
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public String getFeedbackToken() {
        return null;
    }

    @Override
    public String getFeedbackToken2() {
        return null;
    }

    @Override
    public String getPlaylistId() {
        return isContainer() ? mItem.getRatingKey() : null;
    }

    @Override
    public int getPlaylistIndex() {
        return -1;
    }

    @Override
    public String getParams() {
        return isLibraryBrowse() ? mLibraryType : null;
    }

    @Override
    public String getReloadPageKey() {
        return isLibraryBrowse() ? mItem.getRatingKey() : null;
    }

    @Override
    public boolean hasNewContent() {
        return false;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public String getTitle() {
        // Episode/season cards: show the TV show name as the primary line.
        if (isEpisode()) {
            String show = nonEmpty(mItem.getGrandparentTitle());
            if (show != null) {
                return show;
            }
        } else if (isSeason()) {
            String show = firstNonEmpty(mItem.getParentTitle(), mItem.getGrandparentTitle());
            if (show != null) {
                return show;
            }
        }
        return mItem.getTitle();
    }

    @Override
    public CharSequence getSecondTitle() {
        if (isEpisode()) {
            return episodeSecondTitle();
        }
        if (isSeason()) {
            return joinTitles(mItem.getTitle(), yearText());
        }
        return yearText();
    }

    @Nullable
    private CharSequence episodeSecondTitle() {
        String code = episodeCode();
        String episodeTitle = nonEmpty(mItem.getTitle());
        String year = yearText();
        if (code != null && episodeTitle != null) {
            return joinTitles(code + " " + episodeTitle, year);
        }
        if (episodeTitle != null) {
            return joinTitles(episodeTitle, year);
        }
        if (code != null) {
            return joinTitles(code, year);
        }
        return year;
    }

    @Nullable
    private String episodeCode() {
        int season = mItem.getParentIndex();
        int episode = mItem.getIndex();
        if (season <= 0 || episode <= 0) {
            return null;
        }
        return String.format("S%02dE%02d", season, episode);
    }

    @Nullable
    private String yearText() {
        int year = mItem.getYear();
        return year > 0 ? String.valueOf(year) : null;
    }

    @Nullable
    private static String joinTitles(@Nullable String left, @Nullable String right) {
        if (left == null || left.isEmpty()) {
            return right;
        }
        if (right == null || right.isEmpty()) {
            return left;
        }
        return left + TITLE_SEP + right;
    }

    @Nullable
    private static String nonEmpty(@Nullable String value) {
        return value != null && !value.isEmpty() ? value : null;
    }

    @Nullable
    private static String firstNonEmpty(@Nullable String first, @Nullable String second) {
        String a = nonEmpty(first);
        return a != null ? a : nonEmpty(second);
    }

    private boolean isEpisode() {
        return TYPE_EPISODE.equalsIgnoreCase(mItem.getType());
    }

    private boolean isSeason() {
        return TYPE_SEASON.equalsIgnoreCase(mItem.getType());
    }

    @Override
    public String getVideoId() {
        if (isLibraryBrowse() || isContainer()) {
            return null;
        }
        return mItem.getRatingKey();
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public long getDurationMs() {
        // Show/season "duration" from PMS is not a playable length; keeping it caused
        // Video.isMembersOnly() (videoId null + duration > 0) to drop every TV show card.
        if (isContainer() || isLibraryBrowse()) {
            return 0L;
        }
        return mItem.getDurationMs();
    }

    @Override
    public String getBadgeText() {
        return null;
    }

    @Override
    public String getProductionDate() {
        int year = mItem.getYear();
        return year > 0 ? String.valueOf(year) : null;
    }

    @Override
    public long getPublishedDate() {
        return -1;
    }

    @Override
    public String getCardImageUrl() {
        String libraryThumb = libraryBrowseThumbUrl();
        return libraryThumb != null ? libraryThumb : mItem.getThumbUrl();
    }

    @Override
    public String getBackgroundImageUrl() {
        String libraryThumb = libraryBrowseThumbUrl();
        return libraryThumb != null ? libraryThumb : mItem.getThumbUrl();
    }

    @Nullable
    private String libraryBrowseThumbUrl() {
        if (!isLibraryBrowse()) {
            return null;
        }
        if (TYPE_SHOW.equalsIgnoreCase(mLibraryType)) {
            return DRAWABLE_ALL_TV_SHOWS;
        }
        if (TYPE_MOVIE.equalsIgnoreCase(mLibraryType)) {
            return DRAWABLE_ALL_MOVIES;
        }
        return null;
    }

    @Override
    public int getWidth() {
        return 1280;
    }

    @Override
    public int getHeight() {
        return 720;
    }

    @Override
    public String getChannelId() {
        return null;
    }

    @Override
    public String getVideoPreviewUrl() {
        return null;
    }

    @Override
    public String getAudioChannelConfig() {
        return "2.0";
    }

    @Override
    public String getPurchasePrice() {
        return "$0.00";
    }

    @Override
    public String getRentalPrice() {
        return "$0.00";
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int getRatingStyle() {
        return Rating.RATING_5_STARS;
    }

    @Override
    public double getRatingScore() {
        return 0;
    }
    
    /**
     * Must stay {@code false}. SmartTube's {@code Video.isEmpty()} treats {@code isMovie}
     * as YouTube "Free with Ads" (unsupported) and drops the card. Plex movies are normal
     * playable items ({@link #getType()} → {@code TYPE_VIDEO}).
     */
    @Override
    public boolean isMovie() {
        return false;
    }

    @Override
    public boolean hasUploads() {
        return isContainer() || isLibraryBrowse();
    }

    @Override
    public String getClickTrackingParams() {
        return null;
    }

    @Override
    public String getSearchQuery() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MediaItem) {
            MediaItem other = (MediaItem) obj;
            if (isContainer()) {
                String playlistId = getPlaylistId();
                return playlistId != null && playlistId.equals(other.getPlaylistId());
            }
            String videoId = getVideoId();
            return videoId != null && videoId.equals(other.getVideoId());
        }
        return false;
    }

    private boolean isContainer() {
        String type = mItem.getType();
        return TYPE_SHOW.equalsIgnoreCase(type) || TYPE_SEASON.equalsIgnoreCase(type);
    }

    private boolean isLibraryBrowse() {
        return TYPE_LIBRARY.equalsIgnoreCase(mItem.getType());
    }

    @Override
    public int hashCode() {
        if (isContainer()) {
            String playlistId = getPlaylistId();
            return playlistId != null ? playlistId.hashCode() : 0;
        }
        String videoId = getVideoId();
        return videoId != null ? videoId.hashCode() : 0;
    }
}
