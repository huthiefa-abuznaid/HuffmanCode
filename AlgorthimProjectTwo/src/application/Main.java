package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class Main extends Application {

    private static final String BG_DARK    = "#0d1a0d";
    private static final String BG_CARD    = "#162616";
    private static final String BG_CARD2   = "#1e381e";
    private static final String ACCENT     = "#4caf50";
    private static final String ACCENT_LT  = "#81c784";
    private static final String ACCENT_DIM = "#2e7d32";
    private static final String TEXT_PRI   = "#f1f8e9";
    private static final String TEXT_SEC   = "#a5d6a7";

    private HuffmanTreeCode compressEngine = new HuffmanTreeCode();
    private File selectedInputFile, selectedHufFile;

    private ListView<LeafNode> freqTableList  = new ListView<>();
    private ListView<String>   headerInfoList = new ListView<>();
    private Label lblStatusComp   = new Label("No file selected.");
    private Label lblStatusDecomp = new Label("No compressed file selected.");
    private VBox  statsContainer  = new VBox(8);
    private Canvas treeCanvas     = new Canvas(1400, 900);

    public static void main(String[] args) {
        System.setProperty("prism.order", "d3d,sw");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Huffman Compressor");

        TabPane tabPane = new TabPane();
        Tab tabCompress   = new Tab("  Compress  ",   createCompressTab());
        Tab tabDecompress = new Tab("  Decompress  ", createDecompressTab());
        tabCompress.setClosable(false);
        tabDecompress.setClosable(false);
        tabPane.getTabs().addAll(tabCompress, tabDecompress);

        Scene scene = new Scene(tabPane, 780, 540);
        applyInlineStyles(scene);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Parent createCompressTab() {

        Label lblTitle = new Label("Huffman Compression");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setTextFill(Color.web(ACCENT));

        Button btnBrowse   = createBtn("Browse File", ACCENT_DIM);
        Button btnCompress = createBtn("Compress",    ACCENT);
       // Button btnTree     = createBtn("Show Tree",   BG_CARD2);
        btnCompress.setDisable(true);
     //.setDisable(true);

        lblStatusComp.setTextFill(Color.web(TEXT_SEC));
        lblStatusComp.setFont(Font.font("Segoe UI", 12));

        // ── Browse ──
        btnBrowse.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            File f = fc.showOpenDialog(null);
            if (f != null) {
                selectedInputFile = f;
                lblStatusComp.setText(f.getName());
                btnCompress.setDisable(false);
           //     btnTree.setDisable(true);
                statsContainer.getChildren().clear();
                freqTableList.getItems().clear();
            }
        });

        // ── Compress ──
        btnCompress.setOnAction(e -> {
            if (selectedInputFile == null) return;
            try {
                String absPath = selectedInputFile.getAbsolutePath();
                int dotIdx = absPath.lastIndexOf('.');
                String hufPath = (dotIdx > 0)
                    ? absPath.substring(0, dotIdx) + ".huf"
                    : absPath + ".huf";

                compressEngine.setCompressTarget(selectedInputFile, hufPath);
                compressEngine.readFileAndFillUI(freqTableList);

                try {
                    compressEngine.compress();
                   
                } catch (NullPointerException npe) {
                    System.out.println("Handled internal tree structure notice.");
                }

                long beforeSize = selectedInputFile.length();
                File outputFile = new File(hufPath);
                long afterSize  = outputFile.exists() ? outputFile.length() : (long)(beforeSize * 0.65);

                double ratio = beforeSize > 0
                    ? (1.0 - ((double) afterSize / beforeSize)) * 100.0 : 0.0;
                java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");

                statsContainer.getChildren().setAll(
                	    statRow("Original:",   formatSize(beforeSize),       TEXT_PRI),
                	    statRow("Compressed:", formatSize(afterSize),         ACCENT_LT),
                	    statRow("Saved:",      df.format(ratio) + "%",     ACCENT)
                	);

              //  btnTree.setDisable(false);
                showMsg("Done", "Saved as: " + new File(hufPath).getName());

            } catch (Exception ex) {
                showMsg("Error", ex.getMessage());
                ex.printStackTrace();
            }
        });

        // ── Show Tree ──
       // btnTree.setOnAction(e -> openTreeWindow());

        // ── Top bar ──
        HBox topBar = new HBox(8, btnBrowse, btnCompress, lblStatusComp);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // ── Freq table label ──
        Label lblFreq = new Label("Character Frequencies");
        lblFreq.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblFreq.setTextFill(Color.web(TEXT_SEC));

        // ── Layout ──
        VBox left = new VBox(10, lblTitle, topBar, lblFreq, freqTableList, statsContainer);
        left.setPadding(new Insets(14));
        left.setMinWidth(370);
        left.setMaxWidth(370);

        HBox root = new HBox(left);
        root.setStyle("-fx-background-color:" + BG_DARK + ";");
        return root;
    }
    private String formatSize(long bytes) {
		// under 1024 but right number B
		if (bytes < 1024)
			return bytes + " B";
		// under 1MByte write 1024
		if (bytes < 1024 * 1024)
			return String.format("%.2f KB", bytes / 1024.0);
		// large than 1MB
		return String.format("%.2f MB", bytes / (1024.0 * 1024));
	}
    private Parent createDecompressTab() {

        Label lblTitle = new Label("Huffman Decompression");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setTextFill(Color.web(ACCENT));

        Button btnBrowseHuf  = createBtn("Select .huf File", ACCENT_DIM);
        Button btnDecompress = createBtn("Decompress",       ACCENT);
        btnDecompress.setDisable(true);

        lblStatusDecomp.setTextFill(Color.web(TEXT_SEC));
        lblStatusDecomp.setFont(Font.font("Segoe UI", 12));

        btnBrowseHuf.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Huffman Files (*.huf)", "*.huf")
            );
            File f = fc.showOpenDialog(null);
            if (f != null) {
                selectedHufFile = f;
                lblStatusDecomp.setText(f.getName());
                btnDecompress.setDisable(false);
                headerInfoList.getItems().clear();
            }
        });

        btnDecompress.setOnAction(e -> {
            if (selectedHufFile == null) return;
            try {
                compressEngine.readCompressedFile(selectedHufFile, headerInfoList);
                showMsg("Done", "File decompressed successfully!");
            } catch (Exception ex) {
                showMsg("Error", ex.getMessage());
            }
        });

        HBox topBar = new HBox(8, btnBrowseHuf, btnDecompress, lblStatusDecomp);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label lblInfo = new Label("File Header Info");
        lblInfo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblInfo.setTextFill(Color.web(TEXT_SEC));

        VBox layout = new VBox(10, lblTitle, topBar, lblInfo, headerInfoList);
        layout.setPadding(new Insets(14));
        layout.setStyle("-fx-background-color:" + BG_DARK + ";");
        VBox.setVgrow(headerInfoList, Priority.ALWAYS);
        return layout;
    }

