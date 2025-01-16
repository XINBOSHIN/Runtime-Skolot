package pro.gravit.launcher.client.gui.scenes.servermenu;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import jdk.jpackage.internal.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import pro.gravit.launcher.client.DirBridge;
import pro.gravit.launcher.client.ServerPinger;
import pro.gravit.launcher.client.animatefx.animation.BounceIn;
import pro.gravit.launcher.client.animatefx.animation.FadeIn;
import pro.gravit.launcher.client.animatefx.animation.FadeOut;
import pro.gravit.launcher.client.gui.JavaFXApplication;
import pro.gravit.launcher.client.gui.helper.LookupHelper;
import pro.gravit.launcher.client.gui.scenes.AbstractScene;
import pro.gravit.launcher.client.gui.scenes.login.LoginScene;
import pro.gravit.launcher.client.ini4j.Wini;
import pro.gravit.launcher.profiles.ClientProfile;
import pro.gravit.utils.helper.CommonHelper;
import pro.gravit.utils.helper.LogHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class ServerMenuScene extends AbstractScene {
    private static final String SERVER_BUTTON_FXML = "components/serverButton.fxml";
    private static final String SERVER_BUTTON_CUSTOM_FXML = "components/serverButton/%s.fxml";
    private ImageView avatar;
    private HBox categorys;
    private List<ClientProfile> lastProfiles;
    private Image originalAvatarImage;

    private Label all_online;

    public ServerMenuScene(JavaFXApplication application) {
        super("scenes/servermenu/servermenu.fxml", application);
    }

    public JSONObject categorys_list = null;
    public JSONObject categorys_name = null;
    public JSONObject categorys_all = null;

    public ImageView slide_image = null;
    public Label nickname = null;

    private boolean isCodeExecuted = false;

    private static String JSON_URL = LoginScene.url+"/launcher/config.php?get=info"; // Укажите URL для JSON

    public static String getJSON_URL() {
        return JSON_URL;
    }

    @Override
    public void doInit() throws Exception {
        nickname = LookupHelper.lookup(layout, "#nickname");
        nickname.textProperty().setValue(application.stateService.getUsername());

        ServerButtonComponent.all_players = 0;
        avatar = LookupHelper.lookup(layout, "#avatar");
        categorys = LookupHelper.lookup(layout, "#categorys");
        ImageView slide_image = LookupHelper.lookup(layout, "#slide_image");
        HBox slider_contant = LookupHelper.lookup(layout, "#slider");
        originalAvatarImage = avatar.getImage();
        LookupHelper.<ImageView>lookupIfPossible(layout, "#avatar").ifPresent(
            (h) -> {
                try {
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(h.getFitWidth(), h.getFitHeight());
                    clip.setArcWidth(h.getFitWidth());
                    clip.setArcHeight(h.getFitHeight());
                    h.setClip(clip);
                    h.setImage(originalAvatarImage);
                } catch (Throwable e) {
                    LogHelper.warning("Skin head error");
                }
            }
        );

        //Создать ссылки переадерсации
        HBox link_to_site_1 = LookupHelper.<HBox>lookupIfPossible(layout, "#link_to_site_1").get();
        HBox link_to_site_2 = LookupHelper.<HBox>lookupIfPossible(layout, "#link_to_site_2").get();
        HBox link_to_site_3 = LookupHelper.<HBox>lookupIfPossible(layout, "#link_to_site_3").get();
        ImageView link_to_site_4 = LookupHelper.<ImageView>lookupIfPossible(layout, "#link_to_site_4").get();
        ImageView link_to_site_5 = LookupHelper.<ImageView>lookupIfPossible(layout, "#link_to_site_5").get();
        ImageView link_to_site_6 = LookupHelper.<ImageView>lookupIfPossible(layout, "#link_to_site_6").get();
        Label link_to_site_7 = LookupHelper.<Label>lookupIfPossible(layout, "#link_to_site_7").get();
        link_to_site_1.setOnMouseClicked((e)->{
            openWebpage(URI.create(LoginScene.url+"/launcher/api_get.php?redirect=link_to_site_1"));
        });
        link_to_site_2.setOnMouseClicked((e)->{
            openWebpage(URI.create(LoginScene.url+"/launcher/api_get.php?redirect=link_to_site_2"));
        });
        link_to_site_3.setOnMouseClicked((e)->{
            openWebpage(URI.create(LoginScene.url+"/launcher/api_get.php?redirect=link_to_site_3"));
        });
        link_to_site_4.setOnMouseClicked((e)->{
            openWebpage(URI.create(LoginScene.url+"/launcher/api_get.php?redirect=link_to_site_4"));
        });
        link_to_site_5.setOnMouseClicked((e)->{
            openWebpage(URI.create(LoginScene.url+"/launcher/api_get.php?redirect=link_to_site_5"));
        });
        link_to_site_6.setOnMouseClicked((e)->{
            openWebpage(URI.create(LoginScene.url+"/launcher/api_get.php?redirect=link_to_site_6"));
        });
        link_to_site_7.setOnMouseClicked((e)->{
            openWebpage(URI.create(LoginScene.url+"/launcher/api_get.php?redirect=link_to_site_7"));
        });

        ScrollPane scrollPane = LookupHelper.lookup(layout, "#servers");
        scrollPane.setOnScroll(e -> {
            double offset = e.getDeltaY() / scrollPane.getWidth();
            scrollPane.setHvalue(scrollPane.getHvalue() - offset);
        });

        //Открытие меню сверху
        LookupHelper.<HBox>lookupIfPossible(layout, "#get_options").ifPresent((e) -> {
            e.setOnMouseClicked((t) -> {
                LookupHelper.<Pane>lookupIfPossible(layout, "#menu_profile_top").ifPresent((f) -> {
                    if (f.isVisible()) {
                        new FadeOut(f).setResetOnFinished(true).play();
                        f.setVisible(false);
                    } else {
                        f.setVisible(true);
                        new FadeIn(f).play();
                    }
                });
            });
        });

        //Подгрузка всех категорий
        String jsonData = downloadJsonFromUrl(JSON_URL);
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
        categorys_list = (JSONObject) jsonObject.get("servers");
        categorys_name = (JSONObject) jsonObject.get("category_list");
        JSONObject sliders = (JSONObject) jsonObject.get("sliders");
        categorys_all = jsonObject;

        //Загружаем слайдеры
        Thread slidersThread = new Thread(() -> {
            try {
                List<Rectangle> sliders_buttons = new ArrayList<>();
                Platform.runLater(() -> {
                    for (Object slider : sliders.keySet()) {
                        JSONObject thisSlider = (JSONObject) sliders.get(slider);
                        String image_slider = (String) thisSlider.get("image");
                        String link_slider = (String) thisSlider.get("link");

                        Rectangle slide = new Rectangle();
                        slide.setFill(Paint.valueOf("#FFF"));
                        slide.setWidth(7);
                        slide.setHeight(7);
                        slide.setArcWidth(4);
                        slide.setArcHeight(4);
                        slide.setOpacity(0.3);
                        slide.cursorProperty().setValue(Cursor.HAND);
                        slide.getStyleClass().add("slide");
                        sliders_buttons.add(slide);

                        Image image = new Image(image_slider);

                        slide.setOnMouseClicked(event -> {
                            sliders_buttons.forEach(Rectangle -> {
                                Rectangle.getStyleClass().remove("slide_active");
                            });
                            slide.getStyleClass().add("slide_active");
                            slide_image.setImage(image);
                            slide_image.setUserData(link_slider);
                        });

                        if(slider.toString().indexOf("slide_2") != -1 ) {
                            slide.getStyleClass().add("slide_active");
                            slide_image.setImage(image);
                            slide_image.setUserData(link_slider);
                        }

                        slide_image.setOnMouseClicked(event -> {
                            openWebpage(URI.create(slide_image.getUserData().toString()));
                        });


                        slider_contant.getChildren().add(slide);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        slidersThread.start();

                //Список категорий сервера
        Thread downloadThread = new Thread(() -> {
            try {
                // Парсим JSON и создаем кнопки
                JSONObject categoryList = categorys_name;
                List<javafx.scene.control.Button> categorys_buttons = new ArrayList<>();

                Platform.runLater(() -> {
                    for (Object categoryId : categoryList.keySet()) {
                        String categoryName = (String) categoryList.get(categoryId);
                        javafx.scene.control.Button button = new javafx.scene.control.Button();
                        button.textProperty().setValue(categoryName);
                        button.getStyleClass().add("button_category");
                        categorys_buttons.add(button);

                        if(categoryName.toString().indexOf("Все") != -1 ) {
                            button.getStyleClass().add("button_category_active");
                        }
                        button.setOnMouseClicked(event -> {
                            categorys_buttons.forEach(Button -> {
                                Button.getStyleClass().remove("button_category_active");
                            });
                            button.getStyleClass().add("button_category_active");
                            //Все
                            if(button.getText().toString().indexOf("Все") != -1 ) {
                                UpdateServers(1, 0);
                                return;
                            }
                            //Избранные
                            if(button.getText().toString().indexOf("Избранные") != -1 ) {
                                UpdateServers(2, 0);
                                return;
                            }

                            UpdateServers(3, Integer.parseInt((String) categoryId));
                            return;
                        });
                        categorys.getChildren().add(button);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        downloadThread.start();

        //Подгрузка серверов
        reset();
        isResetOnShow = true;
    }

    public static String downloadJsonFromUrl(String url) throws Exception {
        StringBuilder result = new StringBuilder();
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } finally {
            connection.disconnect();
        }

        return result.toString();
    }

    static class ServerButtonCache {
        public ServerButtonComponent serverButton;
        public int position;
        public int position_x;

        public String uuid;
        public int favorite;

        public int category_id;
    }

    public static boolean putAvatarToImageView(JavaFXApplication application, String username, ImageView imageView) {
        int width = (int) imageView.getFitWidth();
        int height = (int) imageView.getFitHeight();
        Image head = application.skinManager.getScaledFxSkinHead(username, width, height);
        if (head == null) return false;
        imageView.setImage(head);
        return true;
    }

    public static ServerButtonComponent getServerButton(JavaFXApplication application, ClientProfile profile, JSONObject categorys) {
        return new ServerButtonComponent(application, profile, categorys);
    }

    @Override
    public void reset() {
        UpdateServers(1, 0);
        putAvatarToImageView(application, application.stateService.getUsername(), avatar);
    }

    //Обновить список серверов
    private void UpdateServers(int typeView, int category_id) {
        lastProfiles = application.stateService.getProfiles();
        Map<ClientProfile, ServerButtonCache> serverButtonCacheMap = new LinkedHashMap<>();
        List<ClientProfile> profiles = new ArrayList<>(lastProfiles);
        int position = 0;
        int position_x = 0;
        Path serversFile = DirBridge.dir.resolve("servers.ini");

        File favorite_file = new File(serversFile.toString());

        try {
            favorite_file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Wini ini = null;
        try {
            ini = new Wini(new File(serversFile.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //ini.put("favoriteservers", ServerUUID, value);

        for (ClientProfile profile : profiles) {

            JSONObject thisCategory = (JSONObject) categorys_list.get(profile.getUUID().toString());

            int thisCategoryId = 0;
            if(thisCategory != null){
                String serverCategory = (String) thisCategory.get("server_category");
                thisCategoryId = Integer.parseInt(serverCategory.toString());
            }

            int favorite = ini.get("favoriteservers", profile.getUUID().toString(), int.class);
            if(typeView == 2){
                if(favorite == 1) {
                    ServerButtonCache cache = new ServerButtonCache();
                    cache.serverButton = getServerButton(application, profile, categorys_all);
                    cache.position = position;
                    cache.favorite = favorite;
                    cache.position_x = position_x;
                    serverButtonCacheMap.put(profile, cache);
                    profile.updateOptionalGraph();
                    position_x++;
                    if (position_x == 4) {
                        position_x = 0;
                        position++;
                    }
                }
            }
            if(typeView == 1){
                ServerButtonComponent.all_players = 0;
                ServerButtonCache cache = new ServerButtonCache();
                cache.serverButton = getServerButton(application, profile, categorys_all);
                cache.position = position;
                cache.favorite = favorite;
                cache.position_x = position_x;
                serverButtonCacheMap.put(profile, cache);
                profile.updateOptionalGraph();
                position_x++;
                if (position_x == 4) {
                    position_x = 0;
                    position++;
                }
            }

            if(typeView == 3) {
                if(thisCategoryId == category_id){
                    ServerButtonCache cache = new ServerButtonCache();
                    cache.serverButton = getServerButton(application, profile, categorys_all);
                    cache.position = position;
                    cache.favorite = favorite;
                    cache.position_x = position_x;
                    serverButtonCacheMap.put(profile, cache);
                    profile.updateOptionalGraph();
                    position_x++;
                    if (position_x == 4) {
                        position_x = 0;
                        position++;
                    }
                }
            }

        }

        ScrollPane scrollPane = LookupHelper.lookup(layout, "#servers");

        GridPane serverList = (GridPane) scrollPane.getContent();
        scrollPane.setContent(serverList);
        serverList.getChildren().clear();
        application.pingService.clear();
        serverButtonCacheMap.forEach((profile, serverButtonCache) -> {
            EventHandler<? super MouseEvent> handle = (event) -> {
                if (!event.getButton().equals(MouseButton.PRIMARY))
                    return;
                changeServer(profile);
                try {
                    switchScene(application.gui.serverInfoScene);
                    application.gui.serverInfoScene.reset();
                } catch (Exception e) {
                    errorHandle(e);
                }
            };

            serverButtonCache.serverButton.addTo(serverList, serverButtonCache.position, serverButtonCache.position_x);
            serverButtonCache.serverButton.setOnMouseClicked(handle);

        });

        CommonHelper.newThread("ServerPinger", true, () -> {
            for (ClientProfile profile : lastProfiles) {
                ClientProfile.ServerProfile serverProfile = profile.getDefaultServerProfile();
                if (!serverProfile.socketPing || serverProfile.serverAddress == null) continue;
                try {
                    ServerPinger pinger = new ServerPinger(serverProfile, profile.getVersion());
                    ServerPinger.Result result = pinger.ping();
                    contextHelper.runInFxThread(() -> {
                        application.pingService.addReport(serverProfile.name, result);
                    });
                } catch (IOException ignored) {
                }
            }
        }).start();


        //Показать общий онлайн
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if (ServerButtonComponent.all_players > 0) {
                            if (!isCodeExecuted){
                                isCodeExecuted = true;
                                Platform.runLater(() -> {
                                    Label all_online = LookupHelper.lookup(layout, "#all_online");
                                    all_online.setText("Общий онлайн\n" + ServerButtonComponent.all_players + " игроков");
                                });
                            }
                        }
                    }
                }, 3000
        );


    }

    @Override
    public String getName() {
        return "serverMenu";
    }

    private void changeServer(ClientProfile profile) {
        application.stateService.setProfile(profile);
        application.runtimeSettings.lastProfile = profile.getUUID();
    }

    private class Button {
    }
}
