/*
 * Copyright (C) 2009-2015 Dell, Inc.
 * See annotations for authorship information
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

package org.dasein.cloud.brightbox.api;

import com.google.gson.annotations.SerializedName;
import org.dasein.cloud.CloudErrorType;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.brightbox.UnauthorizedException;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by stas on 09/02/2015.
 */
public class ErrorHandler implements retrofit.ErrorHandler {
    class BrightBoxError {
        @SerializedName("error_name")
        String errorName;
        String[] errors;
    }
    @Override
    public Throwable handleError(RetrofitError cause) {
        Response r = cause.getResponse();
        if( r != null ) {
            switch( r.getStatus() ) {
                case 401:
                    return new UnauthorizedException(cause);
                case 400:
                case 404:
                case 405:
                case 422:
                    return new InternalException(r.getReason());
                case 403:
                case 409:
                    BrightBoxError error = ( BrightBoxError ) cause.getBodyAs(BrightBoxError.class);
                    if( error != null && error.errors != null && error.errors.length > 0 )
                        return new CloudException(CloudErrorType.GENERAL, r.getStatus(), r.getReason(), error.errors[0]);
                    else return new CloudException(r.getReason());
                case 423:
                    return new CloudException(r.getReason());
                case 500:
                case 501:
                    return new CloudException(r.getReason());
            }
        }
        return cause;
    }
}
