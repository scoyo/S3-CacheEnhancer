S3-CacheEnhancer
================

This is a little command line tool written in Java to set the Cache-Control-Header on Amazon S3 objects.
It uses the AWS-SDK as dependency.


Building
--------

To build the S3-CacheEnhancer you need Maven.

Just run 

    mvn clean package

to build a jar with all the neccessay depencies included.

Then you could run it with

    java -jar ./target/S3-CacheEnhancer-1.0.0-SNAPSHOT-jar-with-dependencies.jar

It will print a help output, because you need to provide some parameters like the AWS credentials and the bucketname.

