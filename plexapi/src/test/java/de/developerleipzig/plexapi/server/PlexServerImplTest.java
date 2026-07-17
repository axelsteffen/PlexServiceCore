package de.developerleipzig.plexapi.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.developerleipzig.plexapi.network.dto.PlexResource;
import de.developerleipzig.plexapi.network.dto.PlexResourceConnection;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PlexServerImplTest {
    private static final Gson GSON = new Gson();
    private static final Type CONNECTIONS_TYPE = new TypeToken<List<PlexResourceConnection>>() {}.getType();

    @Test
    public void pickBaseUrl_prefersRemoteWhenPrivateLocalAlsoPresent() {
        // Remote client: ignore server-LAN / Docker "local" URIs.
        List<PlexResourceConnection> connections = GSON.fromJson("["
                + "{\"protocol\":\"https\","
                + "\"address\":\"172.18.0.3\",\"port\":32400,"
                + "\"uri\":\"https://172-18-0-3.abc123.plex.direct:32400\",\"local\":true},"
                + "{\"protocol\":\"http\","
                + "\"address\":\"192.168.32.10\",\"port\":32400,"
                + "\"uri\":\"http://192.168.32.10:32400\",\"local\":true},"
                + "{\"protocol\":\"https\","
                + "\"address\":\"1.2.3.4\",\"port\":32400,"
                + "\"uri\":\"https://1-2-3-4.abc123.plex.direct:32400\",\"local\":false}"
                + "]", CONNECTIONS_TYPE);

        assertEquals("https://1-2-3-4.abc123.plex.direct:32400",
                PlexServerImpl.pickBaseUrl(connections));
    }

    @Test
    public void pickBaseUrl_prefersRemoteHttpsOverHostnameRemote() {
        List<PlexResourceConnection> connections = GSON.fromJson("["
                + "{\"protocol\":\"http\",\"uri\":\"http://remote.example:32400\",\"local\":false},"
                + "{\"protocol\":\"https\",\"uri\":\"https://remote.example:32400\",\"local\":false}"
                + "]", CONNECTIONS_TYPE);

        assertEquals("https://remote.example:32400", PlexServerImpl.pickBaseUrl(connections));
    }

    @Test
    public void pickBaseUrl_whenOnlyLocal_prefersLanOverDockerPlexDirect() {
        List<PlexResourceConnection> connections = GSON.fromJson("["
                + "{\"protocol\":\"https\","
                + "\"address\":\"172.18.0.3\",\"port\":32400,"
                + "\"uri\":\"https://172-18-0-3.abc123.plex.direct:32400\",\"local\":true},"
                + "{\"protocol\":\"http\","
                + "\"address\":\"192.168.32.10\",\"port\":32400,"
                + "\"uri\":\"http://192.168.32.10:32400\",\"local\":true}"
                + "]", CONNECTIONS_TYPE);

        assertEquals("http://192.168.32.10:32400", PlexServerImpl.pickBaseUrl(connections));
    }

    @Test
    public void ipv4FromPlexDirectHost_parsesEmbeddedIp() {
        assertEquals("172.18.0.3",
                PlexServerImpl.ipv4FromPlexDirectHost(
                        "172-18-0-3.24e04906fce74443a891d4331b4e4aa5.plex.direct"));
        assertNull(PlexServerImpl.ipv4FromPlexDirectHost("example.com"));
    }

    @Test
    public void pickBaseUrl_returnsNullWhenEmpty() {
        assertNull(PlexServerImpl.pickBaseUrl(null));
        assertNull(PlexServerImpl.pickBaseUrl(Collections.emptyList()));
    }

    @Test
    public void fromResource_skipsNonServer() {
        PlexResource resource = GSON.fromJson(
                "{\"name\":\"Phone\",\"product\":\"Plex for Android\",\"provides\":\"client\","
                        + "\"connections\":[{\"uri\":\"http://1.2.3.4:32400\",\"local\":true}]}",
                PlexResource.class);
        assertNull(PlexServerImpl.fromResource(resource));
    }

    @Test
    public void fromResource_mapsServer() {
        PlexResource resource = GSON.fromJson(
                "{\"name\":\"Home\",\"product\":\"Plex Media Server\",\"provides\":\"server\","
                        + "\"clientIdentifier\":\"srv-1\",\"owned\":true,\"presence\":true,"
                        + "\"accessToken\":\"server-token\","
                        + "\"connections\":["
                        + "{\"protocol\":\"https\",\"uri\":\"https://10.0.0.2:32400\",\"local\":true}"
                        + "]}",
                PlexResource.class);

        PlexServerImpl server = PlexServerImpl.fromResource(resource);
        assertNotNull(server);
        assertEquals("srv-1", server.getClientIdentifier());
        assertEquals("Home", server.getName());
        assertEquals("https://10.0.0.2:32400/", server.getBaseUrl());
        assertEquals("server-token", server.getAccessToken());
        assertTrue(server.isOwned());
        assertTrue(server.isOnline());
    }
}
