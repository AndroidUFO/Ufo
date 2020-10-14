package com.androidufo.ufo.api.provider;

import android.content.Context;
import android.content.res.AssetManager;
import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.ufo.Ufo;
import com.androidufo.ufo.api.model.HttpsInfos;
import com.androidufo.ufo.core.HttpProtocol;
import com.androidufo.ufo.okhttp.OkHttpProtocol;
import com.androidufo.ufo.api.utils.GenerateRules;
import com.androidufo.ufo.okhttp.configs.HttpConfigs;
import com.androidufo.ufo.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Provider {

    private final Map<String, Object> apis;
    private final Map<String, HttpProtocol> protocols;
    private final Map<String, Class<? extends HttpConfigs>> configs;
    private final Map<String, HttpsInfos> httpsInfosMap;

    private Provider() {
        apis = new HashMap<>();
        protocols = new HashMap<>();
        configs = new HashMap<>();
        httpsInfosMap = new HashMap<>();
    }

    public static Provider get() {
        return Holder.INSTANCE;
    }

    public static  <T> T api(Class<T> clazz) {
        return (T) Provider.get().apiInstance(clazz);
    }

    public Object apiInstance(Class<?> apiClazz) {
        String canonicalName = apiClazz.getCanonicalName();
        if (apis.containsKey(canonicalName)) {
            Object instance = apis.get(canonicalName);
            if (instance != null) {
                return instance;
            }
        }
        return createApiInstance(canonicalName, apiClazz.getSimpleName());
    }

    public HttpProtocol apiHttpProtocol(Object apiInstance) {
        if (apiInstance == null) {
            return null;
        }
        String address = apiInstance.toString();
        if (protocols.containsKey(address)) {
            return protocols.get(address);
        }
        return null;
    }

    private Object createApiInstance(String canonicalName, String simpleName) {
        try {
            // 创建api实例
            Class<?> generateApiClazz = GenerateRules.getGenerateApiClazz(simpleName);
            Object instance = generateApiClazz.newInstance();
            apis.put(canonicalName, instance);
            // 创建protocol实例
            createProtocol(canonicalName, instance.toString());
            return instance;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createProtocol(String canonicalName, String apiAddress) {
        HttpConfigs httpConfigs = createHttpConfigsInstance(canonicalName);
        OkHttpProtocol protocol;
        HttpsInfos httpsInfos = getHttpsInfos(canonicalName);
        if (httpsInfos != null) {
            try {
                AssetManager assetManager = Ufo.getInstance().getContext().getAssets();
                InputStream certificate = openStream(assetManager, httpsInfos.getAssetsSslCer());
                InputStream bksIs = null;
                if (!EmptyUtils.stringNull(httpsInfos.getAssetsBks())) {
                    bksIs = openStream(assetManager, httpsInfos.getAssetsBks());
                }
                protocol = new OkHttpProtocol(certificate, bksIs, httpsInfos.getBksPassword());
            } catch (IOException e) {
                e.printStackTrace();
                Logger.error(e.getLocalizedMessage());
                protocol = new OkHttpProtocol();
            }
        } else {
            protocol = new OkHttpProtocol();
        }
        protocol.setHttpConfigs(httpConfigs);
        protocols.put(apiAddress, protocol);
    }

    private InputStream openStream(AssetManager assetManager, String file) throws IOException {
        try {
            return assetManager.open(file);
        } catch (IOException e) {
            throw new IOException("AssetManager open https file " + file + " failed");
        }
    }

    private HttpsInfos getHttpsInfos(String canonicalName) {
        if (httpsInfosMap.containsKey(canonicalName)) {
            HttpsInfos httpsInfos = httpsInfosMap.get(canonicalName);
            if (httpsInfos != null) {
                if (!EmptyUtils.stringNull(httpsInfos.getAssetsSslCer())) {
                    return httpsInfos;
                }
            }
        }
        return null;
    }

    private HttpConfigs createHttpConfigsInstance(String canonicalName) {
        if (configs.containsKey(canonicalName)) {
            Class<? extends HttpConfigs> httpConfigsClazz = configs.get(canonicalName);
            if (httpConfigsClazz != null) {
                try {
                    Constructor<? extends HttpConfigs> constructor = httpConfigsClazz.getConstructor(String.class);
                    return constructor.newInstance(canonicalName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void bindHttpConfigs(String canonicalName, Class<? extends HttpConfigs> configClazz) {
        if (!configs.containsKey(canonicalName)) {
            configs.put(canonicalName, configClazz);
        }
    }

    public void bindHttpsInfos(String canonicalName, String assetsSslCer, String assetsBks, String bksPassword) {
        if (EmptyUtils.stringNull(assetsSslCer)) {
            return;
        }
        if (!httpsInfosMap.containsKey(canonicalName)) {
            httpsInfosMap.put(canonicalName, new HttpsInfos(assetsSslCer, assetsBks, bksPassword));
        }
    }

    private static class Holder {
        private static final Provider INSTANCE = new Provider();
    }

}
