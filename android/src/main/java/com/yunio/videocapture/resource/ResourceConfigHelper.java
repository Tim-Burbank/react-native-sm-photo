package com.yunio.videocapture.resource;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.yunio.videocapture.resource.entity.ColorConfig;
import com.yunio.videocapture.resource.entity.IConfig;
import com.yunio.videocapture.resource.entity.ImageConfig;
import com.yunio.videocapture.resource.entity.StringConfig;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ResourceConfigHelper {
    private final static String PLATFORM_ANDROID = "android";
    private final static String LANGUAGE_CHINA = "zh";
    private final static String RESOURCE_STRING = "string";
    private final static String RESOURCE_IMAGE = "image";
    private final static String RESOURCE_COLOR = "color";

    //    private String LANGUAGES[] = {LANGUAGE_CHINA, LANGUAGE_ENGLISH, LANGUAGE_VIETNAM};

    private String local = LANGUAGE_CHINA;

    private Map<String, IConfig> mStringConfig = new HashMap<>();
    private Map<String, IConfig> mImageConfig = new HashMap<>();
    private Map<String, IConfig> mColorConfig = new HashMap<>();
    private Map<String, String> mDefaultConfig = new HashMap<>();

    private static ResourceConfigHelper instance = new ResourceConfigHelper();

    public ResourceConfigHelper() {

    }

    public static ResourceConfigHelper getInstance() {
        return instance;
    }

    public void changeLocal(String local) {
        this.local = local;
    }

    public void initStringConfig(String config, String defaultLanguage) {
        initConfig(RESOURCE_STRING, config);
        mDefaultConfig.put(RESOURCE_STRING, defaultLanguage);
    }

    public void initImageConfig(String config, String defaultLanguage) {
        initConfig(RESOURCE_IMAGE, config);
        mDefaultConfig.put(RESOURCE_IMAGE, defaultLanguage);
    }

    public void initColorConfig(String config, String defaultLanguage) {
        initConfig(RESOURCE_COLOR, config);
        mDefaultConfig.put(RESOURCE_COLOR, defaultLanguage);
    }

    private void initConfig(String type, String config) {
//        String android = DataParser.toStringByKey(config, PLATFORM_ANDROID);
        if (!TextUtils.isEmpty(config)) {
            switch (type) {
                case RESOURCE_STRING:
                    mStringConfig = DataParser.parser(config, new TypeToken<Map<String, StringConfig>>() {
                    }.getType());
                    //                    mStringConfig.put(language, DataParser.parser(languageConfig, StringConfig.class));
                    break;
                case RESOURCE_IMAGE:
                    mImageConfig = DataParser.parser(config, new TypeToken<Map<String, ImageConfig>>() {
                    }.getType());
                    //                    mImageConfig.put(language, DataParser.parser(languageConfig, ImageConfig.class));
                    break;
                case RESOURCE_COLOR:
                    mColorConfig = DataParser.parser(config, new TypeToken<Map<String, ColorConfig>>() {
                    }.getType());
                    //                    mColorConfig.put(language, DataParser.parser(languageConfig, ColorConfig.class));
                    break;
            }
        }
    }

    private IConfig getCurrentConfig(String type) {
        Map<String, IConfig> currentMap = null;
        switch (type) {
            case RESOURCE_STRING:
                currentMap = mStringConfig;
                break;
            case RESOURCE_IMAGE:
                currentMap = mImageConfig;
                break;
            case RESOURCE_COLOR:
                currentMap = mColorConfig;
                break;
        }
        if (currentMap != null) {
            IConfig current = currentMap.get(Locale.getDefault().getLanguage());
            if (current == null) {//获取默认
                String defaultLanguage = mDefaultConfig.get(type);
                if (defaultLanguage == null) {
                    defaultLanguage = LANGUAGE_CHINA;
                }
                current = currentMap.get(defaultLanguage);
            }
            return current;
        }
        return null;
    }


    public StringConfig getStringConfig() {
        IConfig config = getCurrentConfig(RESOURCE_STRING);
        if (config != null) {
            return (StringConfig) config;
        }
        return null;
    }

    public ImageConfig getImageConfig() {
        IConfig config = getCurrentConfig(RESOURCE_IMAGE);
        if (config != null) {
            return (ImageConfig) config;
        }
        return null;
    }

    public ColorConfig getColorConfig() {
        IConfig config = getCurrentConfig(RESOURCE_COLOR);
        if (config != null) {
            return (ColorConfig) config;
        }
        return null;
    }


    public int getIdentifier(Context context, String name, String type) {
        return context.getResources().getIdentifier(name, type, context.getPackageName());
    }
}
