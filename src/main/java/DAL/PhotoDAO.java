package DAL;

import BE.*;
import javafx.collections.ObservableList;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PhotoDAO implements IPhotoDataAccess {

    //added milliseconds to essentially guarantee the uniqueness of filenames.
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSSS");

    private final Path baseRelativePath = Paths.get("QC_Images");



    @Override
    public void saveImageAndPath(List<BufferedImage> photos,
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

            insertImagePathsToDB(connection, persistedPaths, uploader, product);

            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    //TODO exception handling write a beautiful message please
                    throw new Exception("Error while rolling back transaction", ex);
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
                    e.printStackTrace();
                    //TODO figure out how to deal with this best
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

    private List<Path> saveImages(List<BufferedImage> photos,
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
                String fileName = fileNames.get(i) + "_" + now.format(DATE_TIME_FORMATTER) + ".png";
                Path tempFilePath = tempDir.resolve(fileName);
                ImageIO.write(photos.get(i), "png", tempFilePath.toFile());
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
            Files.deleteIfExists(tempDir);
            return movedFilePaths;

        } catch (IOException e) {
            deleteFiles(movedFilePaths);
            deleteRecursively(tempDir);
            throw e;
        }


    }

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
    public void insertImagePathsToDB(Connection connection, List<Path> filePaths, User uploader, Product product) throws Exception {

        String sql = "INSERT INTO Photos (product_id, file_path, uploaded_by, uploaded_at) VALUES (?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Path path : filePaths) {
                statement.setInt(1, product.getId());
                statement.setString(2, path.toString());
                statement.setInt(3, uploader.getId());
                statement.setObject(4, now);
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
                    //TODO get more confident in explaining this
                    photoMap.computeIfAbsent(productId, _ -> new ArrayList<>()).add(photo);
                }
            }
        }
        return photoMap;
    }
}
