module dk.easv.belman {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.belman to javafx.fxml;
    exports dk.easv.belman;
}