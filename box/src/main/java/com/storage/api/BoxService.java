package com.storage.api;

import com.storage.api.utility.StorageAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BoxService {
    private static final Logger logger = LoggerFactory.getLogger(BoxService.class);
    public static void main(String[] args) {
        try {
            StorageAPI.upload();
        } catch (IOException e) {
            logger.error("Exception Uploading File", e);
        }
    }
}
