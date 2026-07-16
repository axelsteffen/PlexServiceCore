package com.liskovsoft.plexapi.server;

import com.liskovsoft.plexapi.network.PlexUrlHelper;
import com.liskovsoft.plexapi.network.dto.PlexResource;
import com.liskovsoft.plexapi.network.dto.PlexResourceConnection;
import com.liskovsoft.plexserviceinterfaces.data.PlexServer;

import java.net.URI;
import java.util.List;
import java.util.Locale;

/**
 * Immutable {@link PlexServer} mapped from a plex.tv resource.
 */
public final class PlexServerImpl implements PlexServer {
    private final String mClientIdentifier;
    private final String mName;
    private final String mBaseUrl;
    private final String mAccessToken;
    private final boolean mOwned;
    private final boolean mOnline;

    public PlexServerImpl(String clientIdentifier, String name, String baseUrl,
                          String accessToken, boolean owned, boolean online) {
        mClientIdentifier = clientIdentifier;
        mName = name;
        mBaseUrl = baseUrl != null ? PlexUrlHelper.normalizeBaseUrl(baseUrl) : null;
        mAccessToken = accessToken;
        mOwned = owned;
        mOnline = online;
    }

    /**
     * Maps a PMS resource, or {@code null} if it is not a server / has no usable connection.
     */
    public static PlexServerImpl fromResource(PlexResource resource) {
        if (resource == null || !resource.isServer()) {
            return null;
        }
        String baseUrl = pickBaseUrl(resource.getConnections());
        if (baseUrl == null) {
            return null;
        }
        return new PlexServerImpl(
                resource.getClientIdentifier(),
                resource.getName(),
                baseUrl,
                resource.getAccessToken(),
                resource.isOwned(),
                resource.isPresence());
    }

    /**
     * Picks a PMS base URI from plex.tv resource connections.
     * <p>
     * If any {@code local=false} (remote) connection exists, private {@code local=true}
     * URIs are ignored — they are only reachable on the server's own LAN/Docker network
     * (e.g. {@code 172.18.0.3} / {@code *.plex.direct} with embedded private IP).
     * Remote clients must use the public/remote connection.
     * <p>
     * If only local connections exist, prefer typical LAN addresses over Docker bridges.
     */
    static String pickBaseUrl(List<PlexResourceConnection> connections) {
        if (connections == null || connections.isEmpty()) {
            return null;
        }

        boolean hasRemote = hasRemoteConnection(connections);

        PlexResourceConnection best = null;
        int bestScore = Integer.MIN_VALUE;

        for (PlexResourceConnection connection : connections) {
            if (connection == null) {
                continue;
            }
            String uri = connection.getUri();
            if (uri == null || uri.isEmpty()) {
                continue;
            }

            // Remote access: never use server-LAN/Docker "local" URIs.
            if (hasRemote && connection.isLocal() && isPrivateConnection(connection)) {
                continue;
            }

            int score = scoreConnection(connection);
            if (score > bestScore) {
                bestScore = score;
                best = connection;
            }
        }

        return best != null ? best.getUri() : null;
    }

