package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.api.Emoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {

	public static final String MOD_ID = "emojiful";
	public static final String MOD_NAME = "Emojiful";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final Map<String, List<Emoji>> EMOJI_MAP = new HashMap<>();
	public static final List<Emoji> EMOJI_LIST = new ArrayList<>();
	public static boolean error = false;
}