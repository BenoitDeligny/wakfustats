package com.wakfoverlay.ui;

import com.wakfoverlay.domain.player.model.Player;
import com.wakfoverlay.domain.player.model.Players;
import com.wakfoverlay.domain.player.port.primary.FetchPlayer;
import com.wakfoverlay.domain.player.port.primary.UpdatePlayerDamages;
import com.wakfoverlay.exposition.TheAnalyzer;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import static javafx.scene.layout.Priority.ALWAYS;

public class MainWindow extends VBox {
    private final List<String> EVOLUTIVE_RECTANGLE_COLORS = Arrays.asList(
            "#33fffe", "#EE1DED", "#7DFA00", "#FF4A00", "#FBE33B", "#6D1FA9"
    );

    private final VBox playersContainer;
    private final Pane resizeHandle;
    private String selectedFilePath;

    private final FetchPlayer fetchPlayer;
    private final UpdatePlayerDamages updatePlayerDamages;
    private final TheAnalyzer theAnalyzer;

    public MainWindow(FetchPlayer players, UpdatePlayerDamages updatePlayerDamages, TheAnalyzer theAnalyzer) {
        this.setStyle("-fx-background-color: rgb(18, 18, 18); -fx-border-color: rgb(51, 51, 51); -fx-border-width: 1;");
        this.setSpacing(2);
        this.setPadding(new Insets(5, 5, 5, 5));

        this.fetchPlayer = players;
        this.updatePlayerDamages = updatePlayerDamages;
        this.theAnalyzer = theAnalyzer;
        this.selectedFilePath = loadFilePath();

        HBox titleBar = createTitleBar();
        this.getChildren().add(titleBar);

        playersContainer = new VBox();
        playersContainer.setSpacing(2);

        ScrollPane scrollPane = new ScrollPane(playersContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: rgb(18, 18, 18); -fx-background-color: rgb(18, 18, 18);");

        VBox.setVgrow(scrollPane, ALWAYS);

        resizeHandle = new Pane();
        resizeHandle.setPrefHeight(5);
        resizeHandle.setMaxHeight(5);
        resizeHandle.setMinHeight(5);
        resizeHandle.setStyle("-fx-background-color: transparent;");
        resizeHandle.setCursor(Cursor.S_RESIZE);

        this.getChildren().addAll(scrollPane, resizeHandle);

        setupResizeHandlers();
    }

    private void setupResizeHandlers() {
        final double[] startY = new double[1];
        final double[] startHeight = new double[1];

        resizeHandle.setOnMousePressed(event -> {
            startY[0] = event.getScreenY();
            if (getScene() != null && getScene().getWindow() != null) {
                startHeight[0] = getScene().getWindow().getHeight();
            }
            event.consume();
        });

        resizeHandle.setOnMouseDragged(event -> {
            if (getScene() != null && getScene().getWindow() != null) {
                Stage stage = (Stage) getScene().getWindow();
                double deltaY = event.getScreenY() - startY[0];
                double newHeight = startHeight[0] + deltaY;
                if (newHeight > 100) {
                    stage.setHeight(newHeight);
                }
            }
            event.consume();
        });
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setPadding(new Insets(5));
        titleBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label label = new Label("DPT Meter");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, ALWAYS);

        Button folderButton = createIconButton("📁", "#2ecc71");
        folderButton.setOnAction(event -> openFolder());

        Button resetButton = createIconButton("🔄", "#3498db");
        resetButton.setOnAction(event -> resetStats());

        Button closeButton = createIconButton("✖", "#e74c3c");
        closeButton.setOnAction(event -> closeWindow());

        titleBar.getChildren().addAll(label, spacer, folderButton, resetButton, closeButton);

        return titleBar;
    }

    private Button createIconButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: " + color + "; " +
                        "-fx-font-size: 15px; " +
                        "-fx-padding: 2 5; " +
                        "-fx-min-width: 20px; " +
                        "-fx-min-height: 20px; " +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    private void openFolder() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");

