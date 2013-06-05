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

import com.beust.jcommander.Parameter;

/**
 * Holds the possible commandline arguments.
 */
public class HeaderEnhancerArguments {

    @Parameter(names = {"-b", "--bucket"}, description = "Name of S3 bucket to use", required = true)
    public String bucketName;

    @Parameter(names = {"-k", "--access-key"}, description = "AWS Access Key", required = true)
    public String awsAccessKey;

    @Parameter(names = {"-s", "--secret-key"}, description = "AWS Secret Key", required = true)
    public String awsSecretKey;

    @Parameter(names = {"-ma", "--maxAge"}, description = "value for max-age")
    public Integer maxAge;

    @Parameter(names = {"-p", "--prefix"}, description = "prefix for S3 objects in bucket")
    public String prefix = null;

    @Parameter(names = {"-t", "--maxThreads"}, description = "max number of Threads to use")
    public Integer maxThreads;
}
