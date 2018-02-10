package ru.sberbank.awsapp;

import android.graphics.Bitmap;

/**
 * Model class for preview page data.
 */

public class PreviewData {
    String name;
    String description;
    Bitmap image;

    public PreviewData(String name, String description, Bitmap image) {
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
