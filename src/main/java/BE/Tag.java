package BE;

public enum Tag {

    //due to scope of project, tag ids will be hardcoded. less than ideal for real-world application.

    DAMAGED(1),
    APPROVED(2),
    REJECTED(3),
    INFORMATION(4),
    COSMETIC(5);

    private final int id;

    Tag(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
