package application;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.print.PrinterJob;
import java.sql.*;
import java.util.Optional;

public class Main extends Application {

    private Stage window;
    private TableView<Student> table;
    private TextField nomInput;
    private TextField prenomInput;
    private TextField emailInput;
    private TextField adresseInput;
    private TextField telephoneInput;

    private Connection conn;
    private PreparedStatement stmt;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/miniprojet";
    private static final String USER = "root";
    private static final String PASS = "";
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

   

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Gestion des inscriptions");

        // Columns
        TableColumn<Student, String> nomColumn = new TableColumn<>("Nom");
        nomColumn.setMinWidth(200);
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Student, String> prenomColumn = new TableColumn<>("Prénom");
        prenomColumn.setMinWidth(200);
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<Student, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setMinWidth(200);
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Student, String> adresseColumn = new TableColumn<>("Adresse");
        adresseColumn.setMinWidth(200);
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        TableColumn<Student, String> telephoneColumn = new TableColumn<>("Téléphone");
        telephoneColumn.setMinWidth(200);
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        table = new TableView<>();
        table.setItems(getEtudiant());
        table.getColumns().addAll(nomColumn, prenomColumn, emailColumn, adresseColumn, telephoneColumn);
        // Appliquer la classe CSS personnalisée à la TableView
        table.getStyleClass().add("table-view-custom");
        //labeltitre
         Label labeltitre= new Label();
         labeltitre.setText("Insription des étudiants");
     
         labeltitre.setTextFill(Color.BLUE);
         labeltitre.setFont(new Font(30));
         labeltitre.getStyleClass().add("label-titre");
         
         
        // Buttons
        Button addButton = new Button("Ajouter");
        addButton.setOnAction(e -> ajouterEtudiant());

        Button updateButton = new Button("Modifier");
        updateButton.setOnAction(e -> modifierEtudiant());

        Button deleteButton = new Button("Supprimer");
        deleteButton.setOnAction(e -> supprimerEtudiant());

       
        Button viewProfileButton = new Button("Voir profil");
        viewProfileButton.setOnAction(e -> afficherProfilEtudiant());

        HBox buttonsBox = new HBox(30);
        buttonsBox.setPadding(new Insets(20));
        buttonsBox.getChildren().addAll(addButton, updateButton, deleteButton,viewProfileButton);
        addButton.getStyleClass().add("button-custom");
        updateButton.getStyleClass().add("button-custom");
        deleteButton.getStyleClass().add("button-custom");
        viewProfileButton.getStyleClass().add("button-custom");

        BorderPane root = new BorderPane();
        BorderPane.setAlignment(labeltitre,Pos.CENTER);
        //labeltitre.setPadding(new Insets());
        root.setTop(labeltitre);
        root.setCenter(table);
        
