package uk.co.jatra.android.configuration;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import android.content.Context;
import android.content.res.Resources;

public class Config {
    private static Config instance;
    
    private static DateFormat sDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
    
    /** Development software - not a formal tracked build. */
    public static final int DEVELOPMENT = 0x1;
    /** Experimental software - never to be released. */
    public static final int EXPERIMENTAL = DEVELOPMENT<<1;
    /** Trial software - released with a fixed lifetime. */
    public static final int TRIAL = DEVELOPMENT<<2;
    /** PreRelease software - Beta, or release candidate. */
    public static final int PRE_RELEASE = DEVELOPMENT<<3;
    /** Release software - The real McCoy. */
    public static final int RELEASE = DEVELOPMENT<<4;

    /** Built using Eclipse */
    public static final int ECLIPSE = DEVELOPMENT<<5;
    /** Built using Ant */
    public static final int ANT = DEVELOPMENT<<6;
    /** Built using Gerrit trigger */
    public static final int GERRIT = DEVELOPMENT<<7;
    /** Built usingAuto build */
    public static final int AUTO = DEVELOPMENT<<8;
    /** Built using Master */
    public static final int MASTER = DEVELOPMENT<<9;
    
    public enum SoftwareType {
        EXPERIMENTAL,
        DEVELOPMENT,
        TRIAL,
        PRE_PRELEASE,
        RELEASE
    };
    public enum BuildType {
        ECLIPSE,
        ANT,
        GERRIT,
        AUTO,
        MASTER
    };
    private final SoftwareType mSoftwareType;
    private final BuildType mBuildType;
    private final String mExpiryDate;
    private final long mExpiry;
   
    
    private Config(Context context) {
        Resources resources = context.getResources();
        mSoftwareType = SoftwareType.valueOf(resources.getString(R.string.software_type));
        mBuildType = BuildType.valueOf(resources.getString(R.string.build_type));
        mExpiryDate = resources.getString(R.string.expiry);
        try {
            Date date = sDateFormat.parse(mExpiryDate);
            mExpiry = date.getTime();
        } catch (Exception e) {
            throw new IllegalArgumentException("expiry date invalid"+mExpiryDate);
        }

    }
    
    public static Config initConfig(Context context) {
        if (instance != null) {
            throw new IllegalStateException("Already initialised");
        } else {
            instance = new Config(context);
        }
        return instance;
    }
    
    public static Config getConfig() {
        return instance;
    }
    
    /**
     * Get the software type, for use when different messages etc should be displayed.
     * @return the Current softweare type according to the config.
     */
    public SoftwareType getSoftwareType() {
        return mSoftwareType;
    }
    
    /**
     * Get the build type, for use when different messages etc should be displayed.
     * @return the Current build type according to the config.
     */
    public BuildType getBuildType() {
        return mBuildType;
    }
    
    /**
     * Get the expiry date
     */
    public String getExpiryDate() {
        return mExpiryDate;
    }
    
    /** 
     * Test for expiration
     */
    public void checkExpired() {
        if (System.currentTimeMillis() > mExpiry) {
            throw new RuntimeException("Expired");
        }
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > mExpiry;
    }
    
    public String getStringPerExpiry(String valid, String expired) {
        return isExpired() ? expired : valid;
    }
    
    public void executePerExpriry(Runnable valid, Runnable expired) {
        if (isExpired()) {
            valid.run();
        } else {
            expired.run();
        }
    }
    
    public <V> V executePerExpiry(Callable<V> valid, Callable<V> expired) throws Exception {
        return isExpired() ? expired.call() : valid.call();
    }
}
