package de.mephisto.radiofx.ui;

import de.mephisto.radiofx.services.ServiceRegistry;
import de.mephisto.radiofx.ui.controller.*;
import de.mephisto.radiofx.util.UIUtil;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

/**
 * The global UI controller, receives the input from the state machine that
 * can be triggered via GPIO or via keyboard inputs.
 */
public class UIStateController {
  private static UIStateController instance = new UIStateController();
  private BorderPane borderPane;
  private Footer footer;

  private RadioUIController radioController;
  private WeatherUIController weatherController;
  private GoogleUINaviController googleNaviController;
  private GoogleUIPlayerController googlePlayerController;

  //radio controller is default
  private UIController activeController = radioController;

  private UIStateController() {
    borderPane = new BorderPane();
    borderPane.setOpacity(1);
  }

  public BorderPane getBorderPane() {
    return borderPane;
  }

  /**
   * Singleton getter
   * @return
   */
  public static UIStateController getInstance() {
    return instance;
  }

  public void showSplashScreen() {
    final SplashScreen splashScene = UIUtil.createSplashScene();
    ServiceRegistry.init(splashScene);
  }

  public void createControllers(SplashScreen splashScene) {
    splashScene.setMessage("Creating Radio Controller...", 0.9);
    radioController = new RadioUIController();
    splashScene.setMessage("Creating Weather Controller...", 0.92);
    weatherController = new WeatherUIController();
    splashScene.setMessage("Creating Google Navi Controller...", 0.94);
    googleNaviController = new GoogleUINaviController();
    splashScene.setMessage("Creating Google Player Controller...", 0.96);
    googlePlayerController = new GoogleUIPlayerController();
  }

  public void showDefault() {
    activeController = radioController;

    new Header(borderPane);
    footer = new Footer(borderPane);

    UIUtil.createScene(borderPane);
    activeController.display(borderPane);

    UIUtil.fadeInComponent(borderPane);
  }

  public void showNext() {
    UIController newController = (UIController) activeController.next();
    updateActiveController(newController);
  }

  public void showPrevious() {
    UIController newController = (UIController) activeController.prev();
    updateActiveController(newController);
  }

  public void push() {
    UIController newController = (UIController) activeController.push();
    updateActiveController(newController);
  }

  public void longPush() {
    UIController newController = (UIController) activeController.longPush();
    updateActiveController(newController);
  }

  /**
   * Checks if the active controller has changed.
   * The events are delegated to the new controller in this case.
   * @param newController
   */
  private synchronized void updateActiveController(final UIController newController) {
    if(!newController.equals(activeController)) {
      footer.switchTab(newController.getFooterId());
      activeController.onDispose();

      //check if the controller creates a new UI
      if(newController.getTabRoot() != null && activeController.getTabRoot() != null) {
        final Node center = newController.getTabRoot();
        final FadeTransition outFader = UIUtil.createOutFader(center);
        outFader.onFinishedProperty().set(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent actionEvent) {
            newController.display(borderPane);
          }
        });
        outFader.play();
      }
      else {
        newController.onDisplay();
      }

      activeController = newController;
    }
  }

  public WeatherUIController getWeatherController() {
    return weatherController;
  }

  public GoogleUINaviController getGoogleNaviController() {
    return googleNaviController;
  }

  public GoogleUIPlayerController getGooglePlayerController() {
    return googlePlayerController;
  }

  public RadioUIController getRadioController() {
    return radioController;
  }

  public void display(UIController controller) {
    controller.display(borderPane);
  }
}
