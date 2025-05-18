package BLL;

import BE.Product;
import BE.User;
import DAL.IPhotoDataAccess;
import DAL.PhotoDAO;

import java.awt.image.BufferedImage;
import java.util.List;

public class PhotoManager {

    private IPhotoDataAccess photoDataAccess;

    public PhotoManager() throws Exception{
        photoDataAccess = new PhotoDAO();
    }

    public void saveImageAndPath(List<BufferedImage> imagesToSave,
                                 List<String> fileNames,
                                 User currentUser,
                                 Product product,
                                 String orderNumber) throws Exception {
        photoDataAccess.saveImageAndPath(imagesToSave, fileNames, currentUser, product, orderNumber);

    }
}
