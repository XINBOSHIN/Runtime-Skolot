package pro.gravit.launcher.client.gui.dialogs;

import javafx.animation.TranslateTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import pro.gravit.launcher.client.animatefx.animation.FadeOut;
import pro.gravit.launcher.client.gui.JavaFXApplication;
import pro.gravit.launcher.client.gui.helper.LookupHelper;
import pro.gravit.launcher.client.gui.helper.PositionHelper;
import pro.gravit.utils.helper.LogHelper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

public class NotificationDialog extends AbstractDialog {
    public static class NotificationSlot {
        public final Consumer<Double> onScroll;
        public final double size;

        public NotificationSlot(Consumer<Double> onScroll, double size) {
            this.onScroll = onScroll;
            this.size = size;
        }
    }

    private static class NotificationSlotsInfo {
        private final LinkedList<NotificationSlot> stack = new LinkedList<>();

        double add(NotificationSlot slot) {
            double offset = 0;
            for (NotificationSlot slot1 : stack) {
                offset += slot1.size;
            }
            stack.add(slot);
            return offset;
        }

        void remove(NotificationSlot removeSlot) {
            boolean isFound = false;
            for (NotificationSlot slot : stack) {
                if (isFound) {
                    slot.onScroll.accept(removeSlot.size);
                    continue;
                }
                if (removeSlot == slot) {
                    isFound = true;
                }
            }
            stack.remove(removeSlot);
        }
    }

    private static final Map<PositionHelper.PositionInfo, NotificationSlotsInfo> slots = new HashMap<>();
    private String header;
    private String text;

    private Pane progress;
    private Text textHeader;
    private Text textDescription;
    private PositionHelper.PositionInfo positionInfo;
    private NotificationSlot positionSlot;
    private double positionOffset;

    public NotificationDialog(JavaFXApplication application, String header, String text) {
        super("components/notification.fxml", application);
        this.header = header;
        this.text = text;
    }

    @Override
    public String getName() {
        return "notify";
    }

    @Override
    protected void doInit() throws Exception {
        textHeader = LookupHelper.lookup(layout, "#notificationHeading");
        textDescription = LookupHelper.lookup(layout, "#notificationText");
        progress = LookupHelper.lookup(layout, "#progress1");
        layout.setOnMouseClicked((e) -> {
            try {
                close();
            } catch (Throwable throwable) {
                errorHandle(throwable);
            }
        });
        textHeader.setText(header);
        textDescription.setText(text);
        setOnClose(() -> {
            LogHelper.info("setOnClose();");
            if (positionSlot != null) {
                NotificationSlotsInfo slotsInfo = slots.get(positionInfo);
                slotsInfo.remove(positionSlot);
            }
        });

        //Автоматическое плавная полоса исчезновения
        try {
            TranslateTransition translateTransition = new TranslateTransition();
            translateTransition.setDuration(Duration.millis(3500));
            translateTransition.setNode(progress);
            translateTransition.setByX(327);
            translateTransition.setAutoReverse(true);
            translateTransition.play();
        } catch (Throwable throwable) {
            errorHandle(throwable);
        }
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        new FadeOut(layout).play();
                    }
                }, 3150
        );
        //Автоматическое исчезнование сообщений
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        layout.setVisible(false);
                        if (positionSlot != null) {
                            NotificationSlotsInfo slotsInfo = slots.get(positionInfo);
                            slotsInfo.remove(positionSlot);
                        }
                    }
                }, 3500
        );
    }

    @Override
    public void reset() {
        super.reset();
    }

    public void setPosition(PositionHelper.PositionInfo position, NotificationSlot positionSlot) {
        if (positionInfo != null) {
            NotificationSlotsInfo slotsInfo = slots.get(positionInfo);
            slotsInfo.remove(positionSlot);
        }
        this.positionInfo = position;
        LogHelper.info("Notification position: %s", position);
        if (position == null) return;
        NotificationSlotsInfo slotsInfo = slots.get(position);
        if (slotsInfo == null) {
            slotsInfo = new NotificationSlotsInfo();
            slots.put(position, slotsInfo);
        }
        this.positionSlot = positionSlot;
        this.positionOffset = slotsInfo.add(positionSlot);
    }

    public void setHeader(String header) {
        this.header = header;
        if (isInit())
            textHeader.setText(header);
    }

    public void setText(String text) {
        this.text = text;
        if (isInit())
            textDescription.setText(text);
    }

    @Override
    public LookupHelper.Point2D getOutSceneCoords(Rectangle2D bounds) {
        if (positionInfo == null) {
            LogHelper.info("Notification position: using central");
            return super.getOutSceneCoords(bounds);
        }
        return PositionHelper.calculate(positionInfo, layout.getPrefWidth(), layout.getPrefHeight(), 0, 30 + positionOffset, 320, bounds.getMaxY());
    }

    @Override
    public LookupHelper.Point2D getSceneCoords(Pane root) {
        if (positionInfo == null) return super.getSceneCoords(root);
        return PositionHelper.calculate(positionInfo, layout.getPrefWidth(), layout.getPrefHeight(), 0, 30 + positionOffset, 320, root.getPrefHeight());
    }

    @Override
    public void errorHandle(Throwable e) {
        // No Stack Overflow
        LogHelper.error(e);
    }

    private class ActionEvent {

        public final Object ACTION = null;
    }
}
