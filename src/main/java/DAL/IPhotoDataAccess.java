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
    void insertImagePathsToDB(Connection connection, List<Path> filePaths, User uploader, Product product) throws Exception;

    void saveImageAndPath(List<BufferedImage> photos,
                          List<String> fileNames,
                          User uploader,
                          Product product,
                          String orderNumber) throws Exception;


    Map<Integer, List<Photo>> getPhotosForProducts(List<Integer> productIds) throws Exception;

}
