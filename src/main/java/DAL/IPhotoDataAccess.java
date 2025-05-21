package DAL;

import BE.Photo;
import BE.Product;
import BE.User;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface IPhotoDataAccess {
    void insertImagePathsToDB(Connection connection, List<Path> filePaths, User uploader, Product product,
                              List<Photo> photos) throws Exception;

    //TODO refactor this, after switching to photo objects, just use them as DTO's, only needs order + list of photos then
    void saveImageAndPath(List<Photo> photosToSave,
                          List<String> fileNames,
                          User uploader,
                          Product product,
                          String orderNumber) throws Exception;


    Map<Integer, List<Photo>> getPhotosForProducts(List<Integer> productIds) throws Exception;

    void updateTag(Photo photo) throws Exception;
}
