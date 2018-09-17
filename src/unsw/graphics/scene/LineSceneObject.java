package unsw.graphics.scene;

import com.jogamp.opengl.GL3;
import java.awt.Color;
import unsw.graphics.CoordFrame2D;
import unsw.graphics.Shader;
import unsw.graphics.geometry.Line2D;
import unsw.graphics.geometry.Point2D;

public class LineSceneObject extends SceneObject{
  private Color myColor;
  Line2D myLine;
  //Create a LineSceneObject from (0,0) to (1,0)
  public LineSceneObject(SceneObject parent, Color lineColor) {
    this(parent, 0, 0, 1, 0, lineColor);
  }

  //Create a LineSceneObject from (x1,y1) to (x2,y2)
  public LineSceneObject(SceneObject parent, float x0, float y0, float x1, float y1, Color lineColor) {
    super(parent);
    myColor = lineColor;
    myLine = new Line2D(x0, y0, x1, y1);
  }

  @Override
  public void drawSelf(GL3 gl, CoordFrame2D frame) {
    Shader.setPenColor(gl, myColor);
    myLine.draw(gl, frame);
  }

}
