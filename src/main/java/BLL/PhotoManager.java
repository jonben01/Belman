package BLL;

import BE.Photo;
import BE.Product;
import BE.User;
import DAL.IPhotoDataAccess;
import DAL.PhotoDAO;

import java.util.List;

public class PhotoManager {

    private IPhotoDataAccess photoDataAccess;

    public PhotoManager() {
        photoDataAccess = new PhotoDAO();
    }

    public void saveImageAndPath(List<Photo> photosToSave,
                                 List<String> fileNames,
                                 User currentUser,
                                 Product product,
                                 String orderNumber) throws Exception {
        photoDataAccess.saveImageAndPath(photosToSave, fileNames, currentUser, product, orderNumber);

    }

    public void updateTag(Photo photo) throws Exception {
        photoDataAccess.updateTag(photo);
    }
}
