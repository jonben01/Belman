package GUI;

import GUI.controllers.*;

public enum View {
    LOGIN("/views/login.fxml", LoginController.class),
    CAMERA("/views/camera.fxml", CameraController.class),
    ADMIN("/views/admin.fxml", AdminController.class),
    USER_CREATION_MODAL("/views/userCreationModal.fxml", UserCreationController.class),
    ORDER("/views/order.fxml", OrderController.class),
    PHOTO_DOC("/views/photoDoc.fxml", PhotoDocController.class);


    private final String fxml;
    private final Class<?> controllerClass;

    View(String fxml, Class<?> controllerClass) {
        this.fxml = fxml;
        this.controllerClass = controllerClass;
    }

    public String getFXML() {
        return fxml;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

}
