package unsw.graphics.scene;

import com.jogamp.opengl.GL3;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import unsw.graphics.CoordFrame2D;
import unsw.graphics.Shader;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Polygon2D;

public class CircularSceneObject extends PolygonalSceneObject {

  private static final int VERTICES = 32;
  private float myRadius;
  private Polygon2D myCircular;
  private Color myFillColor;
  private Color myLineColor;

  //Create a CircularSceneObject with centre 0,0 and radius 1
  public CircularSceneObject(SceneObject parent, Color fillColor, Color lineColor) {
    this(parent, 1f, fillColor, lineColor);
  }

  //Create a CircularSceneObject with centre 0,0 and a given radius
  public CircularSceneObject(SceneObject parent, float radius, Color fillColor, Color lineColor){
    super(parent, null, fillColor, lineColor);
    myFillColor = fillColor;
    myLineColor = lineColor;
    myRadius = radius;
    myCircular = getCircular();
  }

  public Polygon2D getCircular() {
    List<Point2D> points = new ArrayList<Point2D>();
      for (int i = 0; i < VERTICES; i++) {
        float a = (float) (i * Math.PI * 2 / VERTICES); // java.util.Math uses radians!!!
        float x = myRadius * (float) Math.cos(a);
        float y = myRadius * ((float) Math.sin(a)); // Off center
        Point2D p = new Point2D(x, y);
        points.add(p);
      }
    return new Polygon2D(points);
  }

  @Override
  public void drawSelf(GL3 gl, CoordFrame2D frame) {
    if (myFillColor != null) {
      Shader.setPenColor(gl, myFillColor);
      myCircular.draw(gl, frame);
    }
    Shader.setPenColor(gl, myLineColor);
    myCircular.drawOutline(gl, frame);
  }

}
