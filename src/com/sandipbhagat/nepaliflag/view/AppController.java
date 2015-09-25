package com.sandipbhagat.nepaliflag.view;
/**
 * Created by sandip on 8/14/15.
 */

import com.sandipbhagat.nepaliflag.MainApp;
import com.sandipbhagat.nepaliflag.service.ImageLoadService;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.*;

public class AppController {

    private MainApp mainApp;

    private Canvas userCanvas;
    private Double transparencyLevel;
    private Image userImage;
    private ProgressIndicator imageLoadingProgressBar;

    @FXML private BorderPane borderpane;
    @FXML private StackPane contentPane;
    @FXML private HBox imageHbox;
    @FXML private ScrollPane scrollPane;
    @FXML private Button loadButton;
    @FXML private Button clearButton;
    @FXML private Button saveButton;
    @FXML private Button aboutButton;
    @FXML private Slider transparencySlider;


    @FXML
    public void initialize() {

        resetContentPane();

        transparencyLevel = 0.3;
        transparencySlider.setValue(transparencyLevel);

        imageLoadingProgressBar = new ProgressIndicator(0);
        imageLoadingProgressBar.setMaxSize(100, 100);

        setupContentPaneDragAndDropListener();
        setupTransparencySliderChangeListener();
        setupLoadButtonActionListener();
        setupSaveButtonActionListener();
        setupClearButtonActionListener();
        setupAboutButtonOnActionListener();

    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private void setupContentPaneDragAndDropListener() {
        contentPane.setOnDragOver(event -> mouseDragOver(event));
        contentPane.setOnDragDropped(event -> mouseDragDropped(event));
        contentPane.setOnDragExited(event -> {
            contentPane.setStyle("-fx-border-color: none;"
                    + "-fx-border-style: none;");
        });
    }

    private void setupLoadButtonActionListener() {
        loadButton.setOnAction(event -> {

            // create a FileChooser object
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select an image");

            // set extension filter
            FileChooser.ExtensionFilter extensionFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
            FileChooser.ExtensionFilter extensionFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
            fileChooser.getExtensionFilters().addAll(extensionFilterJPG, extensionFilterPNG);

            // Show open file dialog
            File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

            if (file == null) {
                return;
            }

            try {
                if (file != null) {

                    // show progress bar
                    contentPane.getChildren().add(imageLoadingProgressBar);

                    // load image from file path
                    loadImage(file.getAbsolutePath());

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
    }

    private void setupSaveButtonActionListener() {
        saveButton.setOnAction(event -> {

            if(this.userCanvas == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initModality(Modality.WINDOW_MODAL);
                alert.initOwner(mainApp.getPrimaryStage());
                alert.setTitle("Error");
                alert.setHeaderText("No Image");
                alert.setContentText("Please load image first!");
                alert.showAndWait();
            } else {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save As");
                fileChooser.setInitialFileName("untitled.png");
                FileChooser.ExtensionFilter extensionFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
                fileChooser.getExtensionFilters().addAll(extensionFilterPNG);

                // Show save file dialog
                File outputFile = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

                if (outputFile == null) {
                    return;
                }

                saveImage(outputFile);
            }
        });
    }

    private void resetContentPane() {
        if(!contentPane.getChildren().isEmpty()) {
            contentPane.getChildren().remove(scrollPane);
            scrollPane = new ScrollPane();
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            imageHbox = null;
            imageHbox = new HBox();
            imageHbox.setPadding(new Insets(15));
            imageHbox.setSpacing(20);

        } else {
            scrollPane = new ScrollPane();
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            imageHbox = new HBox();
            imageHbox.setPadding(new Insets(15));
            imageHbox.setSpacing(20);
        }
    }

    private void setupClearButtonActionListener() {
        clearButton.setOnAction(event -> {
            this.transparencyLevel = 0.3;
            this.transparencySlider.setValue(transparencyLevel);
            contentPane.setStyle("-fx-border-color: #3F3F3F;"
                    + "-fx-border-width: 5px;"
                    + "-fx-background-color: #e3e3e3;"
                    + "-fx-border-style: dashed;");
            userImage = null;
            userCanvas = null;
            resetContentPane();
            setupContentPaneDragAndDropListener();
        });

    }

    private void loadImage(String filePath) {

        // create a new ImageLoadService object and pass file path
        ImageLoadService imageLoadService = new ImageLoadService(filePath);

        // unbind progressProperty listeners of imageLoadingProgressBar
        imageLoadingProgressBar.progressProperty().unbind();

        // bind progressProperty of imageLoadingProgressBar with progressProperty of imageLoadService
        imageLoadingProgressBar.progressProperty().bind(imageLoadService.progressProperty());

        // start the service
        imageLoadService.start();

        // create final image after the service succeeds
        imageLoadService.setOnSucceeded(event1 -> {
            contentPane.setStyle("-fx-border-color: none;"
                    + "-fx-border-style: none;");
            contentPane.getChildren().remove(imageLoadingProgressBar);
            createFinalImage(imageLoadService.getValue());
        });
    }

    private void saveImage(File file){
        try {

            int w = (int) this.userCanvas.getWidth();
            int h = (int) this.userCanvas.getHeight();

            WritableImage writableImage = new WritableImage(w, h);
            this.userCanvas.snapshot(null, writableImage);

            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initModality(Modality.WINDOW_MODAL);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("Image Saved");
            alert.setHeaderText("Image saved");
            alert.setContentText("You image has been saved!");
            alert.showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    // turn the image into black and white image
    private Image applyBlackAndWhiteEffect(Image image) {

        // store the image width
        int w = (int) image.getWidth();

        // store the image height
        int h = (int) image.getHeight();

        // create a WritableImage
        WritableImage wi = new WritableImage(w, h);

        // get the PixelReader of image
        PixelReader pixelReader = image.getPixelReader();

        // get the PixelWriter of writable image
        PixelWriter pixelWriter = wi.getPixelWriter();

        // replace each pixel with gray scale pixel
        for(int y = 0; y < h; y++) {
            for(int x = 0; x < w; x++){
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color.grayscale());
            }
        }

        // return the writable image
        return wi;
    }

    // apply flag colour to a image
    private Canvas applyFlagEffect(Image image) {

        // image width
        final double w = image.getWidth();

        // image height
        final double h = image.getHeight();

        // create a canvas with dimension equal to image dimensions
        Canvas canvas = new Canvas(w, h);

        // graphic context
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // draw image
        gc.drawImage(image, 0, 0);

        // draw footer text
        gc.setFill(Color.web("#333333"));
        gc.fillText("By Sandip Bhagat", w - 120, h - 10);

        // set blending mode to multiply
        gc.setGlobalBlendMode(BlendMode.MULTIPLY);

        // set transparency level
        gc.setGlobalAlpha(transparencyLevel);

        // draw red rectangle
        gc.setFill(Color.RED);
        gc.fillRect(0, 0, w, h / 3);

        // draw blue rectangle
        gc.setFill(Color.BLUE);
        gc.fillRect(0, h / 3, w, h / 3);

        // draw white rectangle
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 2 * (h / 3), w, h / 3);

        // return the canvas
        return canvas;
    }

    // create final image
    private void createFinalImage(Image image) {

        // set the image view to the given original image
        ImageView userImageView = new ImageView();
        this.userImage = image;
        userImageView.setImage(this.userImage);
        // userImageView.setEffect(new DropShadow(5, 0.0, 0.0, Color.BLACK));

        // apply flag effect to the black and white image and store in a canvas object
        this.userCanvas = applyFlagEffect(applyBlackAndWhiteEffect(this.userImage));
        // this.userCanvas.setEffect(new DropShadow(5, 0.0, 0.0, Color.BLACK));

        addImageAndCanvasToContentPane(userImageView, this.userCanvas);
    }

    private void addImageAndCanvasToContentPane(ImageView imageView, Canvas canvas) {

        resetContentPane();

        // add userImageView and canvas node to imageHbox
        addNodeToImageHbox(imageView);
        addNodeToImageHbox(canvas);

        // add imageHbox node to scrollPane
        scrollPane.setContent(imageHbox);

        // add ScrollPane to contentPane
        contentPane.getChildren().add(scrollPane);
    }

    // add node to imageHbox node
    private void addNodeToImageHbox(Node node) {
        imageHbox.getChildren().add(node);
    }

    private void setupTransparencySliderChangeListener() {
        transparencySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            transparencyLevel = (Double) newValue;
            // System.out.println("Transparency set to: " + transparencyLevel);

            if(this.userCanvas != null) {
                // apply flag effect to the black and white image and store in a canvas object
                this.userCanvas = applyFlagEffect(applyBlackAndWhiteEffect(this.userImage));
                // this.userCanvas.setEffect(new DropShadow(5, 0.0, 0.0, Color.BLACK));
                imageHbox.getChildren().remove(1);
                imageHbox.getChildren().add(userCanvas);
            }
        });

    }

    private void mouseDragDropped(final DragEvent e) {
        final Dragboard db = e.getDragboard();
        boolean success = false;
        if(db.hasFiles()) {
            success = true;

            // only get the first file from the list
            final File file = db.getFiles().get(0);
            try {

                // show progress bar
                contentPane.getChildren().add(imageLoadingProgressBar);

                // load image from file path
                loadImage(file.getAbsolutePath());

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        e.setDropCompleted(success);
        e.consume();
    }

    private void mouseDragOver(final DragEvent e) {
        final Dragboard db = e.getDragboard();

        final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".png")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpeg")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpg");

        if(db.hasFiles()) {
            if(isAccepted) {
                contentPane.setStyle("-fx-border-color: #47D2E9;"
                        + "-fx-border-width: 5px;"
                        // + "-fx-background-color: #c6c6c6;"
                        + "-fx-border-style: dashed;");
                e.acceptTransferModes(TransferMode.COPY);
            }
        } {
            e.consume();
        }
    }



    private void setupAboutButtonOnActionListener() {
        aboutButton.setOnAction( event -> {

            StringBuilder creditText = new StringBuilder();
            creditText.append("This software enables you to embed Nepal flag colour on your profile picture.");
            creditText.append(" I (Sandip Bhagat) is the developer of this software.");
            creditText.append(" You can email me at sandipbgt@gmail.com or be friend");
            creditText.append(" with me on facebook at www.facebook.com/sandip02\n");
            creditText.append("You can also find me on my blog at www.sandipbhagat.com");

            Stage aboutStage = new Stage();
            aboutStage.setTitle("About");
            aboutStage.initModality(Modality.WINDOW_MODAL);
            aboutStage.initOwner(mainApp.getPrimaryStage());
            aboutStage.centerOnScreen();
            aboutStage.setResizable(false);

            VBox root = new VBox();
            root.setSpacing(10);

            Label aboutLabel = new Label();
            aboutLabel.setText(creditText.toString());
            aboutLabel.setWrapText(true);
            aboutLabel.setFont(new Font(17));
            aboutLabel.setPadding(new Insets(10));
            root.getChildren().add(aboutLabel);

            Scene scene = new Scene(root, 350, 250);
            aboutStage.setScene(scene);
            aboutStage.showAndWait();
        });
    }
}
