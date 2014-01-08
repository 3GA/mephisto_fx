package de.mephisto.radiofx.ui;

import de.mephisto.radiofx.util.SceneUtil;
import de.mephisto.radiofx.util.TransitionUtil;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the components to refresh on the splash screen.
 */
public class SplashScreen {
  private static final Logger LOG = LoggerFactory.getLogger(SplashScreen.class);

  private ProgressBar progressBar;
  private Text loadingMsg;
  private Text statusMsg;
  private Node root;
  private double progress;

  public SplashScreen(Node root, ProgressBar progressBar, Text loadingMsg, Text statusMsg) {
    this.progressBar = progressBar;
    this.root = root;
    this.loadingMsg = loadingMsg;
    this.statusMsg = statusMsg;
  }

  public void setMessage(final String text, final double progress) {
    LOG.info("Splash: " + text);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        String msg = text;
        if(msg.length() > 60) {
          msg = msg.substring(0, 59) + "...";
        }
        loadingMsg.setText(msg);
        SplashScreen.this.progress = progress;
        progressBar.setProgress(progress);
      }
    });

  }

  public void dispose() {
    final FadeTransition outFader = TransitionUtil.createOutFader(root);
    outFader.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        SceneUtil.createNavigationScene(UIStateController.getInstance().getBorderPane());
        UIStateController.getInstance().showDefault();
      }
    });
    outFader.play();
  }

  public double getProgress() {
    return progress;
  }

  public void setStatusInfo(final String msg) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        statusMsg.setText(msg);
      }
    });
  }
}
