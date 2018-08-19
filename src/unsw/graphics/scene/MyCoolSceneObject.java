package unsw.graphics.scene;

import java.awt.Color;
import unsw.graphics.geometry.Line2D;
import unsw.graphics.geometry.Polygon2D;

/**
 * A cool scene object
 *
 */
public class MyCoolSceneObject extends SceneObject {

    public MyCoolSceneObject(SceneObject parent) {
        super(parent);
        Polygon2D body = new Polygon2D(0.3f, 0.5f, 0.3f, -0.5f, -0.3f, -0.5f, -0.3f, 0.5f);
        PolygonalSceneObject bodyObj = new PolygonalSceneObject(parent, body, Color.white, Color.black);
        LineSceneObject pantsLine = new LineSceneObject(bodyObj, 0f, -0.2f, 0f, -0.5f, Color.black);
        LineSceneObject belt = new LineSceneObject(bodyObj, -0.3f, 0.1f, 0.3f, 0.1f, Color.black);

        SceneObject glasses = new SceneObject(bodyObj);
        glasses.translate(0, 0.3f);
        glasses.scale(0.3f);
        LineSceneObject stick = new LineSceneObject(glasses, -0.1f, 0, 0.1f, 0, Color.black);
        Polygon2D glass = new Polygon2D(0.2f, 0.1f, 0.2f, -0.1f, -0.2f, -0.1f, -0.2f, 0.1f);
        PolygonalSceneObject leftGlassObj = new PolygonalSceneObject(glasses, glass, Color.white, Color.black);
        leftGlassObj.translate(-0.3f, 0);
        PolygonalSceneObject rightGlassObj = new PolygonalSceneObject(glasses, glass, Color.white, Color.black);
        rightGlassObj.translate(0.3f, 0);
    }



}
