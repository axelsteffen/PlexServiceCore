package de.developerleipzig.plexapi.testutil;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** In-memory {@link SharedPreferences} backing {@link FakeAndroidContext} in plain JVM tests. */
final class FakeSharedPreferences implements SharedPreferences {
    private final Map<String, Object> mValues = new HashMap<>();

    @Override
    public Map<String, ?> getAll() {
        return new HashMap<>(mValues);
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        Object value = mValues.get(key);
        return value != null ? (String) value : defValue;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        Object value = mValues.get(key);
        return value != null ? (Set<String>) value : defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        Object value = mValues.get(key);
        return value != null ? (Integer) value : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        Object value = mValues.get(key);
        return value != null ? (Long) value : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        Object value = mValues.get(key);
        return value != null ? (Float) value : defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        Object value = mValues.get(key);
        return value != null ? (Boolean) value : defValue;
    }

    @Override
    public boolean contains(String key) {
        return mValues.containsKey(key);
    }

    @Override
    public Editor edit() {
        return new FakeEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        // No-op: change listeners aren't exercised by these tests.
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        // No-op: change listeners aren't exercised by these tests.
    }

    private final class FakeEditor implements Editor {
        private final Map<String, Object> mPending = new HashMap<>(mValues);
        private boolean mClear;

        @Override
        public Editor putString(String key, @Nullable String value) {
            mPending.put(key, value);
            return this;
        }

        @Override
        public Editor putStringSet(String key, @Nullable Set<String> values) {
            mPending.put(key, values != null ? new HashSet<>(values) : null);
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            mPending.put(key, value);
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            mPending.put(key, value);
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            mPending.put(key, value);
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            mPending.put(key, value);
            return this;
        }

        @Override
        public Editor remove(String key) {
            mPending.remove(key);
            return this;
        }

        @Override
        public Editor clear() {
            mClear = true;
            mPending.clear();
            return this;
        }

        @Override
        public boolean commit() {
            apply();
            return true;
        }

        @Override
        public void apply() {
            if (mClear) {
                mValues.clear();
            }
            mValues.putAll(mPending);
        }
    }
}
