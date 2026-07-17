package de.developerleipzig.plexapi.service;

import de.developerleipzig.plexapi.network.PlexRetrofitHelper;
import de.developerleipzig.plexapi.network.PlexTvResourcesApi;
import de.developerleipzig.plexapi.network.dto.PlexResource;
import de.developerleipzig.plexapi.prefs.PlexPrefs;
import de.developerleipzig.plexapi.server.PlexServerImpl;
import de.developerleipzig.plexserviceinterfaces.PlexServerService;
import de.developerleipzig.plexserviceinterfaces.data.PlexServer;
import com.liskovsoft.sharedutils.mylogger.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Response;

/**
 * Discovers PMS servers via plex.tv {@code /api/v2/resources} (Phase 1.4).
 */
public class PlexServerServiceImpl implements PlexServerService {
    private static final String TAG = PlexServerServiceImpl.class.getSimpleName();

    private final PlexTvResourcesApi mApi;
    private final PlexPrefs mPrefs;

    public PlexServerServiceImpl() {
        this(PlexRetrofitHelper.createPlexTvApi(PlexTvResourcesApi.class), null);
    }

    /** Package-visible for tests. */
    PlexServerServiceImpl(PlexTvResourcesApi api, PlexPrefs prefs) {
        mApi = api;
        mPrefs = prefs;
    }

    private PlexPrefs prefs() {
        return mPrefs != null ? mPrefs : PlexPrefs.instance();
    }

    @Override
    public Observable<List<PlexServer>> getServersObserve() {
        return Observable.fromCallable(this::fetchServers);
    }

    private List<PlexServer> fetchServers() throws IOException {
        Response<List<PlexResource>> response = mApi.getResources(1, 1).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Failed to list Plex resources: HTTP " + response.code());
        }

        List<PlexResource> body = response.body();
        if (body == null || body.isEmpty()) {
            return Collections.emptyList();
        }

        List<PlexServer> servers = new ArrayList<>();
        for (PlexResource resource : body) {
            PlexServerImpl server = PlexServerImpl.fromResource(resource);
            if (server != null) {
                servers.add(server);
            }
        }

        Log.d(TAG, "Discovered " + servers.size() + " Plex server(s)");
        return servers;
    }

    @Override
    public PlexServer getSelectedServer() {
        return prefs().getSelectedServer();
    }

    @Override
    public void selectServer(PlexServer server) {
        prefs().setSelectedServer(server);
        if (server != null) {
            Log.d(TAG, "Selected server: " + server.getName() + " @ " + server.getBaseUrl());
        } else {
            Log.d(TAG, "Cleared selected server");
        }
    }
}
