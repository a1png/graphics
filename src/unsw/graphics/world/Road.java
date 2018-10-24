package unsw.graphics.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import unsw.graphics.*;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

    private List<Point2D> points;
    private float width;
    private float height;
    private TriangleMesh roadMesh;
    private Texture roadTexture;


    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(float width, List<Point2D> spine, float height) {
        this.width = width;
        this.height = height;
        this.points = spine;
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return width;
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return points.size() / 3;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public Point2D controlPoint(int i) {
        return points.get(i);
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public Point2D point(float t) {
        int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 3;
        
        Point2D p0 = points.get(i++);
        Point2D p1 = points.get(i++);
        Point2D p2 = points.get(i++);
        Point2D p3 = points.get(i++);
        

        float x = b(0, t) * p0.getX() + b(1, t) * p1.getX() + b(2, t) * p2.getX() + b(3, t) * p3.getX();
        float y = b(0, t) * p0.getY() + b(1, t) * p1.getY() + b(2, t) * p2.getY() + b(3, t) * p3.getY();        
        
        return new Point2D(x, y);
    }
    
    /**
     * Calculate the Bezier coefficients
     * 
     * @param i
     * @param t
     * @return
     */
    private float b(int i, float t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;
            
        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }

    public void genMesh(GL3 gl) throws IOException {
        List<Point3D> vertices = getCurvePoints();
        List<Integer> indices = new ArrayList<>();
        List<Point2D> texCoords = new ArrayList<>();


        for (int i=0; i<vertices.size()-2; i+=2) {
            indices.add(i);
            indices.add(i+3);
            indices.add(i+2);

            indices.add(i);
            indices.add(i+1);
            indices.add(i+3);
        }

        for (int i=0; i<vertices.size(); i+=4) {
            texCoords.add(new Point2D(0, 1));
            texCoords.add(new Point2D(0, 0));
            texCoords.add(new Point2D(1, 1));
            texCoords.add(new Point2D(1, 0));
        }

        roadMesh = new TriangleMesh(vertices, indices, true, texCoords);
        roadMesh.init(gl);

        roadTexture = new Texture(gl, "res/textures/canLabel.bmp", "bmp", false);

    }

    public Vector3 getTangent(float t) {
        int i = (int)Math.floor(t);
        float x, y;
        t = t - i;

        i *= 3;

        Point2D p0 = points.get(i++);
        Point2D p1 = points.get(i++);
        Point2D p2 = points.get(i++);
        Point2D p3 = points.get(i++);

        x = 3 * (
                    (1-t) * (1-t) * (p1.getX() - p0.getX())
                    + 2 * t * (1-t) * (p2.getX() - p1.getX())
                    + t * t * (p3.getX() - p2.getX())
                );
        y = 3 * (
                    (1-t) * (1-t) * (p1.getY() - p0.getY())
                    + 2 * t * (1-t) * (p2.getY() - p1.getY())
                    + t * t * (p3.getY() - p2.getY())
                );
        return new Vector3(x, y, 0).normalize();
    }

    private List<Point3D> getCurvePoints() {
        Point2D spine;
        Vector3 tangent;
        float[] mat = new float[16];
        Matrix4 frenet;
        Point3D upper, lower;
        float[] L1 = {width/2, 0, 0, 1}, L2 = {-width/2, 0, 0, 1};
        Vector4 L1Vector = new Vector4(L1), L2Vector = new Vector4(L2);
        List<Point3D> vertices = new ArrayList<>();
        for (float t=0; t<this.size(); t+=0.01) {
            spine = point(t);
            tangent = getTangent(t);
            mat[0] = tangent.getY();
            mat[2] = -tangent.getX();
            mat[5] = this.height;
            mat[8] = tangent.getX();
            mat[10] = tangent.getY();
            mat[12] = spine.getX();
            mat[13] = this.height;
            mat[14] = spine.getY();
            mat[15] = 1;
            frenet = new Matrix4(mat);
            upper = frenet.multiply(L1Vector).asPoint3D();
            lower = frenet.multiply(L2Vector).asPoint3D();
            vertices.add(upper);
            vertices.add(lower);
        }
        return vertices;
    }

    public void drawRoad(GL3 gl, CoordFrame3D frame) {
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, roadTexture.getId());

        roadMesh.draw(gl, frame);
    }
}