        String lastPath = loadFilePath();
        if (lastPath != null) {
            File lastFile = new File(lastPath);
            if (lastFile.exists()) {
                fileChooser.setInitialDirectory(lastFile.getParentFile());
            } else {
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            }
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        FileChooser.ExtensionFilter logFilter =
                new FileChooser.ExtensionFilter("Fichiers Log (*.log)", "*.log");
        FileChooser.ExtensionFilter textFilter =
                new FileChooser.ExtensionFilter("Fichiers Texte", "*.txt");
        FileChooser.ExtensionFilter allFilter =
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*");
        fileChooser.getExtensionFilters().addAll(logFilter, textFilter, allFilter);

        File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());

        if (selectedFile != null) {
            this.selectedFilePath = selectedFile.getAbsolutePath();
            saveFilePath(this.selectedFilePath);
        }
    }

    private void saveFilePath(String filePath) {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        prefs.put("selectedFilePath", filePath);
    }

    private String loadFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        return prefs.get("selectedFilePath", null);
    }

    private void resetStats() {
        updatePlayerDamages.resetPlayersDamages();
    }

    private void closeWindow() {
        Stage stage = (Stage) this.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    public void updateDisplay() {
        playersContainer.getChildren().clear();

        TheAnalyzer.FileReadStatus status = theAnalyzer.readLogFile(selectedFilePath);

        if (status != TheAnalyzer.FileReadStatus.SUCCESS) {
            String message = switch (status) {
                case NO_FILE_SELECTED -> "Aucun fichier sélectionné. Veuillez sélectionner un fichier de log.";
                case FILE_NOT_FOUND -> "Le fichier sélectionné n'existe pas.";
                case EMPTY_FILE -> "Le fichier sélectionné est vide.";
                case IO_ERROR -> "Erreur lors de la lecture du fichier.";
                default -> "Erreur inconnue lors de la lecture du fichier.";
            };

            displayMessage(message);
            return;
        }

        Players rankedPlayers = fetchPlayer.rankedPlayers();
        if (rankedPlayers.players().isEmpty()) {
            displayMessage("Aucune donnée disponible dans le fichier sélectionné.");
            return;
        }

        int totalDamages = rankedPlayers.players().stream().mapToInt(Player::damages).sum();

        for (int i = 0; i < rankedPlayers.players().size(); i++) {
            Player player = rankedPlayers.players().get(i);

            HBox playerBox = new HBox();
            playerBox.setPadding(new Insets(2, 5, 2, 5));
            playerBox.setSpacing(10);

            Label playerNameLabel = new Label(player.name());
            playerNameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            playerNameLabel.setMinWidth(50);

            Pane pane = new Pane();
            pane.setPrefSize(100, 20);
            HBox.setHgrow(pane, ALWAYS);

            Rectangle border = new Rectangle(0, 0, 100, 20);
            border.setStroke(Color.GRAY);
            border.setFill(Color.TRANSPARENT);

            Rectangle evolutiveRectangle = new Rectangle(0, 0, (totalDamages == 0 ? 0 : (player.damages() / (double) totalDamages) * 100), 20);
            String bgColor = EVOLUTIVE_RECTANGLE_COLORS.get(i % EVOLUTIVE_RECTANGLE_COLORS.size());
            evolutiveRectangle.setFill(Color.web(bgColor));

            Label damageLabel = new Label(player.damages() + " (" + String.format("%.2f", ((double) player.damages() / totalDamages) * 100) + "%)");
            damageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

            pane.getChildren().addAll(border, evolutiveRectangle);

            playerBox.getChildren().addAll(playerNameLabel, pane, damageLabel);

            playersContainer.getChildren().add(playerBox);
        }
    }

    private void displayMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        HBox messageBox = new HBox(messageLabel);
        messageBox.setAlignment(javafx.geometry.Pos.CENTER);
        messageBox.setPadding(new Insets(20, 0, 0, 0));

        playersContainer.getChildren().add(messageBox);
    }
}
