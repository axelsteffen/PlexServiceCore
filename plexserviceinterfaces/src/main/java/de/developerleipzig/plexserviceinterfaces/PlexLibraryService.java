package de.developerleipzig.plexserviceinterfaces;

import de.developerleipzig.plexserviceinterfaces.data.PlexHubGroup;
import de.developerleipzig.plexserviceinterfaces.data.PlexLibrary;
import de.developerleipzig.plexserviceinterfaces.data.PlexMediaItem;
import de.developerleipzig.plexserviceinterfaces.data.PlexMediaPage;

import java.util.List;

import io.reactivex.Observable;

/**
 * Browse Plex libraries and fetch media lists (lazy / on-demand).
 */
public interface PlexLibraryService {
    Observable<List<PlexLibrary>> getLibrariesObserve();

    Observable<List<PlexMediaItem>> getMoviesObserve(PlexLibrary library);

    Observable<List<PlexMediaItem>> getShowsObserve(PlexLibrary library);

    /** Show → seasons, season → episodes (Phase 3.3). */
    Observable<List<PlexMediaItem>> getChildrenObserve(PlexMediaItem parent);

    /** Paginated section browse (Phase 3.4). {@code offset} is PMS container start. */
    Observable<PlexMediaPage> getMoviesPageObserve(PlexLibrary library, int offset);

    Observable<PlexMediaPage> getShowsPageObserve(PlexLibrary library, int offset);

    Observable<PlexMediaPage> getChildrenPageObserve(PlexMediaItem parent, int offset);

    /** Continue Watching for a library section. */
    Observable<PlexMediaPage> getOnDeckPageObserve(PlexLibrary library, int offset);

    /** Recently added items for a library section. */
    Observable<PlexMediaPage> getRecentlyAddedPageObserve(PlexLibrary library, int offset);

    /** Section hubs (Continue / Recently Added / recommendations, …). */
    Observable<List<PlexHubGroup>> getSectionHubsObserve(PlexLibrary library);

    /**
     * Discover watchlist (official-app style), typically filtered to movies ({@code type=1}).
     */
    Observable<PlexMediaPage> getWatchlistPageObserve(int type, int offset);
}