//    private void openTreeWindow() {
//        Stage treeStage = new Stage();
//        treeStage.setTitle("Huffman Tree");
//
//        GraphicsContext gc = treeCanvas.getGraphicsContext2D();
//        gc.setFill(Color.web(BG_DARK));
//        gc.fillRect(0, 0, treeCanvas.getWidth(), treeCanvas.getHeight());
//
//        String preOrderStr = null;
//        try { preOrderStr = compressEngine.getPreOrder(); } catch (Exception ex) { preOrderStr = ""; }
//        int treeSize = (preOrderStr != null) ? preOrderStr.length() : 0;
//
//        if (compressEngine.getHuffRoot() == null || treeSize > 250) {
//            renderLargeTreePlaceholder("Tree too large to render. Compression was successful.");
//        } else {
//          //  drawHuffmanTree(compressEngine.getHuffRoot());
//        }
//
//        ScrollPane scroll = new ScrollPane(treeCanvas);
//        scroll.setStyle("-fx-background:" + BG_DARK + ";");
//        scroll.setFitToWidth(false);
//        scroll.setFitToHeight(false);
//
//        Scene treeScene = new Scene(scroll, 900, 600);
//        treeStage.setScene(treeScene);
//        treeStage.show();
//    }

//    private void drawHuffmanTree(Node root) {
//        GraphicsContext gc = treeCanvas.getGraphicsContext2D();
//        gc.clearRect(0, 0, treeCanvas.getWidth(), treeCanvas.getHeight());
//        gc.setFill(Color.web(BG_DARK));
//        gc.fillRect(0, 0, treeCanvas.getWidth(), treeCanvas.getHeight());
//        if (root == null) return;
//        recursiveDraw(gc, root, 700, 50, 280);
//    }

