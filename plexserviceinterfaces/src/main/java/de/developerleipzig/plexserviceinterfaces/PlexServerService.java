package de.developerleipzig.plexserviceinterfaces;

import de.developerleipzig.plexserviceinterfaces.data.PlexServer;

import java.util.List;

import io.reactivex.Observable;

/**
 * Discover and select a Plex Media Server.
 */
public interface PlexServerService {
    Observable<List<PlexServer>> getServersObserve();

    PlexServer getSelectedServer();

    void selectServer(PlexServer server);
}
