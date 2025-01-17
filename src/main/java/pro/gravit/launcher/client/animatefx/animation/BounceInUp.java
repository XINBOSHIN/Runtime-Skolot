package pro.gravit.launcher.client.animatefx.animation;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * @author Loïc Sculier aka typhon0
 */
public class BounceInUp extends AnimationFX{

    /**
     * Create new BounceInUp
     *
     * @param node The node to affect
     */
    public BounceInUp(Node node) {
        super(node);
    }

    public BounceInUp() {
    }

    @Override
    AnimationFX resetNode() {
        getNode().setOpacity(1);
        getNode().setTranslateY(0);
        return this;
    }

    @Override
    void initTimeline() {
        double startY = getNode().getScene().getHeight() - getNode().localToScene(0, 0).getY();
        setTimeline( new Timeline(
                        new KeyFrame(Duration.millis(0),
                                new KeyValue(getNode().opacityProperty(), 0, Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000)),
                                new KeyValue(getNode().translateYProperty(), startY, Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000))
                        ),
                        new KeyFrame(Duration.millis(600),
                                new KeyValue(getNode().opacityProperty(), 1, Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000)),
                                new KeyValue(getNode().translateYProperty(), -20, Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000))
                        ),
                        new KeyFrame(Duration.millis(750),
                                new KeyValue(getNode().translateYProperty(), 10, Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000))
                        ),
                        new KeyFrame(Duration.millis(900),
                                new KeyValue(getNode().translateYProperty(), -5, Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000))
                        ),
                        new KeyFrame(Duration.millis(1000),
                                new KeyValue(getNode().translateYProperty(), 0, Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000))
                        )

                ));
    }

   
}

