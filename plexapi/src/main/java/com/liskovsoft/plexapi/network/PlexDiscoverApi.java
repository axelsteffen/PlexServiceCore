package com.liskovsoft.plexapi.network;

import com.liskovsoft.plexapi.network.dto.MediaContainerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Plex Discover provider (watchlist and related account shelves).
 * Base URL = {@link PlexRetrofitHelper#DEFAULT_DISCOVER_BASE_URL}.
 */
public interface PlexDiscoverApi {
    @GET("library/sections/watchlist/all")
    Call<MediaContainerResponse> getWatchlist(
            @Query("type") Integer type,
            @Header(PlexHeaders.CONTAINER_START) Integer containerStart,
            @Header(PlexHeaders.CONTAINER_SIZE) Integer containerSize,
            @Header(PlexHeaders.TOKEN) String token);
}