    private static boolean hasRemoteConnection(List<PlexResourceConnection> connections) {
        for (PlexResourceConnection connection : connections) {
            if (connection == null || connection.isLocal()) {
                continue;
            }
            String uri = connection.getUri();
            if (uri != null && !uri.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    static int scoreConnection(PlexResourceConnection connection) {
        int score = 0;
        if (!connection.isLocal()) {
            score += 30; // remote path for clients outside the server LAN
        } else {
            score += 10;
        }
        if (isHttps(connection)) {
            score += 2;
        }

        String host = resolveHost(connection);
        if (host == null || host.isEmpty()) {
            return score;
        }

        String hostLower = host.toLowerCase(Locale.US);
        if (hostLower.endsWith(".plex.direct")) {
            String embedded = ipv4FromPlexDirectHost(hostLower);
            if (embedded != null && isPrivateIpv4(embedded)) {
                // Private IP baked into plex.direct — useless off-LAN / Docker bridge.
                score -= 40;
            } else {
                // Public IP via plex.direct — normal remote access.
                score += 5;
            }
        } else if (isIpv4Literal(hostLower)) {
            if (isPrivateIpv4(hostLower)) {
                score += scorePrivateLanPreference(hostLower);
            } else {
                score += 5; // public IP literal
            }
        }

        return score;
    }

    /** Among private locals only: prefer 192.168 over 10.x over Docker 172.16/12. */
    private static int scorePrivateLanPreference(String ip) {
        if (isRfc1918_192_168(ip)) {
            return 8;
        }
        if (isRfc1918_10(ip)) {
            return 4;
        }
        if (isRfc1918_172_16(ip)) {
            return -20;
        }
        return 0;
    }

    private static boolean isPrivateConnection(PlexResourceConnection connection) {
        String host = resolveHost(connection);
        if (host == null || host.isEmpty()) {
            return false;
        }
        String hostLower = host.toLowerCase(Locale.US);
        if (hostLower.endsWith(".plex.direct")) {
            String embedded = ipv4FromPlexDirectHost(hostLower);
            return embedded != null && isPrivateIpv4(embedded);
        }
        return isIpv4Literal(hostLower) && isPrivateIpv4(hostLower);
    }

    private static boolean isPrivateIpv4(String ip) {
        return isRfc1918_192_168(ip) || isRfc1918_10(ip) || isRfc1918_172_16(ip);
    }

    private static String resolveHost(PlexResourceConnection connection) {
        String address = connection.getAddress();
        if (address != null && !address.isEmpty()) {
            return address.trim();
        }
        return hostFromUri(connection.getUri());
    }

    private static String hostFromUri(String uri) {
        if (uri == null || uri.isEmpty()) {
            return null;
        }
        try {
            URI parsed = URI.create(uri);
            return parsed.getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * {@code 172-18-0-3.<server-hash>.plex.direct} → {@code 172.18.0.3}
     */
    static String ipv4FromPlexDirectHost(String host) {
        if (host == null) {
            return null;
        }
        String lower = host.toLowerCase(Locale.US);
        int plexDirect = lower.indexOf(".plex.direct");
        if (plexDirect <= 0) {
            return null;
        }
        String before = lower.substring(0, plexDirect);
        int lastDot = before.lastIndexOf('.');
        String ipPart = lastDot >= 0 ? before.substring(0, lastDot) : before;
        String[] labels = ipPart.split("-");
        if (labels.length != 4) {
            return null;
        }
        String candidate = labels[0] + "." + labels[1] + "." + labels[2] + "." + labels[3];
        return isIpv4Literal(candidate) ? candidate : null;
    }

    private static boolean isIpv4Literal(String value) {
        if (value == null) {
            return false;
        }
        String[] parts = value.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            try {
                int n = Integer.parseInt(part);
                if (n < 0 || n > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRfc1918_192_168(String ip) {
        String[] p = ip.split("\\.");
        return "192".equals(p[0]) && "168".equals(p[1]);
    }

    private static boolean isRfc1918_10(String ip) {
        return ip.startsWith("10.");
    }

    private static boolean isRfc1918_172_16(String ip) {
        String[] p = ip.split("\\.");
        if (!"172".equals(p[0])) {
            return false;
        }
        try {
            int second = Integer.parseInt(p[1]);
            return second >= 16 && second <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isHttps(PlexResourceConnection connection) {
        String protocol = connection.getProtocol();
        if (protocol != null && protocol.equalsIgnoreCase("https")) {
            return true;
        }
        String uri = connection.getUri();
        return uri != null && uri.regionMatches(true, 0, "https://", 0, 8);
    }

    @Override
    public String getClientIdentifier() {
        return mClientIdentifier;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getBaseUrl() {
        return mBaseUrl;
    }

    @Override
    public String getAccessToken() {
        return mAccessToken;
    }

    @Override
    public boolean isOwned() {
        return mOwned;
    }

    @Override
    public boolean isOnline() {
        return mOnline;
    }
}
