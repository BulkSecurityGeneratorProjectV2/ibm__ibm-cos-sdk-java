/*
 * Copyright 2015-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.ibm.cloud.objectstorage.services.s3.model;

import java.io.Serializable;

/**
 * Result object for Head Bucket request.
 */
public class HeadBucketResult implements Serializable {

    private String bucketRegion;
    private boolean IBMSSEKPEnabled;
    private String IBMSSEKPCrk;

    // IBM-Specifc
    /**
     * Returns if KP has been enabled
     */
    public boolean getIBMSSEKPEnabled() {
        return IBMSSEKPEnabled;
    }

    // IBM-Specifc
    /**
     * Sets if KP is enabled
     * @param iBMSSEKPEnabled
     */
    public void setIBMSSEKPEnabled(boolean iBMSSEKPEnabled) {
        this.IBMSSEKPEnabled = iBMSSEKPEnabled;
    }

    // IBM-Specifc
    /**
     * Returns the CRK header of the HEAD request
     */
    public String getIBMSSEKPCrk() {
        return IBMSSEKPCrk;
    }

    // IBM-Specifc
    /**
     * Sets the CRK value
     * @param iBMSSEKPCrkName
     */
    public void setIBMSSEKPCrk(String iBMSSEKPCrk) {
        this.IBMSSEKPCrk = iBMSSEKPCrk;
    }

    /**
     * Returns the AWS region where the bucket is located.
     */
    public String getBucketRegion() {
        return bucketRegion;
    }

    public void setBucketRegion(String bucketRegion) {
        this.bucketRegion = bucketRegion;
    }

    public HeadBucketResult withBucketRegion(String bucketRegion) {
        setBucketRegion(bucketRegion);
        return this;
    }
}
