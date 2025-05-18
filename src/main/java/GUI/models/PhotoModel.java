package GUI.models;

import BE.Photo;
import BE.Product;
import BE.User;
import BLL.PhotoManager;

import java.awt.image.BufferedImage;
import java.util.List;

public class PhotoModel {

    private PhotoManager photoManager;

    public PhotoModel() throws Exception {
        photoManager = new PhotoManager();
    }

    public void saveImageAndPath(List<Photo> photosToSave,
                                 List<String> fileNames,
                                 User currentUser,
                                 Product product,
                                 String orderNumber) throws Exception {
        photoManager.saveImageAndPath(photosToSave, fileNames, currentUser, product, orderNumber);
    }
}
