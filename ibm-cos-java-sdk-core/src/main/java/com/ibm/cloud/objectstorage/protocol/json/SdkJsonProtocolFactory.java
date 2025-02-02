/*
 *
 * Copyright (c) 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.ibm.cloud.objectstorage.protocol.json;

import com.ibm.cloud.objectstorage.AmazonServiceException;
import com.ibm.cloud.objectstorage.AmazonWebServiceResponse;
import com.ibm.cloud.objectstorage.SDKGlobalConfiguration;
import com.ibm.cloud.objectstorage.annotation.SdkProtectedApi;
import com.ibm.cloud.objectstorage.annotation.ThreadSafe;
import com.ibm.cloud.objectstorage.http.HttpResponseHandler;
import com.ibm.cloud.objectstorage.protocol.OperationInfo;
import com.ibm.cloud.objectstorage.protocol.Protocol;
import com.ibm.cloud.objectstorage.protocol.ProtocolRequestMarshaller;
import com.ibm.cloud.objectstorage.protocol.json.internal.EmptyBodyJsonMarshaller;
import com.ibm.cloud.objectstorage.transform.JsonErrorUnmarshaller;
import com.ibm.cloud.objectstorage.transform.JsonUnmarshallerContext;
import com.ibm.cloud.objectstorage.transform.Unmarshaller;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory to generate the various JSON protocol handlers and generators depending on the wire protocol to be used for
 * communicating with the AWS service.
 */
@ThreadSafe
@SdkProtectedApi
public class SdkJsonProtocolFactory implements SdkJsonMarshallerFactory {

    private final JsonClientMetadata metadata;

    private final List<JsonErrorUnmarshaller> errorUnmarshallers = new ArrayList<JsonErrorUnmarshaller>();

    public SdkJsonProtocolFactory(JsonClientMetadata metadata) {
        this.metadata = metadata;
        createErrorUnmarshallers();
    }

    @Override
    public StructuredJsonGenerator createGenerator() {
        return getSdkFactory().createWriter(getContentType());
    }

    @Override
    public String getContentType() {
        return getContentTypeResolver().resolveContentType(metadata);
    }

    public <T> ProtocolRequestMarshaller<T> createProtocolMarshaller(OperationInfo operationInfo, T origRequest) {
        return JsonProtocolMarshallerBuilder.<T>standard()
                .jsonGenerator(createGenerator(operationInfo))
                .contentType(getContentType())
                .operationInfo(operationInfo)
                .originalRequest(origRequest)
                .emptyBodyMarshaller(createEmptyBodyMarshaller(operationInfo))
                .build();
    }

    private StructuredJsonGenerator createGenerator(OperationInfo operationInfo) {
        if (operationInfo.hasPayloadMembers() || operationInfo.protocol() == Protocol.AWS_JSON) {
            return createGenerator();
        } else {
            return StructuredJsonGenerator.NO_OP;
        }
    }

    /**
     * For requests with payloads, if it has an explicit payload member and that member is null,
     * the body should be rendered as empty JSON.
     *
     * The API Gateway protocol has it's own factory and should not appear here.
     */
    private EmptyBodyJsonMarshaller createEmptyBodyMarshaller(OperationInfo operationInfo) {
        if (operationInfo.protocol() == Protocol.API_GATEWAY) {
            throw new IllegalStateException("Detected the API_GATEWAY protocol which should not be used with this "
                                            + "protocol factory.");
        }
        if (!operationInfo.hasPayloadMembers() || operationInfo.protocol() == Protocol.API_GATEWAY) {
            return EmptyBodyJsonMarshaller.NULL;
        } else {
            return EmptyBodyJsonMarshaller.EMPTY;
        }
    }

    /**
     * Returns the response handler to be used for handling a successful response.
     *
     * @param operationMetadata Additional context information about an operation to create the appropriate response handler.
     */
    public <T> HttpResponseHandler<AmazonWebServiceResponse<T>> createResponseHandler(
            JsonOperationMetadata operationMetadata,
            Unmarshaller<T, JsonUnmarshallerContext> responseUnmarshaller) {
        return getSdkFactory().createResponseHandler(operationMetadata, responseUnmarshaller);
    }

    /**
     * Creates a response handler for handling a error response (non 2xx response).
     */
    public HttpResponseHandler<AmazonServiceException> createErrorResponseHandler(
            JsonErrorResponseMetadata errorResponsMetadata) {
        return getSdkFactory().createErrorResponseHandler(errorUnmarshallers, errorResponsMetadata
                .getCustomErrorCodeFieldName());
    }

    @SuppressWarnings("unchecked")
    private void createErrorUnmarshallers() {
        for (JsonErrorShapeMetadata errorMetadata : metadata.getErrorShapeMetadata()) {
            if (errorMetadata.getExceptionUnmarshaller() != null) {
                errorUnmarshallers.add(errorMetadata.getExceptionUnmarshaller());
            } else if (errorMetadata.getModeledClass() != null) {
                errorUnmarshallers.add(new JsonErrorUnmarshaller(
                        (Class<? extends AmazonServiceException>) errorMetadata.getModeledClass(),
                        errorMetadata.getErrorCode()));
            }
        }

        if (metadata.getBaseServiceExceptionClass() != null) {
            errorUnmarshallers.add(new JsonErrorUnmarshaller(
                    (Class<? extends AmazonServiceException>) metadata.getBaseServiceExceptionClass(), null));
        }
    }

    /**
     * @return Instance of {@link SdkStructuredJsonFactory} to use in creating handlers.
     */
    private SdkStructuredJsonFactory getSdkFactory() {
        if (isCborEnabled()) {
            return SdkStructuredCborFactory.SDK_CBOR_FACTORY;
        } else if (isIonEnabled()) {
            return isIonBinaryEnabled()
                    ? SdkStructuredIonFactory.SDK_ION_BINARY_FACTORY
                    : SdkStructuredIonFactory.SDK_ION_TEXT_FACTORY;
        } else {
            return SdkStructuredPlainJsonFactory.SDK_JSON_FACTORY;
        }
    }

    /**
     * @return Content type resolver implementation to use.
     */
    private JsonContentTypeResolver getContentTypeResolver() {
        if (isCborEnabled()) {
            return JsonContentTypeResolver.CBOR;
        } else if (isIonEnabled()) {
            return isIonBinaryEnabled()
                    ? JsonContentTypeResolver.ION_BINARY
                    : JsonContentTypeResolver.ION_TEXT;
        } else {
            return JsonContentTypeResolver.JSON;
        }
    }

    private boolean isCborEnabled() {
        return metadata.isSupportsCbor() && !SDKGlobalConfiguration.isCborDisabled();
    }

    private boolean isIonEnabled() {
        return metadata.isSupportsIon();
    }

    boolean isIonBinaryEnabled() {
        return !SDKGlobalConfiguration.isIonBinaryDisabled();
    }
}
