package de.mephisto.radiofx.ui.controller;

import de.mephisto.radiofx.services.IServiceModel;
import de.mephisto.radiofx.services.ServiceRegistry;
import de.mephisto.radiofx.services.mpd.IMpdService;
import de.mephisto.radiofx.services.mpd.StationInfo;
import de.mephisto.radiofx.ui.Footer;
import de.mephisto.radiofx.ui.Pager;
import de.mephisto.radiofx.ui.UIStateController;
import de.mephisto.radiofx.util.Colors;
import de.mephisto.radiofx.util.Fonts;
import de.mephisto.radiofx.util.PaneUtil;
import de.mephisto.radiofx.util.TransitionUtil;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Controls the UI for the radio
 */
public class RadioUIController extends PageableUIController {
  private static final Font RADIO_STATION_FONT = Font.font("Tahoma", FontWeight.BOLD, 34);
  private static final Font RADIO_TRACK_FONT= Font.font("Tahoma", FontWeight.BOLD, 28);
  private static final Font RADIO_INTERPRET_FONT= Font.font("Tahoma", FontWeight.NORMAL, 28);

  private static final String LOADING_MSG = "Resolving Station Info...";
  private static final int REFRESH_TIMEOUT = 8000;

  private Text stationText;
  private Text interpretText;
  private Text trackText;
  private Text urlText;
  private FadeTransition blink;

  private IMpdService mpdService;
  private long selectionTime = new Date().getTime();
  private StationInfo activeStation;

  public RadioUIController() {
    super(ServiceRegistry.getMpdService());
  }

  @Override
  public BorderPane init() {
    this.mpdService = ServiceRegistry.getMpdService();

    BorderPane tabRoot = new BorderPane();
    tabRoot.setMinHeight(PaneUtil.MIN_MAIN_HEIGHT);

    VBox verticalRoot = new VBox(20);
    verticalRoot.setPadding(new Insets(40, 0, 0, 20));
    tabRoot.setCenter(verticalRoot);

    stationText = new Text(0, 0, "");
    stationText.setFont(RADIO_STATION_FONT);
    stationText.setFill(Colors.COLOR_DARK_HEADER);
    verticalRoot.getChildren().add(stationText);

    trackText = new Text(0, 0, "");
    trackText.setFont(RADIO_TRACK_FONT);
    trackText.setFill(Colors.COLOR_DARK_HEADER);
    verticalRoot.getChildren().add(trackText);

    interpretText = new Text(0, 0, "");
    interpretText.setFont(RADIO_INTERPRET_FONT);
    interpretText.setFill(Colors.COLOR_DARK_HEADER);
    verticalRoot.getChildren().add(interpretText);

    blink = TransitionUtil.createBlink(trackText);
    blink.play();

    urlText = new Text(0, 0, "");
    urlText.setFont(Fonts.FONT_NORMAL_16);
    urlText.setFill(Colors.COLOR_DARK_HEADER);

    verticalRoot.getChildren().add(urlText);

    super.setPager(new Pager(tabRoot, ServiceRegistry.getMpdService().getServiceData(false)));

    final List<IServiceModel> serviceData = ServiceRegistry.getMpdService().getServiceData(false);
    if(!serviceData.isEmpty()) {
      for(IServiceModel model : serviceData) {
        StationInfo info = (StationInfo) model;
        if(info.isActive())  {
          activeStation = info;
          getPager().select(model);
          break;
        }
      }
      updatePage(serviceData.get(0));
    }

    return tabRoot;
  }

  /**
   * Updates the fields with the given station info.
   * @param model
   */
  @Override
  public void updatePage(IServiceModel model) {
    if(!model.equals(getPager().getActiveModel())) {
      return;
    }

    StationInfo info = (StationInfo) model;
    stationText.setText(formatValue(info.getName(), 30));
    if(StringUtils.isEmpty(info.getTrack())) {
      if(info.isActive()) {
        long current = new Date().getTime();
        if(current-selectionTime > REFRESH_TIMEOUT) {
          trackText.setText("- not station info available -");
          interpretText.setText("");
          stopBlink();
        }
        else {
          trackText.setText(LOADING_MSG);
          interpretText.setText("");
        }
      }
      else {
        trackText.setText("");
        interpretText.setText("");
      }
    }
    else {
      String track = info.getTrack();
      String interpret = "";
      if(track.contains("-")) {
        interpret = track.substring(track.indexOf("-")+1, track.length()).trim();
        track = track.substring(0, track.indexOf("-")).trim();
      }
      if(interpret.contains("|")) {
        interpret = interpret.substring(0, interpret.indexOf("|")).trim();
      }
      trackText.setText(formatValue(track, 42));
      interpretText.setText(formatValue(interpret, 44));
      stopBlink();
    }
    urlText.setText(formatValue(info.getUrl(), 120));
  }

  @Override
  public int getFooterId() {
    return Footer.FOOTER_RADIO;
  }

  @Override
  public IRotaryControllable push() {
    if(getPager().getActiveModel() == this.activeStation) {
      getPager().next();
      push();
    }

    StationInfo info = (StationInfo) getPager().getActiveModel();
    playStation(info);
    return this;
  }

  @Override
  public IRotaryControllable longPush() {
    return UIStateController.getInstance().getWeatherController();
  }

  @Override
  public void onDisplay() {
    if(!mpdService.isRadioMode() && activeStation != null) {
      activeStation.setActive(false);
    }
    getPager().updateActivity();
  }

  private void stopBlink() {
    blink.stop();
    trackText.setOpacity(1);
  }

  /**
   * Formats the string before output.
   * @param value
   * @param length
   * @return
   */
  private String formatValue(String value, int length)  {
    if(value != null && value.length() > length) {
      int lastWhitespace = value.lastIndexOf(" ");
      if(lastWhitespace > 0 && lastWhitespace < length) {
        length = lastWhitespace;
      }
      value = value.substring(0, length) + "...";
      value = value.replaceAll("\"", "");
    }
    return value;
  }

  private void playStation(StationInfo info) {
    this.activeStation = info;
    selectionTime = new Date().getTime();
    trackText.setText(LOADING_MSG);
    interpretText.setText("");
    blink.play();
    mpdService.playStation(info);
    mpdService.forceRefresh();
    getPager().updateActivity();
  }
}
