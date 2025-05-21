package GUI.util;

//essentially a BE, but only ever used in PhotoDoc, so I decided to put it here.

import BE.Photo;
import BE.Tag;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;

public class PhotoCard extends VBox {
    private Photo photo;
    private ImageView imageView;
    private static final int IMAGE_WIDTH = 480;
    private static final int IMAGE_HEIGHT = 270;

    public PhotoCard(Photo photo) {
        this.photo = photo;

        imageView = new ImageView(new Image(new File(photo.getFilePath()).toURI().toString(),
                IMAGE_WIDTH, IMAGE_HEIGHT, true, true));
        imageView.setFitWidth(IMAGE_WIDTH);
        imageView.setFitHeight(IMAGE_HEIGHT);
        imageView.getStyleClass().add("photo");
        this.getStyleClass().add("image-card");
        updateStyleClass();
        this.getChildren().add(imageView);
    }

    public Photo getPhoto() {
        return photo;
    }
    public ImageView getImageView() {
        return imageView;
    }

    public void updateStyleClass() {
        this.getStyleClass().removeIf(style -> style.startsWith("tag-"));

        String tagStyle;
        if (photo.getTag() == null) {
            tagStyle = "tag-null";
        } else if (photo.getTag() == Tag.APPROVED) {
            tagStyle = "tag-approved";
        } else if (photo.getTag() == Tag.REJECTED) {
            tagStyle = "tag-rejected";
        } else {
            tagStyle = "tag-info";
        }
        this.getStyleClass().add(tagStyle);
        //reapply css to update style class
        this.applyCss();
        this.layout();
    }
}
