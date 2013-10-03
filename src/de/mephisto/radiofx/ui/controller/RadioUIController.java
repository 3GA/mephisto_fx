package de.mephisto.radiofx.ui.controller;

import de.mephisto.radiofx.services.IServiceModel;
import de.mephisto.radiofx.services.ServiceRegistry;
import de.mephisto.radiofx.services.mpd.IMpdService;
import de.mephisto.radiofx.services.mpd.StationInfo;
import de.mephisto.radiofx.ui.Pager;
import de.mephisto.radiofx.ui.UIStateController;
import de.mephisto.radiofx.util.UIUtil;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Controls the UI for the radio
 */
public class RadioUIController extends PageableUIController {
  private static final Font RADIO_STATION_FONT = Font.font("Tahoma", FontWeight.BOLD, 32);
  private static final Font RADIO_TRACK_FONT= Font.font("Tahoma", FontWeight.NORMAL, 18);
  private static final Font RADIO_URL_FONT= Font.font("Tahoma", FontWeight.NORMAL, 12);

  private static final String LOADING_MSG = "Resolving Station Info...";

  private Text stationText;
  private Text trackText;
  private Text urlText;
  private FadeTransition blink;

  private StationInfo activeStation;

  private IMpdService mpdService;

  public RadioUIController() {
    super(ServiceRegistry.getMpdService());
  }

  @Override
  public BorderPane init() {
    this.mpdService = ServiceRegistry.getMpdService();

    BorderPane tabRoot = new BorderPane();
    tabRoot.setMinHeight(UIUtil.MIN_MAIN_HEIGHT);

    VBox verticalRoot = new VBox(20);
    verticalRoot.setPadding(new Insets(30, 0, 0, 20));
    tabRoot.setCenter(verticalRoot);

    stationText = new Text(0, 0, "");
    stationText.setFont(RADIO_STATION_FONT);
    stationText.setFill(UIUtil.COLOR_DARK_HEADER);
    verticalRoot.getChildren().add(stationText);

    trackText = new Text(0, 0, "");
    trackText.setFont(RADIO_TRACK_FONT);
    trackText.setFill(UIUtil.COLOR_DARK_HEADER);
    verticalRoot.getChildren().add(trackText);

    blink = UIUtil.createBlink(trackText);
    blink.play();

    urlText = new Text(0, 0, "");
    urlText.setFont(RADIO_URL_FONT);
    urlText.setFill(UIUtil.COLOR_DARK_HEADER);

    verticalRoot.getChildren().add(urlText);

    super.setPager(new Pager(tabRoot, ServiceRegistry.getMpdService().getServiceData()));
    super.setTabRoot(tabRoot);

    final List<IServiceModel> serviceData = ServiceRegistry.getMpdService().getServiceData();
    if(!serviceData.isEmpty()) {
      activeStation = (StationInfo) serviceData.get(0);
      for(IServiceModel model : serviceData) {
        StationInfo info = (StationInfo) model;
        if(info.isActive())  {
          playStation(info);
          getPager().select(model);
          break;
        }
      }
      updatePage(activeStation);
    }

    return tabRoot;
  }

  /**
   * Updates the fields with the given station info.
   * @param model
   */
  @Override
  public void updatePage(IServiceModel model) {
    StationInfo info = (StationInfo) model;
    stationText.setText(formatValue(info.getName(), 26));
    if(StringUtils.isEmpty(info.getTrack())) {
      if(info.isActive()) {
        trackText.setText(LOADING_MSG);
      }
      else {
        trackText.setText("");
      }
    }
    else {
      trackText.setText(formatValue(info.getTrack(), 44));
      stopBlink();
    }
    urlText.setText(formatValue(info.getUrl(), 70));
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
      if(lastWhitespace < length) {
        length = lastWhitespace;
      }
      value = value.substring(0, length) + "...";
    }
    return value;
  }

  private void playStation(StationInfo info) {
    if(info != null && !info.equals(activeStation)) {
      trackText.setText(LOADING_MSG);
      blink.play();
      this.activeStation = info;
      mpdService.playStation(info);
      mpdService.forceRefresh();
      getPager().updateActivity();
    }
  }

  @Override
  public IRotaryControllable push() {
    StationInfo info = (StationInfo) getPager().getActiveModel();
    playStation(info);
    return this;
  }

  @Override
  public IRotaryControllable longPush() {
    return UIStateController.getInstance().getWeatherController();
  }
}
