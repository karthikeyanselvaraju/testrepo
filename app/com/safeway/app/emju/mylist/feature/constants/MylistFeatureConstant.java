package com.safeway.app.emju.mylist.feature.constants;

import com.safeway.app.emju.feature.FeatureConstants;

import play.Configuration;
import play.Play;

public class MylistFeatureConstant extends FeatureConstants{
	
	private static Configuration config = Play.application().configuration();
	public static final boolean NAI_ENABLE_SC_FEATURE = convertStringtoBoolean(config.getString("gallery.nai.sc.enable"));
	public static final boolean NAI_ENABLE_PD_FEATURE = convertStringtoBoolean(config.getString("gallery.nai.pd.enable"));

}
