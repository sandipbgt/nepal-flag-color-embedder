package com.sandipbhagat.nepaliflag.service;

/**
 * Created by sandip on 8/16/15.
 */

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.*;
import java.io.FileInputStream;

public class ImageLoadService extends Service<Image> {

    private String imagePath;

    public ImageLoadService(String path) {
        this.imagePath = path;
    }

    @Override
    protected Task<Image> createTask() {

        return new Task<Image>() {

            @Override
            protected Image call() throws Exception {
                this.updateMessage("Loading image from: " + imagePath);
                Image resizedImage = new Image(new FileInputStream(imagePath), 800, 600, true, true);
                updateProgress(resizedImage.getProgress(), 100.0);

                return resizedImage;
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                updateMessage("The task was cancelled");
            }

            @Override
            protected void failed() {
                super.failed();
                updateMessage("The task failed");
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                updateProgress(100, 100);
                updateMessage("Successfully loaded image from: " + imagePath);
            }
        };
    }
}

