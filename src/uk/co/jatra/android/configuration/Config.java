package uk.co.jatra.android.configuration;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

/**
 * Config class that reads configuration information from a known file res/values/config.xml
 * The config.xml is expected to be modified, or even created at compile time.
 * eg a -pre-compile target can be added using the task echoxml, see the custom_rules.xml file.
 * <pre>
 * Parameters
    build_num  
    version  
    build_type one of the enumerated constants below in BuildType
    software_type  done of the enumerated constants below in SoftwareType
    expiry    format must be  Jul 1, 2012  (ie MEDIUM, Locale, US)
    </pre>
 *  Note, no expiration action is implicit in this file, it is up to the application to use it.
 */

public class Config {
    private static Config instance;
    

    private static DateFormat sDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
    
    /** Unknown build or software type. */
    public static final int UNKNOWN = 0;
    
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


    private static final String TAG = "Config";
    
    /** The permitted Software types in the config.xml */
    public enum SoftwareType {
        UNKNOWN,
        EXPERIMENTAL,
        DEVELOPMENT,
        TRIAL,
        PRE_PRELEASE,
        RELEASE
    };
    /** The permitted Build types in the config.xml */
    public enum BuildType {
        UNKNOWN,
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
   
    /**
     * private constructor reads from resource files.
     * Expected that the config values will be in config.xml
     * 
     * @param context
     */
    private Config(Context context) {
        Resources resources = context.getResources();
        
        String softwareType = resources.getString(R.string.software_type);
        mSoftwareType = softwareType == null ? SoftwareType.UNKNOWN : SoftwareType.valueOf(softwareType);
        
        String buildType = resources.getString(R.string.build_type);
        mBuildType = buildType == null ? BuildType.UNKNOWN : BuildType.valueOf(buildType);
        
        mExpiryDate = resources.getString(R.string.expiry);
        Date date = null;
        try {
            date = sDateFormat.parse(mExpiryDate);
        } catch (Exception e) {
            Log.e(TAG, "expiry date invalid"+mExpiryDate+". Using max date");
        }
        mExpiry = date == null ? Long.MAX_VALUE : date.getTime();

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
     * Test for expiration, with exception thrown if expired.
     */
    public void checkExpired() {
        if (System.currentTimeMillis() > mExpiry) {
            throw new RuntimeException("Expired");
        }
    }
    
    /** 
     * Test for expiration.
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > mExpiry;
    }
    
    /**
     * @param valid String to be returned if not yet expired
     * @param expired String to be returned if expired
     * @return whichever String is appropriate according to expiry date.
     */
    public String getStringPerExpiry(String valid, String expired) {
        return isExpired() ? expired : valid;
    }
    
    /**
     * Allow a choice of two different actions according to whether the expiry date has been reached.
     * The chosen runnable is run synchronously on the calling thread.
     * @param valid A runnable that is run if the expired date has not been reached.
     * @param expired A runnable that is run if the expiry date has been reached
     */
    public void executePerExpriry(Runnable valid, Runnable expired) {
        if (isExpired()) {
            valid.run();
        } else {
            expired.run();
        }
    }
    
    /**
     * Allow a choice of two different actions according to whether the expiry date has been reached.
     * The chosen Callable is run synchronously on the calling thread.
     * @param valid A Callable that is run if the expired date has not been reached.
     * @param expired A Callable that is run if the expiry date has been reached
     */
    public <V> V executePerExpiry(Callable<V> valid, Callable<V> expired) throws Exception {
        return isExpired() ? expired.call() : valid.call();
    }
}
