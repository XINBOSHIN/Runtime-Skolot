package pro.gravit.launcher.client.animatefx.animation;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * @author Loïc Sculier aka typhon0
 */

public class FadeInServer extends AnimationFX {

    /**
     * Create a new FadeIn animation
     *
     * @param node the node to affect
     */
    public FadeInServer(Node node) {
        super(node);
    }

    public FadeInServer() {
    }

    @Override
    AnimationFX resetNode() {
        getNode().setOpacity(1);
        return this;
    }

    @Override
    void initTimeline() {
        setTimeline(new Timeline(
                new KeyFrame(Duration.millis(0),
                        new KeyValue(getNode().opacityProperty(), 0.5, AnimateFXInterpolator.EASE)
                ),
                new KeyFrame(Duration.millis(500),
                        new KeyValue(getNode().opacityProperty(), 0.8, AnimateFXInterpolator.EASE)
                )

        ));
    }
}

