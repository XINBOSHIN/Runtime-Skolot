package pro.gravit.launcher.client.gui.scenes.servermenu;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.json.simple.JSONObject;
import pro.gravit.launcher.client.DirBridge;
import pro.gravit.launcher.client.animatefx.animation.BounceIn;
import pro.gravit.launcher.client.animatefx.animation.FadeInServer;
import pro.gravit.launcher.client.animatefx.animation.FadeOutServer;
import pro.gravit.launcher.client.animatefx.animation.ZoomIn;
import pro.gravit.launcher.client.gui.JavaFXApplication;
import pro.gravit.launcher.client.gui.helper.LookupHelper;
import pro.gravit.launcher.client.gui.impl.AbstractVisualComponent;
import pro.gravit.launcher.client.ini4j.Wini;
import pro.gravit.launcher.profiles.ClientProfile;
import pro.gravit.utils.helper.LogHelper;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.logging.Logger;

public class ServerButtonComponent extends AbstractVisualComponent {
    private static final String SERVER_BUTTON_FXML = "components/serverButton.fxml";
    private static final String SERVER_BUTTON_CUSTOM_FXML = "components/serverButton/%s.fxml";
    public ClientProfile profile;
    public JSONObject categorys;
    private Button saveButton;
    private Button resetButton;
    private Pane blockon_online;
    private Pane blocko_offline;
    private boolean shop_online;

    public static int all_players = 0;

    protected ServerButtonComponent(JavaFXApplication application, ClientProfile profile, JSONObject categorys) {
        super(getFXMLPath(application, profile), application);
        this.profile = profile;
        this.categorys = categorys;
    }

    private static String getFXMLPath(JavaFXApplication application, ClientProfile profile) {
        String customFxmlName = String.format(SERVER_BUTTON_CUSTOM_FXML, profile.getUUID());
        URL customFxml = application.tryResource(customFxmlName);
        if (customFxml != null) {
            return customFxmlName;
        }
        return SERVER_BUTTON_FXML;
    }

    @Override
    public String getName() {
        return "serverButton";
    }

    @Override
    protected void doInit() throws Exception {
        shop_online = true;

        /*Получаем категорию товара*/
        int thisCategoryId = 0;
        String category_name = "";
        String categoryName = null;
        if (categorys != null) {
            JSONObject categorys_name = (JSONObject) categorys.get("category_list");
            JSONObject categorys_list = (JSONObject) categorys.get("servers");
            JSONObject thisCategory = (JSONObject) categorys_list.get(profile.getUUID().toString());
            if (thisCategory != null) {
                String serverCategory = (String) thisCategory.get("server_category");
                thisCategoryId = Integer.parseInt(serverCategory.toString());
            }
            category_name = (String) categorys_name.get(String.valueOf(thisCategoryId));
            if(category_name != ""){
                LookupHelper.<Labeled>lookup(layout, "#category_name").setText(category_name);
            }
        }

        LookupHelper.<Labeled>lookup(layout, "#nameServer").setText(profile.getTitle());
        LookupHelper.<Labeled>lookup(layout, "#genreServer").setText(profile.getVersion().toString());
        LookupHelper.<ImageView>lookupIfPossible(layout, "#serverLogo").ifPresent((a) -> {
            try {
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(a.getFitWidth(), a.getFitHeight());
                clip.setArcWidth(20.0);
                clip.setArcHeight(20.0);
                a.setClip(clip);
            } catch (Throwable e) {
                LogHelper.error(e);
            }
        });

        //Подгрузка избранных серверов
        Path serversFile = DirBridge.dir.resolve("servers.ini");
        Wini ini = null;
        try {
            ini = new Wini(new File(serversFile.toString()));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        Wini finalIni = ini;
        int favorite = finalIni.get("favoriteservers", profile.getUUID().toString(), int.class);
        if (favorite == 1) {
            LogHelper.info("ServerOn");
            LookupHelper.<ImageView>lookupIfPossible(layout, "#favorite").ifPresent((e) -> {
                e.setOpacity(1);
            });
        }

        //Анимация наводки
        LookupHelper.<Pane>lookup(layout, "#serverButtonLayout").setOnMouseExited((e) -> {
            new FadeOutServer(LookupHelper.<ImageView>lookup(layout, "#serverLogo")).play();
            LookupHelper.<ImageView>lookup(layout, "#serverLogo").setOpacity(0.29);
        });

        //Анимация наводки
        LookupHelper.<Pane>lookup(layout, "#serverButtonLayout").setOnMouseEntered((e) -> {
            new FadeInServer(LookupHelper.<ImageView>lookup(layout, "#serverLogo")).play();
            LookupHelper.<ImageView>lookup(layout, "#serverLogo").setOpacity(1);
        });

        //Добавление сервера в Избранные
        LookupHelper.<ImageView>lookupIfPossible(layout, "#favorite").ifPresent((e) -> {
            e.setOnMouseClicked((a) -> {
                if (e.getOpacity() < 0.7) {
                    new BounceIn(e).play();
                    new FadeInServer(e).play();
                    try {
                        addToFavorite(profile.getUUID().toString(), 1);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    new FadeOutServer(e).play();
                    try {
                        addToFavorite(profile.getUUID().toString(), 0);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }

            });
        });

        //Получение онлайна сервера
        application.pingService.getPingReport(profile.getDefaultServerProfile().name).thenAccept((report) -> {
            //report.playersOnline
            if (report == null) {
                if (shop_online) {
                    LookupHelper.<Pane>lookup(layout, "#block-offline").setVisible(true);
                    LogHelper.info("test");
                }
            } else {
                if (shop_online) {
                    LookupHelper.<Pane>lookup(layout, "#block-offline").setVisible(false);
                    LookupHelper.<Pane>lookup(layout, "#blockon_online").setVisible(true);
                    all_players += report.playersOnline;
                    LookupHelper.<Labeled>lookup(layout, "#online").setText(String.valueOf(report.playersOnline) + "/" + String.valueOf(report.maxPlayers));
                    new BounceIn(LookupHelper.<Labeled>lookup(layout, "#online")).play();
                }
            }
        });

        //Если онлайн не получен показать то, что сервер offline
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if (shop_online) {
                            if (!LookupHelper.<Pane>lookup(layout, "#blockon_online").isVisible()) {
                                LookupHelper.<Pane>lookup(layout, "#block-offline").setVisible(true);
                                new BounceIn(LookupHelper.<Pane>lookup(layout, "#block-offline")).play();
                                LogHelper.info(String.valueOf(all_players));
                            }
                        }
                    }
                }, 3000
        );

        blockon_online = LookupHelper.lookup(layout, "#block-online");
        blocko_offline = LookupHelper.lookup(layout, "#block-offline");
        saveButton = LookupHelper.lookup(layout, "#save");
        resetButton = LookupHelper.lookup(layout, "#reset");
    }

