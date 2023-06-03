module imprimer {
	requires javafx.controls;
	requires java.sql;
	
	opens application to javafx.graphics, javafx.fxml;
	
    requires javafx.fxml;
	requires javafx.graphics;

    
    exports application;
	
}
