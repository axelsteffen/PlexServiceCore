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
    public void fromMetadata_mapsParentAndIndex() {
        PlexMetadata episode = GSON.fromJson(
                "{\"ratingKey\":\"4002\",\"key\":\"/library/metadata/4002\",\"title\":\"E2\","
                        + "\"type\":\"episode\",\"parentRatingKey\":\"3001\","
                        + "\"grandparentRatingKey\":\"2001\",\"index\":2,"
                        + "\"parentIndex\":1,\"parentTitle\":\"Season 1\","
                        + "\"grandparentTitle\":\"Breaking Bad\"}",
                PlexMetadata.class);
        PlexMediaItemImpl item = PlexMediaItemImpl.fromMetadata(episode, "https://plex:32400/", "tok");
        assertNotNull(item);
        assertEquals("3001", item.getParentRatingKey());
        assertEquals("2001", item.getGrandparentRatingKey());
        assertEquals(2, item.getIndex());
        assertEquals(1, item.getParentIndex());
        assertEquals("Season 1", item.getParentTitle());
        assertEquals("Breaking Bad", item.getGrandparentTitle());
    }
}
