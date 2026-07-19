package de.developerleipzig.plexapi.adapter;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import de.developerleipzig.plexapi.library.PlexPage;
import de.developerleipzig.plexserviceinterfaces.data.PlexLibrary;
import de.developerleipzig.plexserviceinterfaces.data.PlexMediaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Fork-only adapter: wraps a Plex library movie page as MSC {@link MediaGroup}
 * so existing UI ({@code VideoGroup.from(MediaGroup)}) can consume it.
 * <p>
 * Items are adapted via {@link PlexMediaItemAdapter}. Pagination uses
 * {@link #getNextPageKey()} as the next PMS offset (string).
 */
public final class PlexMediaGroupAdapter implements MediaGroup {
    private static final String TYPE_LIBRARY_STUB = "library";

    /** Row / pagination kind for home shelves and library browse. */
    public enum Kind {
        LIBRARY,
        LIBRARY_GRID,
        CONTAINER,
        ON_DECK,
        RECENTLY_ADDED,
        WATCHLIST,
        HUB_RECOMMENDED
    }

    private final Kind mKind;
    private final PlexLibrary mLibrary;
    private final PlexMediaItem mContainer;
    private final String mTitle;
    private final int mWatchlistType;
    private final List<MediaItem> mMediaItems;
    private final String mNextPageKey;

    private PlexMediaGroupAdapter(Kind kind,
                                  @Nullable PlexLibrary library,
                                  @Nullable PlexMediaItem container,
                                  @Nullable String title,
                                  int watchlistType,
                                  List<MediaItem> mediaItems,
                                  @Nullable String nextPageKey) {
        mKind = kind != null ? kind : Kind.LIBRARY;
        mLibrary = library;
        mContainer = container;
        mTitle = title;
        mWatchlistType = watchlistType;
        mMediaItems = mediaItems;
        mNextPageKey = nextPageKey;
    }

    /**
     * @return adapter, or {@code null} if {@code library} is null / has no key
     */
    @Nullable
    public static PlexMediaGroupAdapter from(@Nullable PlexLibrary library,
                                             @Nullable List<PlexMediaItem> items) {
        return from(library, items, null);
    }

    @Nullable
    public static PlexMediaGroupAdapter from(@Nullable PlexLibrary library,
                                             @Nullable List<PlexMediaItem> items,
                                             @Nullable PlexPage page) {
        if (library == null || library.getKey() == null || library.getKey().isEmpty()) {
            return null;
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        MediaItem browseStub = PlexMediaItemAdapter.fromLibraryBrowse(library);
        if (browseStub != null) {
            mediaItems.add(browseStub);
        }

        appendItems(mediaItems, items);

        List<MediaItem> result = mediaItems.isEmpty() ? null : mediaItems;
        return new PlexMediaGroupAdapter(
                Kind.LIBRARY, library, null, null, 0, result, nextPageKeyFrom(page));
    }

    /**
     * Library row with browse stub + custom hub/recommended items (not {@code /all}).
     */
    @Nullable
    public static PlexMediaGroupAdapter fromRecommended(@Nullable PlexLibrary library,
                                                        @Nullable List<PlexMediaItem> items,
                                                        @Nullable PlexPage page) {
        return fromRecommended(library, null, items, page);
    }

    @Nullable
    public static PlexMediaGroupAdapter fromRecommended(@Nullable PlexLibrary library,
                                                        @Nullable String title,
                                                        @Nullable List<PlexMediaItem> items,
                                                        @Nullable PlexPage page) {
        if (library == null || library.getKey() == null || library.getKey().isEmpty()) {
            return null;
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        MediaItem browseStub = PlexMediaItemAdapter.fromLibraryBrowse(library);
        if (browseStub != null) {
            mediaItems.add(browseStub);
        }
        appendItems(mediaItems, items);

        List<MediaItem> result = mediaItems.isEmpty() ? null : mediaItems;
        return new PlexMediaGroupAdapter(
                Kind.HUB_RECOMMENDED, library, null, title, 0, result, nextPageKeyFrom(page));
    }

    /**
     * Single browse-stub row (e.g. "Alle Filme" / "Alle TV-Shows").
     */
    @Nullable
    public static PlexMediaGroupAdapter fromBrowseCard(@Nullable PlexLibrary library,
                                                       @Nullable String rowTitle,
                                                       @Nullable String cardTitle) {
        if (library == null || library.getKey() == null || library.getKey().isEmpty()) {
            return null;
        }
        if (rowTitle == null || rowTitle.isEmpty()) {
            return null;
        }
        MediaItem browseStub = PlexMediaItemAdapter.fromLibraryBrowse(library, cardTitle);
        if (browseStub == null) {
            return null;
        }
        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        mediaItems.add(browseStub);
        return new PlexMediaGroupAdapter(
                Kind.HUB_RECOMMENDED, library, null, rowTitle, 0, mediaItems, null);
    }

    /**
     * Titled shelf without browse stub (Continue Watching, Recently Added, Watchlist).
     */
    @Nullable
    public static PlexMediaGroupAdapter fromSimple(@Nullable String title,
                                                   @Nullable Kind kind,
                                                   @Nullable PlexLibrary library,
                                                   @Nullable List<PlexMediaItem> items,
                                                   @Nullable PlexPage page) {
        return fromSimple(title, kind, library, 0, items, page);
    }

    @Nullable
    public static PlexMediaGroupAdapter fromSimple(@Nullable String title,
                                                   @Nullable Kind kind,
                                                   @Nullable PlexLibrary library,
                                                   int watchlistType,
                                                   @Nullable List<PlexMediaItem> items,
                                                   @Nullable PlexPage page) {
        if (title == null || title.isEmpty()) {
            return null;
        }
        Kind resolved = kind != null ? kind : Kind.ON_DECK;
        if (resolved != Kind.WATCHLIST
                && (library == null || library.getKey() == null || library.getKey().isEmpty())) {
            // Multi-library merge: allow null library (no further pagination).
            if (items == null || items.isEmpty()) {
                return null;
            }
            ArrayList<MediaItem> mediaItems = new ArrayList<>();
            appendItems(mediaItems, items);
            if (mediaItems.isEmpty()) {
                return null;
            }
            return new PlexMediaGroupAdapter(
                    resolved, null, null, title, watchlistType, mediaItems, null);
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        appendItems(mediaItems, items);
        if (mediaItems.isEmpty()) {
            return null;
        }
        return new PlexMediaGroupAdapter(
                resolved, library, null, title, watchlistType, mediaItems, nextPageKeyFrom(page));
    }

    /**
     * Full-library grid (Phase 3.4): same items as a row but without the browse stub.
     */
    @Nullable
    public static PlexMediaGroupAdapter fromLibraryGrid(@Nullable PlexLibrary library,
                                                        @Nullable List<PlexMediaItem> items,
                                                        @Nullable PlexPage page) {
        if (library == null || library.getKey() == null || library.getKey().isEmpty()) {
            return null;
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        appendItems(mediaItems, items);

        List<MediaItem> result = mediaItems.isEmpty() ? null : mediaItems;
        return new PlexMediaGroupAdapter(
                Kind.LIBRARY_GRID, library, null, null, 0, result, nextPageKeyFrom(page));
    }

    /**
     * Children of a show or season (Phase 3.3 drill-down).
     */
    @Nullable
    public static PlexMediaGroupAdapter fromContainer(@Nullable PlexMediaItem container,
                                                      @Nullable List<PlexMediaItem> items) {
        return fromContainer(container, items, null);
    }

    @Nullable
    public static PlexMediaGroupAdapter fromContainer(@Nullable PlexMediaItem container,
                                                      @Nullable List<PlexMediaItem> items,
                                                      @Nullable PlexPage page) {
        if (container == null || container.getRatingKey() == null || container.getRatingKey().isEmpty()) {
            return null;
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        appendItems(mediaItems, items);

        List<MediaItem> result = mediaItems.isEmpty() ? null : mediaItems;
        return new PlexMediaGroupAdapter(
                Kind.CONTAINER, null, container, null, 0, result, nextPageKeyFrom(page));
    }

    /**
     * Continuation page: new items only, with updated {@link #getNextPageKey()}.
     */
    @Nullable
    public static PlexMediaGroupAdapter continueFrom(@Nullable PlexMediaGroupAdapter base,
                                                     @Nullable List<PlexMediaItem> items,
                                                     @Nullable PlexPage page) {
        if (base == null) {
            return null;
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        appendItems(mediaItems, items);

        if (mediaItems.isEmpty()) {
            return null;
        }

        return new PlexMediaGroupAdapter(
                base.mKind,
                base.mLibrary,
                base.mContainer,
                base.mTitle,
                base.mWatchlistType,
                mediaItems,
                nextPageKeyFrom(page));
    }

    public Kind getKind() {
        return mKind;
    }

    public int getWatchlistType() {
        return mWatchlistType;
    }

    /** Underlying Plex library (section key for later pagination / drill-down). */
    public PlexLibrary getPlexLibrary() {
        return mLibrary;
    }

    /** Parent show/season when this group lists children. */
    @Nullable
    public PlexMediaItem getPlexContainer() {
        return mContainer;
    }

    public boolean isLibraryGroup() {
        return mKind == Kind.LIBRARY || mKind == Kind.LIBRARY_GRID || mKind == Kind.HUB_RECOMMENDED;
    }

    public boolean isContainerGroup() {
        return mKind == Kind.CONTAINER && mContainer != null;
    }

    public boolean isOnDeckGroup() {
        return mKind == Kind.ON_DECK;
    }

    public boolean isRecentlyAddedGroup() {
        return mKind == Kind.RECENTLY_ADDED;
    }

    public boolean isWatchlistGroup() {
        return mKind == Kind.WATCHLIST;
    }

    @Override
    public int getType() {
        return TYPE_MOVIES;
    }

    @Nullable
    @Override
    public List<MediaItem> getMediaItems() {
        return mMediaItems;
    }

    @Override
    public String getTitle() {
        if (mTitle != null && !mTitle.isEmpty()) {
            return mTitle;
        }
        if (mLibrary != null) {
            return mLibrary.getTitle();
        }
        return mContainer != null ? mContainer.getTitle() : null;
    }

    @Override
    public String getChannelId() {
        return null;
    }

    /** Plex library section key or parent ratingKey — reserved for pagination. */
    @Override
    public String getParams() {
        if (mLibrary != null) {
            return mLibrary.getKey();
        }
        return mContainer != null ? mContainer.getRatingKey() : null;
    }

    @Override
    public String getReloadPageKey() {
        return null;
    }

    @Override
    public String getNextPageKey() {
        return mNextPageKey;
    }

    @Override
    public String getChannelUrl() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return mMediaItems == null || mMediaItems.isEmpty();
    }

    @Nullable
    private static String nextPageKeyFrom(@Nullable PlexPage page) {
        if (page == null) {
            return null;
        }
        int nextOffset = page.getNextOffset();
        return nextOffset >= 0 ? String.valueOf(nextOffset) : null;
    }

    private static void appendItems(ArrayList<MediaItem> mediaItems, @Nullable List<PlexMediaItem> items) {
        if (items == null) {
            return;
        }
        for (PlexMediaItem item : items) {
            if (TYPE_LIBRARY_STUB.equalsIgnoreCase(item.getType())) {
                continue;
            }
            MediaItem adapted = PlexMediaItemAdapter.from(item);
            if (adapted != null) {
                mediaItems.add(adapted);
            }
        }
    }
}
