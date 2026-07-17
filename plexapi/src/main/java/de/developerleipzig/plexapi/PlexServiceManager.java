package de.developerleipzig.plexapi;

import android.content.Context;

import de.developerleipzig.plexapi.prefs.PlexPrefs;
import de.developerleipzig.plexapi.service.PlexLibraryServiceImpl;
import de.developerleipzig.plexapi.service.PlexMediaServiceImpl;
import de.developerleipzig.plexapi.service.PlexServerServiceImpl;
import de.developerleipzig.plexapi.service.PlexSignInServiceImpl;
import de.developerleipzig.plexserviceinterfaces.PlexLibraryService;
import de.developerleipzig.plexserviceinterfaces.PlexMediaService;
import de.developerleipzig.plexserviceinterfaces.PlexServerService;
import de.developerleipzig.plexserviceinterfaces.PlexSignInService;
import com.liskovsoft.sharedutils.mylogger.Log;

/**
 * Fork-only entry point for Plex services.
 * Sign-in, server discovery, library listing, and stream resolve are live.
 */
public final class PlexServiceManager implements de.developerleipzig.plexserviceinterfaces.PlexServiceManager {
    private static final String TAG = PlexServiceManager.class.getSimpleName();
    private static PlexServiceManager sInstance;

    private final PlexSignInService mSignInService;
    private final PlexServerService mServerService;
    private final PlexLibraryService mLibraryService;
    private final PlexMediaService mMediaService;

    private PlexServiceManager() {
        Log.d(TAG, "Starting...");
        mSignInService = new PlexSignInServiceImpl();
        mServerService = new PlexServerServiceImpl();
        mLibraryService = new PlexLibraryServiceImpl();
        mMediaService = new PlexMediaServiceImpl();
    }

    /**
     * Optional early init so {@link PlexPrefs} has a Context before the first plex.tv call
     * (also covered once {@code GlobalPreferences} is ready).
     */
    public static void init(Context context) {
        if (context != null) {
            PlexPrefs.instance(context);
        }
    }

    public static de.developerleipzig.plexserviceinterfaces.PlexServiceManager instance() {
        if (sInstance == null) {
            sInstance = new PlexServiceManager();
        }
        return sInstance;
    }

    @Override
    public PlexSignInService getSignInService() {
        return mSignInService;
    }

    @Override
    public PlexServerService getServerService() {
        return mServerService;
    }

    @Override
    public PlexLibraryService getLibraryService() {
        return mLibraryService;
    }

    @Override
    public PlexMediaService getMediaService() {
        return mMediaService;
    }
}
