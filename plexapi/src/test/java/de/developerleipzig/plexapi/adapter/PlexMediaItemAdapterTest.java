package de.developerleipzig.plexapi.adapter;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import de.developerleipzig.plexapi.library.PlexLibraryImpl;
import de.developerleipzig.plexapi.library.PlexMediaItemImpl;
import de.developerleipzig.plexserviceinterfaces.data.PlexBackedMediaItem;
import de.developerleipzig.plexserviceinterfaces.data.PlexMediaItem;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PlexMediaItemAdapterTest {
    @Test
    public void from_mapsCoreFields() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "12345",
                "/library/metadata/12345",
                "Test Movie",
                "movie",
                5_400_000L,
                "https://plex/thumb.jpg?X-Plex-Token=t",
                2020);

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals(MediaItem.TYPE_VIDEO, item.getType());
        assertEquals("12345", item.getVideoId());
        assertEquals("Test Movie", item.getTitle());
        assertEquals(5_400_000L, item.getDurationMs());
        assertEquals("https://plex/thumb.jpg?X-Plex-Token=t", item.getCardImageUrl());
        assertEquals("https://plex/thumb.jpg?X-Plex-Token=t", item.getBackgroundImageUrl());
        assertEquals("2020", item.getSecondTitle());
        assertEquals("2020", item.getProductionDate());
        // isMovie must be false: SmartTube Video.isEmpty() drops YouTube "Free with Ads" movies
        assertFalse(item.isMovie());
        assertFalse(item.isLive());
        assertFalse(item.isShorts());
    }

    @Test
    public void from_movieWithGenres_appendsGenresToSecondTitle() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "12346", "/library/metadata/12346", "Test Movie", "movie",
                5_400_000L, null, 2020, 0L, null, null, 0, null, null, 0,
                java.util.Arrays.asList("Action", "Drama", "Thriller", "Crime"));

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        // Capped at 3 genres for readability on TV screens.
        assertEquals("2020 · Action, Drama, Thriller", item.getSecondTitle());
    }

    @Test
    public void from_movieWithoutGenres_secondTitleIsYearOnly() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "12347", "/library/metadata/12347", "Test Movie", "movie",
                5_400_000L, null, 2020);

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals("2020", item.getSecondTitle());
    }

    @Test
    public void from_nullOrEmptyRatingKey_returnsNull() {
        assertNull(PlexMediaItemAdapter.from(null));
        assertNull(PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "", "/k", "t", "movie", 0, null, 0)));
    }

    @Test
    public void from_show_mapsAsPlaylistContainer() {
        // PMS often sends duration on shows; adapter must zero it (Video.isMembersOnly).
        PlexMediaItem plex = new PlexMediaItemImpl(
                "2001", "/library/metadata/2001", "Breaking Bad", "show", 5_400_000L, null, 2008);

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals(MediaItem.TYPE_PLAYLIST, item.getType());
        assertNull(item.getVideoId());
        assertEquals("2001", item.getPlaylistId());
        assertEquals(0L, item.getDurationMs());
        assertTrue(item.hasUploads());
        assertFalse(item.isMovie());
    }

    @Test
    public void from_season_mapsAsPlaylistContainer() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "3001", "/library/metadata/3001", "Season 1", "season", 0, null, 2008);

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals(MediaItem.TYPE_PLAYLIST, item.getType());
        assertNull(item.getVideoId());
        assertEquals("3001", item.getPlaylistId());
        assertTrue(item.hasUploads());
    }

    @Test
    public void from_episode_mapsAsVideo() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "4001", "/library/metadata/4001", "Pilot", "episode", 3_600_000L, null, 2008);

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals(MediaItem.TYPE_VIDEO, item.getType());
        assertEquals("4001", item.getVideoId());
        assertNull(item.getPlaylistId());
        assertFalse(item.hasUploads());
    }

    @Test
    public void from_episode_usesShowNameAsTitle() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "4001", "/library/metadata/4001", "Pilot", "episode", 3_600_000L, null, 2008,
                0L, "3001", "2001", 1, "Season 1", "Breaking Bad", 1);

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals("Breaking Bad", item.getTitle());
        assertEquals("S01E01 Pilot · 2008", item.getSecondTitle());
    }

    @Test
    public void from_season_usesShowNameAsTitle() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "3001", "/library/metadata/3001", "Season 1", "season", 0L, null, 2008,
                0L, "2001", "2001", 1, "Breaking Bad", "Breaking Bad", 0);

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals("Breaking Bad", item.getTitle());
        assertEquals("Season 1 · 2008", item.getSecondTitle());
    }

    @Test
    public void from_nonMovie_isMovieFalse() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "9", "/library/metadata/9", "Episode", "episode", 0, null, 0);
        MediaItem item = PlexMediaItemAdapter.from(plex);
        assertNotNull(item);
        assertFalse(item.isMovie());
    }

    @Test
    public void equals_byRatingKey() {
        MediaItem a = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "42", "/k", "A", "movie", 0, null, 0));
        MediaItem b = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "42", "/k", "B", "movie", 0, null, 0));
        MediaItem c = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "99", "/k", "C", "movie", 0, null, 0));
        assertNotNull(a);
        assertEquals(a, b);
        assertFalse(a.equals(c));
    }

    @Test
    public void from_implementsPlexBackedMediaItem() {
        MediaItem item = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "1", "/k", "T", "movie", 0, null, 0));
        assertNotNull(item);
        assertTrue(item instanceof PlexBackedMediaItem);
    }

    @Test
    public void from_mapsViewOffsetToProgress() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "12345",
                "/library/metadata/12345",
                "Test Movie",
                "movie",
                5_400_000L,
                null,
                2020,
                540_000L); // 10%

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals(10, item.getPercentWatched());
        assertEquals(540, item.getStartTimeSeconds());
    }

    @Test
    public void fromLibraryBrowse_movie_usesAllMoviesDrawable() {
        MediaItem item = PlexMediaItemAdapter.fromLibraryBrowse(
                new PlexLibraryImpl("1", "Movies", "movie"));

        assertNotNull(item);
        assertEquals("android.resource://org.smarttube.beta/drawable/all_movies",
                item.getCardImageUrl());
        assertEquals("android.resource://org.smarttube.beta/drawable/all_movies",
                item.getBackgroundImageUrl());
        assertTrue(item.hasUploads());
    }

    @Test
    public void fromLibraryBrowse_show_usesAllTvShowsDrawable() {
        MediaItem item = PlexMediaItemAdapter.fromLibraryBrowse(
                new PlexLibraryImpl("2", "TV Shows", "show"));

        assertNotNull(item);
        assertEquals("android.resource://org.smarttube.beta/drawable/all_tv_shows",
                item.getCardImageUrl());
        assertEquals("android.resource://org.smarttube.beta/drawable/all_tv_shows",
                item.getBackgroundImageUrl());
    }

    @Test
    public void fromSearchEntry_movie_usesMarkerReloadKey() {
        MediaItem item = PlexMediaItemAdapter.fromSearchEntry(
                new PlexLibraryImpl("1", "Movies", "movie"), "Suchen");

        assertNotNull(item);
        assertEquals(PlexMediaItemAdapter.SEARCH_ENTRY_MOVIE, item.getReloadPageKey());
        assertNull(item.getVideoId());
        assertTrue(item.hasUploads());
    }

    @Test
    public void fromSearchEntry_show_usesMarkerReloadKey() {
        MediaItem item = PlexMediaItemAdapter.fromSearchEntry(
                new PlexLibraryImpl("2", "TV Shows", "show"), null);

        assertNotNull(item);
        assertEquals(PlexMediaItemAdapter.SEARCH_ENTRY_SHOW, item.getReloadPageKey());
        assertEquals("Suchen", item.getTitle());
    }

    @Test
    public void fromSearchEntry_movie_usesSearchMoviesDrawable() {
        MediaItem item = PlexMediaItemAdapter.fromSearchEntry(
                new PlexLibraryImpl("1", "Movies", "movie"), "Suchen");

        assertNotNull(item);
        assertEquals("android.resource://org.smarttube.beta/drawable/all_movies_search",
                item.getCardImageUrl());
        assertEquals("android.resource://org.smarttube.beta/drawable/all_movies_search",
                item.getBackgroundImageUrl());
    }

    @Test
    public void fromSearchEntry_show_usesSearchTvShowsDrawable() {
        MediaItem item = PlexMediaItemAdapter.fromSearchEntry(
                new PlexLibraryImpl("2", "TV Shows", "show"), null);

        assertNotNull(item);
        assertEquals("android.resource://org.smarttube.beta/drawable/all_tv_shows_search",
                item.getCardImageUrl());
        assertEquals("android.resource://org.smarttube.beta/drawable/all_tv_shows_search",
                item.getBackgroundImageUrl());
    }

    @Test
    public void badgeText_movie_isHoursMinutesSeconds() {
        MediaItem item = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "12345", "/library/metadata/12345", "Test Movie", "movie",
                5_400_000L, null, 2020));

        assertNotNull(item);
        assertEquals("1:30:00", item.getBadgeText());
    }

    @Test
    public void badgeText_episodeUnderAnHour_omitsHours() {
        MediaItem item = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "4001", "/library/metadata/4001", "Pilot", "episode", 754_000L, null, 2008));

        assertNotNull(item);
        assertEquals("12:34", item.getBadgeText());
    }

    @Test
    public void badgeText_withoutDuration_isNull() {
        MediaItem item = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "4002", "/library/metadata/4002", "Pilot", "episode", 0L, null, 2008));

        assertNotNull(item);
        assertNull(item.getBadgeText());
    }

    @Test
    public void badgeText_show_countsSeasons() {
        MediaItem item = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "2001", "/library/metadata/2001", "Breaking Bad", "show", 5_400_000L, null, 2008,
                0L, null, null, 0, null, null, 0, null, 3, 62));

        assertNotNull(item);
        assertEquals("3 Staffeln", item.getBadgeText());
    }

    @Test
    public void badgeText_showWithSingleSeason_isSingular() {
        MediaItem item = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "2002", "/library/metadata/2002", "Chernobyl", "show", 0L, null, 2019,
                0L, null, null, 0, null, null, 0, null, 1, 5));

        assertNotNull(item);
        assertEquals("1 Staffel", item.getBadgeText());
    }

    @Test
    public void badgeText_showWithoutSeasonCount_fallsBackToEpisodes() {
        MediaItem item = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "2003", "/library/metadata/2003", "Breaking Bad", "show", 0L, null, 2008,
                0L, null, null, 0, null, null, 0, null, 0, 62));

        assertNotNull(item);
        assertEquals("62 Folgen", item.getBadgeText());
    }

    @Test
    public void badgeText_season_countsEpisodes() {
        MediaItem item = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "3001", "/library/metadata/3001", "Season 1", "season", 0L, null, 2008,
                0L, "2001", "2001", 1, "Breaking Bad", "Breaking Bad", 0, null, 7, 7));

        assertNotNull(item);
        assertEquals("7 Folgen", item.getBadgeText());
    }

    @Test
    public void badgeText_containerWithoutCounts_isNull() {
        MediaItem item = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "2004", "/library/metadata/2004", "Breaking Bad", "show", 5_400_000L, null, 2008));

        assertNotNull(item);
        assertNull(item.getBadgeText());
    }

    @Test
    public void badgeText_stubCards_isNull() {
        MediaItem libraryBrowse = PlexMediaItemAdapter.fromLibraryBrowse(
                new PlexLibraryImpl("1", "Movies", "movie"));
        MediaItem searchEntry = PlexMediaItemAdapter.fromSearchEntry(
                new PlexLibraryImpl("1", "Movies", "movie"), "Suchen");

        assertNotNull(libraryBrowse);
        assertNotNull(searchEntry);
        assertNull(libraryBrowse.getBadgeText());
        assertNull(searchEntry.getBadgeText());
    }

    @Test
    public void fromSearchEntry_markerNeverCollidesWithRealLibraryKey() {
        // A real library.getKey() (PMS section id) must never equal a search marker,
        // otherwise PlexChannelUploadsPresenter.openChannel would misroute a real
        // library-browse stub to the search screen.
        assertFalse(PlexMediaItemAdapter.SEARCH_ENTRY_MOVIE.matches("\\d+"));
        assertFalse(PlexMediaItemAdapter.SEARCH_ENTRY_SHOW.matches("\\d+"));
    }
}
