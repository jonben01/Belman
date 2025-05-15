package BE;

import java.sql.Timestamp;

public class Photo {
    private int id;
    private String filePath;
    private int productId;
    private String capturedBy;
    private Timestamp takenAt;
    private Tag tag;

    public Photo(int id, String filePath, int productId, String capturedBy, Timestamp takenAt, Tag tag) {
        this.id = id;
        this.filePath = filePath;
        this.productId = productId;
        this.capturedBy = capturedBy;
        this.takenAt = takenAt;
        this.tag = tag;
    }

    public Photo() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getCapturedBy() {
        return capturedBy;
    }

    public void setCapturedBy(String capturedBy) {
        this.capturedBy = capturedBy;
    }

    public Timestamp getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(Timestamp takenAt) {
        this.takenAt = takenAt;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
