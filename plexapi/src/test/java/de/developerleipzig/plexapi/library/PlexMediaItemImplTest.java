package de.developerleipzig.plexapi.library;

import com.google.gson.Gson;
import de.developerleipzig.plexapi.network.dto.PlexMetadata;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PlexMediaItemImplTest {
    private static final Gson GSON = new Gson();

    @Test
    public void resolveThumbPath_prefersThumbThenParentThenArt() {
        PlexMetadata withThumb = GSON.fromJson(
                "{\"ratingKey\":\"1\",\"thumb\":\"/t\",\"parentThumb\":\"/p\",\"art\":\"/a\"}",
                PlexMetadata.class);
        assertEquals("/t", PlexMediaItemImpl.resolveThumbPath(withThumb));

        PlexMetadata parentOnly = GSON.fromJson(
                "{\"ratingKey\":\"2\",\"parentThumb\":\"/p\",\"art\":\"/a\"}",
                PlexMetadata.class);
        assertEquals("/p", PlexMediaItemImpl.resolveThumbPath(parentOnly));

        PlexMetadata artOnly = GSON.fromJson(
                "{\"ratingKey\":\"3\",\"art\":\"/a\"}",
                PlexMetadata.class);
        assertEquals("/a", PlexMediaItemImpl.resolveThumbPath(artOnly));
    }

    @Test
    public void resolveThumbPath_fallsBackToMetadataEndpoint() {
        PlexMetadata bare = GSON.fromJson("{\"ratingKey\":\"1049\"}", PlexMetadata.class);
        assertEquals("/library/metadata/1049/thumb", PlexMediaItemImpl.resolveThumbPath(bare));
    }

    @Test
    public void fromMetadata_buildsAbsoluteFallbackThumb() {
        PlexMetadata bare = GSON.fromJson(
                "{\"ratingKey\":\"1049\",\"key\":\"/library/metadata/1049\",\"title\":\"X\"}",
                PlexMetadata.class);
        PlexMediaItemImpl item = PlexMediaItemImpl.fromMetadata(bare, "https://plex:32400/", "tok");
        assertNotNull(item);
        assertTrue(item.getThumbUrl().contains("/library/metadata/1049/thumb"));
        assertTrue(item.getThumbUrl().contains("X-Plex-Token=tok"));
    }
}
