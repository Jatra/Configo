package uk.co.jatra.android.configuration;

import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity
{
	private Config mConfig;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		mConfig = Config.initConfig(this);
	}
}
