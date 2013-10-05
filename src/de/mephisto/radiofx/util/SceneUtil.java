package de.mephisto.radiofx.util;

import de.mephisto.radiofx.MephistoRadioFX;
import de.mephisto.radiofx.resources.ResourceLoader;
import de.mephisto.radiofx.ui.SplashScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 *
 */
public class SceneUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SceneUtil.class);

  /**
   * Creates the loading scene with progress bar.
   * @return
   */
  public static SplashScreen createSplashScene() {
    Group root = new Group();
    VBox vbox = new VBox(20);
    vbox.setAlignment(Pos.CENTER);
    root.getChildren().add(vbox);
    Canvas logoCanvas = TransitionUtil.createImageCanvas(ResourceLoader.getResource("logo.png"), TransitionUtil.WIDTH, 150);
    vbox.getChildren().add(logoCanvas);

    double y = TransitionUtil.WIDTH-50;
    ProgressBar loadingBar = new ProgressBar();
    loadingBar.setMinWidth(TransitionUtil.WIDTH - 100);
    loadingBar.setStyle("-fx-accent: " + Colors.HEX_COLOR_DARK_2 + ";");
    loadingBar.setLayoutY(y);
    vbox.getChildren().add(loadingBar);

    Text loadingMsg = new Text("Initializing...");
    loadingMsg.setFont(Fonts.FONT_NORMAL_14);
    vbox.getChildren().add(loadingMsg);

    try {
      HBox ipBox = new HBox();
      ipBox.setPadding(new Insets(20, 5, 5, 0));
      ipBox.setAlignment(Pos.BASELINE_RIGHT);
      Text ip = new Text("Version " + Version.VERSION + ", IP: " + Inet4Address.getLocalHost().getHostAddress());
      ip.setFont(Fonts.FONT_NORMAL_10);
      ipBox.getChildren().add(ip);
      vbox.getChildren().add(ipBox);
    } catch (UnknownHostException e) {
      LOG.error("Failed to resolved IP information: " + e.getMessage());
    }


    Stage primaryStage = MephistoRadioFX.getInstance().getStage();
    Scene scene = new Scene(root, TransitionUtil.WIDTH, TransitionUtil.HEIGHT, Color.valueOf(Colors.HEX_COLOR_BACKGROUND));
    primaryStage.setScene(scene);

    return new SplashScreen(vbox, loadingBar, loadingMsg);
  }

  /**
   * Creates the initial state of the the UI.
   *
   * @param borderPane
   */
  public static void createNavigationScene(BorderPane borderPane) {
    Group root = new Group();
    root.getChildren().add(borderPane);
    Stage primaryStage = MephistoRadioFX.getInstance().getStage();
    if (primaryStage.getScene() == null) {
      Scene scene = new Scene(root, TransitionUtil.WIDTH, TransitionUtil.HEIGHT, Color.valueOf(Colors.HEX_COLOR_BACKGROUND));
      scene.getStylesheets().add(ResourceLoader.getResource("theme.css"));
    }
    else {
      primaryStage.getScene().setRoot(root);
    }
  }
}
