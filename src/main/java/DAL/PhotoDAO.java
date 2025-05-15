package DAL;

import BE.*;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PhotoDAO implements IPhotoDataAccess {

    @Override
    public void insertImagePathsToDB(Connection connection, List<Path> filePaths, User uploader, Product product) throws Exception {

    }

    @Override
    public void saveImageAndPath(List<BufferedImage> photos, List<String> fileNames, User uploader, String productNumber) throws Exception {

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