        root.setBottom(buttonsBox);
   
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        window.setScene(scene);
        window.show();
    }

    

    private ObservableList<Student> getEtudiant() {
        ObservableList<Student> etudiants = FXCollections.observableArrayList();

        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            String sql = "SELECT * FROM etudiant";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String email = rs.getString("email");
                String adresse = rs.getString("adresse");
                String telephone = rs.getString("telephone");

                Student etudiant = new Student(id, nom, prenom, email, adresse, telephone);
                etudiants.add(etudiant);
            }

            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de la récupération des étudiants.");
        } finally {
            closeConnection();
        }

        return etudiants;
    }

    private void closeConnection() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Ajouter un étudiant
    private void ajouterEtudiant() {
        // Créer une nouvelle fenêtre pour l'ajout d'un étudiant
        Stage ajoutStage = new Stage();
        ajoutStage.setTitle("Ajouter un étudiant");

        nomInput = new TextField();
        prenomInput = new TextField();
        emailInput = new TextField();
        adresseInput = new TextField();
        telephoneInput = new TextField();

        Button addButton = new Button("Ajouter");
        
        addButton.setOnAction(e -> {
            String nom = nomInput.getText();
            String prenom = prenomInput.getText();
            String email = emailInput.getText();
            String adresse = adresseInput.getText();
            String telephone = telephoneInput.getText();

            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || adresse.isEmpty() || telephone.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs doivent être remplis.");
                return;
            }

            // Ajouter l'étudiant à la base de données
            String sql = "INSERT INTO etudiant (nom, prenom, email, adresse, telephone) VALUES (?, ?, ?, ?, ?)";
            try {
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, nom);
                stmt.setString(2, prenom);
                stmt.setString(3, email);
                stmt.setString(4, adresse);
                stmt.setString(5, telephone);
                stmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Étudiant ajouté avec succès.");

                // Actualiser la table des étudiants
                table.setItems(getEtudiant());

                // Fermer la fenêtre d'ajout
                ajoutStage.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de l'ajout de l'étudiant.");
            }
        });

        VBox ajoutBox = new VBox(10);
        ajoutBox.setPadding(new Insets(10));
        ajoutBox.getChildren().addAll(
                new Label("Nom:"),
                nomInput,
                new Label("Prénom:"),
                prenomInput,
                new Label("Email:"),
                emailInput,
                new Label("Adresse:"),
                adresseInput,
                new Label("Téléphone:"),
                telephoneInput,
                addButton
        );

        Scene scene = new Scene(ajoutBox,400,500);
        ajoutStage.setScene(scene);
        ajoutStage.show();
    }

    // Modifier un étudiant
    private void modifierEtudiant() {
        // Récupérer l'étudiant sélectionné dans la table
        Student etudiantSelectionne = table.getSelectionModel().getSelectedItem();
        if (etudiantSelectionne == null) {
        	showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun étudiant sélectionné.");
        	return;
        	}
        // Créer une nouvelle fenêtre pour la modification de l'étudiant
        Stage modificationStage = new Stage();
        modificationStage.setTitle("Modifier un étudiant");

        nomInput = new TextField(etudiantSelectionne.getNom());
        prenomInput = new TextField(etudiantSelectionne.getPrenom());
        emailInput = new TextField(etudiantSelectionne.getEmail());
        adresseInput = new TextField(etudiantSelectionne.getAdresse());
        telephoneInput = new TextField(etudiantSelectionne.getTelephone());

        Button updateButton = new Button("Modifier");
        
        updateButton.setOnAction(e -> {
            String nom = nomInput.getText();
            String prenom = prenomInput.getText();
            String email = emailInput.getText();
            String adresse = adresseInput.getText();
            String telephone = telephoneInput.getText();

            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || adresse.isEmpty() || telephone.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs doivent être remplis.");
                return;
            }

            // Modifier l'étudiant dans la base de données
            String sql = "UPDATE etudiant SET nom=?, prenom=?, email=?, adresse=?, telephone=? WHERE id=?";
            try {
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, nom);
                stmt.setString(2, prenom);
                stmt.setString(3, email);
                stmt.setString(4, adresse);
                stmt.setString(5, telephone);
                stmt.setInt(6, etudiantSelectionne.getId());
                stmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Étudiant modifié avec succès.");

                // Actualiser la table des étudiants
                table.setItems(getEtudiant());

                // Fermer la fenêtre de modification
                modificationStage.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de la modification de l'étudiant.");
            }
        });

        VBox modificationBox = new VBox(10);
        modificationBox.setPadding(new Insets(10));
        modificationBox.getChildren().addAll(
                new Label("Nom:"),
                nomInput,
                new Label("Prénom:"),
                prenomInput,
                new Label("Email:"),
                emailInput,
                new Label("Adresse:"),
                adresseInput,
                new Label("Téléphone:"),
                telephoneInput,
                updateButton
        );

        Scene scene = new Scene(modificationBox,400,500);
        modificationStage.setScene(scene);
        modificationStage.show();
    }

    // Supprimer un étudiant
    private void supprimerEtudiant() {
        // Récupérer l'étudiant sélectionné dans la table
        Student etudiantSelectionne = table.getSelectionModel().getSelectedItem();

        if (etudiantSelectionne == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun étudiant sélectionné.");
            return;
        }

        // Afficher une boîte de dialogue de confirmation
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Confirmation");
        confirmationDialog.setHeaderText("Supprimer l'étudiant");
        confirmationDialog.setContentText("Êtes-vous sûr de vouloir supprimer l'étudiant sélectionné ?");
        Optional<ButtonType> result = confirmationDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Supprimer l'étudiant de la base de données
            String sql = "DELETE FROM etudiant WHERE id=?";
            try {
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, etudiantSelectionne.getId());
                stmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Étudiant supprimé avec succès.");

                // Actualiser la table des étudiants
                table.setItems(getEtudiant());
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de la suppression de l'étudiant.");
            }
        }
    }

    // Afficher une boîte de dialogue d'alerte
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
 // Afficher le profil de l'étudiant
    private void afficherProfilEtudiant() {
        // Récupérer l'étudiant sélectionné dans la table
        Student etudiantSelectionne = table.getSelectionModel().getSelectedItem();
        if (etudiantSelectionne == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun étudiant sélectionné.");
            return;
        }

        // Créer les éléments de la nouvelle scène
        Label nomLabel = new Label("Nom: " + etudiantSelectionne.getNom());
        Label prenomLabel = new Label("Prénom: " + etudiantSelectionne.getPrenom());
        Label emailLabel = new Label("Email: " + etudiantSelectionne.getEmail());
        Label adresseLabel = new Label("Adresse: " + etudiantSelectionne.getAdresse());
        Label telephoneLabel = new Label("Téléphone: " + etudiantSelectionne.getTelephone());

        Button printButton = new Button("Imprimer");
       
        printButton.setOnAction(e -> imprimerFicheRecapitulative(etudiantSelectionne));

        // Créer la mise en page de la nouvelle scène
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(nomLabel, prenomLabel, emailLabel, adresseLabel, telephoneLabel, printButton);

        Scene newScene = new Scene(vbox, 400, 500);
        Stage newStage = new Stage();
        newStage.setTitle("Profil de l'étudiant");
        newStage.setScene(newScene);
        newStage.show();
    }

    // Imprimer la fiche récapitulative d'un étudiant
    private void imprimerFicheRecapitulative(Student etudiant) {
    	
    	    // Créer le contenu à imprimer
    	    StringBuilder contenuImpression = new StringBuilder();
    	    contenuImpression.append("\n");
    	    contenuImpression.append("Nom: ").append(etudiant.getNom()).append("\n");
    	    contenuImpression.append("Prénom: ").append(etudiant.getPrenom()).append("\n");
    	    contenuImpression.append("Email: ").append(etudiant.getEmail()).append("\n");
    	    contenuImpression.append("Adresse: ").append(etudiant.getAdresse()).append("\n");
    	    contenuImpression.append("Téléphone: ").append(etudiant.getTelephone()).append("\n");

    	    // Créer un objet PrinterJob
    	    PrinterJob job = PrinterJob.createPrinterJob();
    	    if (job != null) {
    	        // Afficher la boîte de dialogue d'impression
    	        boolean showDialog = job.showPrintDialog(null);
    	        if (showDialog) {
    	            // Créer un objet Text à partir du contenu à imprimer
    	            Text contenuText = new Text(contenuImpression.toString());

    	            // Imprimer le contenu
    	            boolean printed = job.printPage(contenuText);
    	            if (printed) {
    	                // Terminer l'impression
    	                job.endJob();
    	            } else {
    	                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'impression.");
    	            }
    	        }
    	    } else {
    	        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer une tâche d'impression.");
    	    }
    }

    

    public static void main(String[] args) {
        launch(args);
    }
}

