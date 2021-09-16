package com.demo.retrofit.entity;

import java.util.ArrayList;

public class S3UploadParam {
    public ArrayList<S3PhotoBucket> bucketList;

    public class S3PhotoBucket {
        public String bucket;
        public String endpoint;
        public String region;

        @Override
        public String toString() {
            return "S3PhotoBucket{" +
                    "bucket='" + bucket + '\'' +
                    ", endpoint='" + endpoint + '\'' +
                    ", region='" + region + '\'' +
                    '}';
        }
    }
}