    public void setOnMouseClicked(EventHandler<? super MouseEvent> eventHandler) {
        LookupHelper.<Pane>lookup(layout, "#serverButtonLayout").setOnMouseClicked(eventHandler);
        //setOnMouseClicked(eventHandler);
    }

    //Устанавливаем FavoriteServer
    public void addToFavorite(String ServerUUID, int value) throws Exception  {
        Path serversFile = DirBridge.dir.resolve("servers.ini");
        try{
            Wini ini = new Wini(new File(serversFile.toString()));
            //int age = ini.get("owner", "age", int.class);
            ini.put("favoriteservers", ServerUUID, value);
            ini.store();
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    public void enableSaveButton(String text, EventHandler<ActionEvent> eventHandler) {
        saveButton.setVisible(true);
        if (text != null)
            saveButton.setText(text);
        saveButton.setOnAction(eventHandler);
    }

    //Отключить мониторинг
    public void disableonline() {
        shop_online = false;
        blockon_online.setVisible(false);
        blockon_online.setOpacity(0);
        blocko_offline.setVisible(false);
        blocko_offline.setOpacity(0);
    }

    //Отключить Картинку сервера
    public void disableserverbackground() {
        try {
            LookupHelper.<Pane>lookup(layout, "#serverButtonLayout").getChildren().forEach((node -> {
                if(node.getId() != null){
                    if(node.getId().toString().indexOf("save") != -1 ) {
                        LookupHelper.<Pane>lookup(layout, "#serverButton").setStyle("-fx-cursor: default;");
                        node.setStyle("-fx-background-radius: 10px 10px 10px 10px;-fx-font-size: 12px;");
                    }else{
                        LookupHelper.<Pane>lookup(layout, "#serverButton").setStyle("-fx-cursor: default;");
                        node.setVisible(false);
                    }
                }else{
                    LookupHelper.<Pane>lookup(layout, "#serverButton").setStyle("-fx-cursor: default;");
                    node.setVisible(false);
                }

            }));
        } catch (Exception e) {
            LogHelper.error(e);
        }

        LookupHelper.<ImageView>lookupIfPossible(layout, "#favorite").get().setVisible(false);
    }


    public void enableResetButton(String text, EventHandler<ActionEvent> eventHandler) {
        resetButton.setVisible(true);
        if (text != null)
            resetButton.setText(text);
        resetButton.setOnAction(eventHandler);
    }

    public void addTo(Pane pane) {
        if (!isInit()) {
            try {
                init();
            } catch (Exception e) {
                LogHelper.error(e);
            }
        }
        pane.getChildren().add(layout);
    }

    public void addTo(GridPane pane, int position, int position_x) {
        if (!isInit()) {
            try {
                init();
            } catch (Exception e) {
                LogHelper.error(e);
            }
        }
        pane.add(layout, position_x, position);
        new ZoomIn(layout).play();
        //pane.getChildren().add(position, layout);
    }

    @Override
    public void reset() {

    }

    @Override
    public void disable() {

    }

    @Override
    public void enable() {

    }
}
