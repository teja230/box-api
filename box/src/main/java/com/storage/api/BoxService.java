package com.storage.api;

import com.storage.api.utility.StorageAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.storage.api.storage.BoxConstants.BOX_SERVICE;

public class BoxService {
    private static final Logger logger = LoggerFactory.getLogger(BoxService.class);
    public static void main(String[] args) {
        try {
            StorageAPI.upload();
        } catch (IOException e) {
            logger.error(BOX_SERVICE, "API_1652", "Exception Uploading File", e);
        }
    }
}
