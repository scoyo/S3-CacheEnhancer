/*
 * Copyright 2013, Moritz Siuts
 *
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
 */
package com.scoyo.tools.s3cacheenhancer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class S3HeaderEnhancer {

    private final AmazonS3 s3;
    private final String bucketName;
    private final String prefix;
    private int maxAge = 94608000;
    private int maxThreads = 10;

    public S3HeaderEnhancer(AmazonS3 s3, String bucketName) {
        this(s3, bucketName, null);
    }

    public S3HeaderEnhancer(AmazonS3 s3, String bucketName, String prefix) {
        this.bucketName = bucketName;
        this.s3 = s3;
        this.prefix = prefix;
    }

    public void createCacheHeaders() throws AmazonClientException {

        ExecutorService executorService =  new ThreadPoolExecutor(
                maxThreads, // core thread pool size
                maxThreads, // maximum thread pool size
                1, // time to wait before resizing pool
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(maxThreads, true),
                new ThreadPoolExecutor.CallerRunsPolicy());

        String maxAgeHeader = "public, max-age=" + maxAge;
        ObjectListing listing = null;
        if (prefix == null) {
            listing = s3.listObjects(bucketName);
        } else {
            listing = s3.listObjects(bucketName, prefix);
        }

        setHeaders(listing, maxAgeHeader, executorService);
        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            setHeaders(listing, maxAgeHeader, executorService);
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setHeaders(ObjectListing listing, final String maxAgeHeader, ExecutorService executorService) {

        for (final S3ObjectSummary summary : listing.getObjectSummaries()) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    String bucket = summary.getBucketName();
                    String key = summary.getKey();

                    ObjectMetadata metadata = null;
                    try {
                        metadata = s3.getObjectMetadata(bucket, key);
                    } catch (AmazonS3Exception exception) {
                        System.out.println("Could not update " + key + " [" + exception.getMessage() + "]");
                        return;
                    }

                    if ("application/x-directory".equals(metadata.getContentType())) {
                        System.out.println("Skipping because content-type " + key);
                        return;
                    }

                    if (!maxAgeHeader.equals(metadata.getCacheControl())) {
                        metadata.setCacheControl(maxAgeHeader);
                    } else {
                        System.out.println("Skipping because header is already correct " + key);
                        return;
                    }

                    AccessControlList acl = s3.getObjectAcl(summary.getBucketName(), summary.getKey());

                    CopyObjectRequest copyReq = new CopyObjectRequest(bucket, key, bucket, key)
                            .withAccessControlList(acl)
                            .withNewObjectMetadata(metadata);

                    CopyObjectResult result = s3.copyObject(copyReq);

                    if (result != null) {
                        System.out.println("Updated " + key);
                    } else {
                        System.out.println("Could not update " + key);
                    }
                }
            });
        }
    }

    /**
     * The the maxAge value for the Cache-Control-Header.
     * Defaults to {@code 94608000}.
     * @param maxAge a positive int, see above
     */
    public void setMaxAge(int maxAge) {
        if (maxAge < 0) {
            throw new IllegalArgumentException("maxAge must be positive.");
        }
        this.maxAge = maxAge;
    }

    /**
     * The maxmimum size of the the ThreadPool to use.
     * Defaults to 10.
     * @param maxThreads see above, must be at least 1
     */
    public void setMaxThreads(int maxThreads) {
        if (maxThreads < 1) {
            throw new IllegalArgumentException("maxThreads must be >= 1.");
        }
        this.maxThreads = maxThreads;
    }


}