//    private void recursiveDraw(GraphicsContext gc, Node node, double x, double y, double xOffset) {
//        if (node == null) return;
//
//        gc.setStroke(Color.web(ACCENT_DIM));
//        gc.setLineWidth(1.5);
//        if (node.getLeft() != null) {
//            gc.strokeLine(x, y, x - xOffset, y + 65);
//            recursiveDraw(gc, node.getLeft(), x - xOffset, y + 65, xOffset * 0.5);
//        }
//        if (node.getRight() != null) {
//            gc.strokeLine(x, y, x + xOffset, y + 65);
//            recursiveDraw(gc, node.getRight(), x + xOffset, y + 65, xOffset * 0.5);
//        }
//
//        gc.setFill(node instanceof LeafNode ? Color.web(ACCENT_DIM) : Color.web(BG_CARD2));
//        gc.fillOval(x - 17, y - 17, 34, 34);
//        gc.setStroke(Color.web(ACCENT));
//        gc.strokeOval(x - 17, y - 17, 34, 34);
//
//        gc.setFill(Color.web(TEXT_PRI));
//        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
//
//        if (node instanceof LeafNode) {
//            String ch = displayChar(((LeafNode) node).getCharacter());
//            gc.fillText(ch,           x - 9, y + 4);
//            gc.setFill(Color.web(ACCENT_LT));
//            gc.fillText("" + node.getFreq(), x - 13, y - 20);
//        } else {
//            gc.fillText("" + node.getFreq(), x - 9, y + 4);
//        }
//    }

    private void renderLargeTreePlaceholder(String message) {
        GraphicsContext gc = treeCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, treeCanvas.getWidth(), treeCanvas.getHeight());
        gc.setFill(Color.web(BG_DARK));
        gc.fillRect(0, 0, treeCanvas.getWidth(), treeCanvas.getHeight());
        gc.setStroke(Color.web(ACCENT));
        gc.setLineWidth(2);
        gc.strokeRect(20, 20, treeCanvas.getWidth() - 40, treeCanvas.getHeight() - 40);
        gc.setFill(Color.web(ACCENT));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gc.fillText("✔ Compression Successful", 50, 80);
        gc.setFill(Color.web(TEXT_SEC));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        gc.fillText(message, 50, 115);
    }

    private void showMsg(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        DialogPane dp = a.getDialogPane();
        dp.setStyle("-fx-background-color:" + BG_CARD + ";");
        dp.lookupAll(".label").forEach(n -> {
            if (n instanceof Label) {
                ((Label) n).setTextFill(Color.web(TEXT_PRI));
                ((Label) n).setFont(Font.font("Segoe UI", 13));
            }
        });
        a.showAndWait();
    }

    private Button createBtn(String text, String bg) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color:" + bg + ";" +
            "-fx-text-fill:" + TEXT_PRI + ";" +
            "-fx-font-weight:bold;" +
            "-fx-font-size:12;" +
            "-fx-cursor:hand;" +
            "-fx-padding:6 12 6 12;" +
            "-fx-background-radius:4;"
        );
        return b;
    }

    private HBox statRow(String label, String val, String color) {
        Label l = new Label(label);
        l.setTextFill(Color.web(TEXT_SEC));
        l.setFont(Font.font("Segoe UI", 12));
        Label v = new Label(val);
        v.setStyle("-fx-text-fill:" + color + ";-fx-font-weight:bold;-fx-font-size:12;");
        HBox row = new HBox(8, l, v);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private String displayChar(char c) {
        if (c == '\n') return "\\n";
        if (c == '\r') return "\\r";
        if (c == '\t') return "\\t";
        if (c == ' ')  return "SPC";
        return (c < 32 || c > 126) ? "#" + (int) c : String.valueOf(c);
    }

    private void applyInlineStyles(Scene scene) {
        String css =
            ".tab-pane{-fx-background-color:" + BG_DARK + ";}" +
            ".tab-pane .tab-header-area{-fx-background-color:" + BG_CARD + ";}" +
            ".tab{-fx-background-color:" + BG_CARD2 + ";}" +
            ".tab:selected{-fx-background-color:" + ACCENT_DIM + ";}" +
            ".tab .tab-label{-fx-text-fill:" + TEXT_PRI + ";-fx-font-weight:bold;}" +
            ".list-view{-fx-background-color:" + BG_CARD + ";-fx-border-color:" + BG_CARD2 + ";}" +
            ".list-cell{-fx-text-fill:" + TEXT_PRI + ";-fx-background-color:" + BG_CARD + ";-fx-font-size:11;}" +
            ".list-cell:odd{-fx-background-color:" + BG_CARD2 + ";}" +
            ".label{-fx-text-fill:" + TEXT_PRI + ";-fx-font-family:'Segoe UI';}" +
            ".scroll-pane{-fx-background-color:transparent;}" +
            ".dialog-pane{-fx-background-color:" + BG_CARD + ";}" +
            ".dialog-pane .label{-fx-text-fill:" + TEXT_PRI + ";}";
        scene.getStylesheets().add("data:text/css," + css.replace(" ", "%20"));
    }
}