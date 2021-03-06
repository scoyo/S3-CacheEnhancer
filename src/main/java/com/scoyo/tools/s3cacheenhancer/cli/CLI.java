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
package com.scoyo.tools.s3cacheenhancer.cli;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.scoyo.tools.s3cacheenhancer.S3HeaderEnhancer;

/**
 * Commandline interface for {@code S3HeaderEnhancer}.
 */
public class CLI {

    public static void main(String[] args) {
        // parse the commandline arguments
        HeaderEnhancerArguments arguments = new HeaderEnhancerArguments();
        JCommander jCommander = new JCommander(arguments);
        jCommander.setProgramName("java -jar S3-CacheEnhancer-1.0.0-SNAPSHOT-jar-with-dependencies.jar");
        try {
            jCommander.parse(args);
        } catch (ParameterException ex) {
            jCommander.usage();
            System.exit(1);
        }

        S3HeaderEnhancer enhancer = buildS3HeaderEnhancer(arguments);
        enhancer.createCacheHeaders();
    }


    private static S3HeaderEnhancer buildS3HeaderEnhancer(HeaderEnhancerArguments arguments) {
        AWSCredentials credentials = new BasicAWSCredentials(arguments.awsAccessKey, arguments.awsSecretKey);
        AmazonS3 s3 = new AmazonS3Client(credentials);
        S3HeaderEnhancer enhancer = new S3HeaderEnhancer(s3, arguments.bucketName, arguments.prefix);

        if (arguments.maxAge != null) {
            enhancer.setMaxAge(arguments.maxAge);
        }
        if (arguments.maxThreads != null) {
            enhancer.setMaxThreads(arguments.maxThreads.intValue());
        }
        return enhancer;
    }


}
