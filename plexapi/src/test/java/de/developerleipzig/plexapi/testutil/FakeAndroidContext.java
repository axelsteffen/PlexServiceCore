package de.developerleipzig.plexapi.testutil;

import android.content.Context;
import android.content.SharedPreferences;

import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Minimal fake {@link Context} for {@code PlexPrefs}-style prefs classes in plain JVM unit tests.
 * <p>
 * Replaces Robolectric's {@code RuntimeEnvironment.application}: this module's classpath also
 * carries SmartTube's shaded ASM ({@code smarttube:latest}), which breaks Robolectric's
 * bytecode instrumentation (see {@code gradle/plexapi.gradle.kts}). Only {@code
 * getSharedPreferences}/{@code getApplicationContext} are backed by real behavior — enough for
 * {@code SharedPreferencesBase} subclasses; every other {@link Context} method returns Mockito's
 * default (null/0/false).
 */
public final class FakeAndroidContext {
    private FakeAndroidContext() {
    }

    public static Context create() {
        Context context = Mockito.mock(Context.class);
        Map<String, SharedPreferences> prefsByName = new HashMap<>();
        when(context.getApplicationContext()).thenReturn(context);
        when(context.getSharedPreferences(anyString(), anyInt())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            return prefsByName.computeIfAbsent(name, n -> new FakeSharedPreferences());
        });
        return context;
    }
}
