package DAL;

import BE.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


//TODO split this class up, gigantic breach of single responsibility.

public class PhotoDAO implements IPhotoDataAccess {

    //added milliseconds to essentially guarantee the uniqueness of filenames.
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSSS");

    private final Path baseRelativePath = Paths.get("QC_Images");



    //TODO refactor this, after switching to photo objects, just use them as DTO's, only needs order + list of photos then
    @Override
    public void saveImageAndPath(List<Photo> photos,
                                 List<String> fileNames,
                                 User uploader,
                                 Product product,
                                 String orderNumber) throws Exception {

        if (photos.size() != fileNames.size()) {
            throw new IllegalArgumentException("Number of photos and file names must be equal");
        }
        Connection connection = null;
        List<Path> persistedPaths = new ArrayList<>();

        try {
            connection = DBConnector.getInstance().getConnection();
            connection.setAutoCommit(false);

            Path orderFolderPath = baseRelativePath.resolve(orderNumber + "_Images");
            Path productFolderPath = orderFolderPath.resolve(product.getProductNumber() + "_Images");

            persistedPaths = saveImages(photos, fileNames, productFolderPath);

            insertImagePathsToDB(connection, persistedPaths, uploader, product, photos);

            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new Exception("Error while rolling back image transaction", ex);
                }
                deleteFiles(persistedPaths);
                throw new Exception("Error while saving images", e);
            }

        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    throw new Exception("Error while closing connection while saving images", e);
                }
            }
        }
    }

    private void deleteFiles(List<Path> filePaths) {
        for (Path path : filePaths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignoredException) {
                //Deletion failure is what it is
                //ideally its logged or flagged so it can be manually deleted later.
            }
        }
    }

    private List<Path> saveImages(List<Photo> photos,
                                  List<String> fileNames,
                                  Path productFolderPath) throws IOException {
        //This recursively tries to create the directories if they don't exist.
        //So it will create both the order folder and the product folder if they don't exist.'
        Files.createDirectories(productFolderPath);

        Path tempDir = Files.createTempDirectory(productFolderPath, "temp_images");
        List<Path> tempFilePaths = new ArrayList<>(photos.size());
        List<Path> movedFilePaths = new ArrayList<>(photos.size());

        try {
            LocalDateTime now = LocalDateTime.now();

            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                BufferedImage bufferedImage = photo.getImage();
                String fileName = fileNames.get(i) + "_" + now.format(DATE_TIME_FORMATTER) + ".png";
                Path tempFilePath = tempDir.resolve(fileName);
                ImageIO.write(bufferedImage, "png", tempFilePath.toFile());
                tempFilePaths.add(tempFilePath);
            }
            for (Path tempFilePath : tempFilePaths) {
                Path movedFilePath = productFolderPath.resolve(tempFilePath.getFileName());
                if (Files.exists(movedFilePath)) {
                    throw new IOException("File already exists: " + movedFilePath);
                }
                //move the temp file to the final destination.
                Files.move(tempFilePath, movedFilePath);
                movedFilePaths.add(movedFilePath);
            }
            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                Path path = movedFilePaths.get(i);
                photo.setFilePath(path.toString());
            }
            Files.deleteIfExists(tempDir);
            return movedFilePaths;

        } catch (IOException e) {
            deleteFiles(movedFilePaths);
            deleteRecursively(tempDir);
            throw e;
        }
    }

    /**
     * Deletes the specified directory and its contents recursively. -- recursive due to walk()
     * If the directory does not exist or is null, the method will exit silently.
     *
     * @param tempDir the path to the directory to be deleted recursively
     *                including all files and subdirectories within it.
     */
    private void deleteRecursively(Path tempDir) {
        if (tempDir == null || Files.notExists(tempDir)) {
            return;
        }
        //auto close stream after use.
        try (Stream<Path> walk = Files.walk(tempDir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignoredException) {
                    //Deletion failure is what it is
                    //ideally its logged or flagged so it can be manually deleted later.
                }
            });
        } catch (IOException ignoredException) {
            //same as previous catch
        }
    }

    @Override
    public void insertImagePathsToDB(Connection connection, List<Path> filePaths, User uploader, Product product,
                                     List<Photo> photos) throws Exception {

        String sql = "INSERT INTO Photos (product_id, file_path, uploaded_by, uploaded_at, tag_id) VALUES (?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                Path path = filePaths.get(i);

                statement.setInt(1, product.getId());
                statement.setString(2, path.toString());
                statement.setInt(3, uploader.getId());
                statement.setObject(4, now);

                if (photo.getTag() != null) {
                    statement.setInt(5, photo.getTag().getId());
                } else {
                    statement.setNull(5, java.sql.Types.INTEGER);
                }
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }


    //should arguably just call getPhotosForOrder for each product, but either is fine, I think.
    @Override
    public Map<Integer, List<Photo>> getPhotosForProducts(List<Integer> productIds) throws Exception {
        Map<Integer, List<Photo>> photoMap = new HashMap<>();

        //exit early if no productIds are given
        if (productIds == null || productIds.isEmpty()) {
            return photoMap;
        }

        //Creating a comma-separated list of "?" placeholders for the SQL below, i.e 4 products = "?, ?, ?, ?"
        String placeholders = productIds.stream().map(id -> "?").collect(Collectors.joining(", "));

        String sql = """
            SELECT p.*, u.first_name, u.last_name, t.name AS tag_name
            FROM Photos p
            LEFT JOIN Users u ON p.uploaded_by = u.id
            LEFT JOIN Tags t ON p.tag_id = t.id
            WHERE p.product_id IN (%s)
        """.formatted(placeholders);
        //TODO remember the string formatter a bit better, for exam

        try (Connection conn = DBConnector.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < productIds.size(); i++) {
                stmt.setInt(i + 1, productIds.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id");

                    Photo photo = new Photo();
                    photo.setId(rs.getInt("id"));
                    photo.setFilePath(rs.getString("file_path"));
                    photo.setTakenAt(rs.getTimestamp("uploaded_at"));

                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    photo.setCapturedBy(firstName + " " + lastName);

                    String tagName = rs.getString("tag_name");
                    if (tagName != null) {
                        try {
                            photo.setTag(Tag.valueOf(tagName.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            photo.setTag(null);
                        }
                    }
                    photoMap.computeIfAbsent(productId, _ -> new ArrayList<>()).add(photo);
                }
            }
        }
        return photoMap;
    }

    @Override
    public void updateTag(Photo photo) throws Exception {
        String sql;

        if (photo.getTag() == Tag.APPROVED) {
            sql = "UPDATE Photos SET tag_id = 2 WHERE id = ?";
        } else if (photo.getTag() == Tag.REJECTED) {
            sql = "UPDATE Photos SET tag_id = 3 WHERE id = ?";
        } else {
            sql = "UPDATE Photos SET tag_id = 5 WHERE id = ?";
        }

        try (Connection connection = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, photo.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("An error occurred while updating tag for photo with id: " + photo.getProductId(), e);
        }

    }
}
