/**
 * Copyright (C) 2012-2013 Dell, Inc.
 * See annotations for authorship information
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.dasein.cloud.brightbox;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dasein.cloud.AbstractCloud;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.ContextRequirements;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.brightbox.api.AuthenticationService;
import org.dasein.cloud.brightbox.api.CloudApiService;
import org.dasein.cloud.brightbox.api.ErrorHandler;
import org.dasein.cloud.brightbox.api.model.Token;
import org.dasein.cloud.brightbox.compute.BrightBoxComputeServices;
import org.dasein.cloud.brightbox.dc.Zones;
import org.dasein.cloud.compute.ComputeServices;
import org.dasein.cloud.network.NetworkServices;
import org.dasein.cloud.platform.PlatformServices;
import org.dasein.cloud.util.Cache;
import org.dasein.cloud.util.CacheLevel;
import org.dasein.util.uom.time.Second;
import org.dasein.util.uom.time.TimePeriod;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Date;

/**
 * Add header info here
 * @version 2013.01 initial version
 * @since 2013.01
 */
public class BrightBoxCloud extends AbstractCloud {
    static private final Logger logger = getLogger(BrightBoxCloud.class);

    static private @Nonnull String getLastItem(@Nonnull String name) {
        int idx = name.lastIndexOf('.');

        if( idx < 0 ) {
            return name;
        }
        else if( idx == ( name.length() - 1 ) ) {
            return "";
        }
        return name.substring(idx + 1);
    }

    static public @Nonnull Logger getLogger(@Nonnull Class<?> cls) {
        String pkg = getLastItem(cls.getPackage().getName());

        if( pkg.equals("brightbox") ) {
            pkg = "";
        }
        else {
            pkg = pkg + ".";
        }
        return Logger.getLogger("dasein.cloud.brightbox.std." + pkg + getLastItem(cls.getName()));
    }

    static public @Nonnull RestAdapter.Log getWireLog(@Nonnull final Class<?> cls) {
        return new RestAdapter.Log() {
            @Override public void log(String message) {
                Logger.getLogger("dasein.cloud.brightbox.wire." + getLastItem(cls.getPackage().getName()) + "." + getLastItem(cls.getName())).log(Level.DEBUG, message);
            }
        };
    }

    public BrightBoxCloud() {
    }

    @Override
    public @Nonnull String getCloudName() {
        ProviderContext ctx = getContext();
        String name = ( ctx == null ? null : ctx.getCloud().getCloudName() );

        return ( name == null ? "BrightBox" : name );
    }

    @Override
    public @Nonnull ContextRequirements getContextRequirements() {
        // define the information needed to connect to this cloud in the form of context requirements
        // this brightbox defines a single keypair that any client must provide to the ProviderContext when connecting
        return new ContextRequirements(
                new ContextRequirements.Field("apiKey", "The API key used to connect to this cloud", ContextRequirements.FieldType.KEYPAIR, ContextRequirements.Field.ACCESS_KEYS, true)
        );
    }

    @Override
    public @Nonnull Zones getDataCenterServices() {
        return new Zones(this);
    }

    @Override
    public @Nullable ComputeServices getComputeServices() {
        return new BrightBoxComputeServices(this);
    }

    @Override
    public @Nullable NetworkServices getNetworkServices() {
        return super.getNetworkServices();
    }

    @Override
    public @Nullable PlatformServices getPlatformServices() {
        return super.getPlatformServices();
    }

    @Override
    public @Nonnull String getProviderName() {
        ProviderContext ctx = getContext();
        String name = (ctx == null ? null : ctx.getCloud().getProviderName());

        return (name == null ? "BrightBox" : name);
    }

    /**
     * Requests a new authentication token or returns a cached one
     * @return authentication token
     * @throws InternalException
     * @throws CloudException
     */
    public @Nonnull String authenticate() throws InternalException, CloudException {
        ProviderContext ctx = getContext();
        if( ctx == null ) {
            throw new InternalException("No context is present for the authenticate call");
        }
        Cache<String> cache = Cache.getInstance(this, "tokens", String.class, CacheLevel.REGION_ACCOUNT);
        Iterable<String> tokens = cache.get(ctx);
        if( tokens != null ) {
            String token = tokens.iterator().next();
            if( token != null ) {
                return token;
            }
        }
        // nothing in the cache, request a new token
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(getContext().getCloud().getEndpoint())
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(getWireLog(BrightBoxCloud.class))
                .setErrorHandler(new ErrorHandler())
                .build();
        AuthenticationService service = adapter.create(AuthenticationService.class);
        byte[][] keys = ( byte[][] ) getContext().getConfigurationValue("apiKey");
        String clientId = new String(keys[0]);
        String clientSecret = new String(keys[1]);
        String authorization = "Basic " + Base64.encodeBase64String((clientId + ":" + clientSecret).getBytes());
        Token token = service.getToken(authorization, clientId, "none");
        if( token != null ) {
            cache = Cache.getInstance(this, "tokens", String.class, CacheLevel.REGION_ACCOUNT, new TimePeriod<Second>(token.getExpiresIn(), TimePeriod.SECOND));
            cache.put(ctx, Arrays.asList(token.getAccessToken()));
            return token.getAccessToken();
        }
        throw new CloudException("Unable to authenticate");
    }

    /**
     * Return transport service instance
     * @return
     * @throws CloudException
     * @throws InternalException
     */
    public CloudApiService getCloudApiService() throws CloudException, InternalException {
        final String token = authenticate();
        RequestInterceptor interceptor = new RequestInterceptor() {
            @Override public void intercept(RequestFacade request) {
                request.addHeader("Authorization", "OAuth " + token);
            }
        };
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(getContext().getCloud().getEndpoint())
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(getWireLog(BrightBoxCloud.class))
                .setErrorHandler(new ErrorHandler())
                .setRequestInterceptor(interceptor)
                .build();
        return adapter.create(CloudApiService.class);
    }

    @Override
    public @Nullable String testContext() {
        if( logger.isTraceEnabled() ) {
            logger.trace("ENTER - " + BrightBoxCloud.class.getName() + ".testContext()");
        }
        try {
            ProviderContext ctx = getContext();

            if( ctx == null ) {
                logger.warn("No context was provided for testing");
                return null;
            }
            try {
                authenticate();
                byte[][] keys = ( byte[][] ) getContext().getConfigurationValue("apiKey");
                return new String(keys[0]);
            }
            catch( Throwable t ) {
                logger.error("Error querying API key: " + t.getMessage());
                t.printStackTrace();
                return null;
            }
        }
        finally {
            if( logger.isTraceEnabled() ) {
                logger.trace("EXIT - " + BrightBoxCloud.class.getName() + ".textContext()");
            }
        }
    }
}