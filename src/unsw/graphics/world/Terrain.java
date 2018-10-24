package unsw.graphics.world;



import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;


/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    private int width;
    private int depth;
    private float[][] altitudes;
    private List<Tree> trees;
    private List<Road> roads;
    private Vector3 sunlight;
    private List<Point3D> grids = new ArrayList<>();
    private TriangleMesh terrainMesh;
    private TriangleMesh treeMesh;
    private Texture terrainTexture;
    private Texture treeTexture;
    private float rotateY = 0;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth, Vector3 sunlight) {
        this.width = width;
        this.depth = depth;
        altitudes = new float[width][depth];
        trees = new ArrayList<Tree>();
        roads = new ArrayList<Road>();
        this.sunlight = sunlight;
    }

    public List<Tree> trees() {
        return trees;
    }

    public List<Road> roads() {
        return roads;
    }

    public Vector3 getSunlight() {
        return sunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        sunlight = new Vector3(dx, dy, dz);      
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return altitudes[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, float h) {
        altitudes[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     * 
     * @param x
     * @param z
     * @return
     *                           p0
     *                           |\
     *                           | \
     *                           |  \
     *                           |   \
     * (x-x0) * y2 + (x2-x) * y0 | p  \   (x-x1) * y2 + (x2-x) * y1
     *                           |     \
     *                           |      \
     *                           |       \
     *                           |        \
     *                        p2  ---------   p1
     */
    public float altitude(float x, float z) {
        float y0, y1, y2, inter_y_1, inter_y_2, inter_z_1, inter_z_2, altitude;
        int x0, z0, x1, z1, x2, z2;
        x0 = (int) Math.floor((double) x);
        z0 = (int) Math.floor((double) z);
        if (x0==x && z0==z) {
            return (float) getGridAltitude((int) x, (int) z);
        }
        if (x==x0) {
            altitude = (float) (getGridAltitude(x0, z0) + (getGridAltitude(x0, z0+1) - getGridAltitude(x0, z0)) * (z-z0));
            return altitude;
        } else if (z==z0) {
            altitude = (float) (getGridAltitude(x0, z0) + (getGridAltitude(x0+1, z0) - getGridAltitude(x0, z0)) * (x-x0));
            return altitude;
        }

        if ((x-x0) > (z-z0)) {
            x1 = (int) Math.ceil((double) x);
            x2 = x1;
            z1 = z0;
            z2 = (int) Math.ceil((double) z);
            inter_z_1 = z0;
            inter_z_2 = z0 + x % 1;
        } else {
            x1 = (int) Math.ceil((double) x);
            x2 = x0;
            z1 = (int) Math.ceil((double) z);
            z2 = z1;
            inter_z_1 = z0 + x % 1;
            inter_z_2 = z1;
        }

        y0 = altitudes[x0][z0];
        y1 = altitudes[x1][z1];
        y2 = altitudes[x2][z2];

        inter_y_1 = (x-x0) * y1 + (x1-x) * y0;
        inter_y_2 = (x-x0) * y1 + (x1-x) * y2;

        altitude = (z - inter_z_1) / (inter_z_2-inter_z_1) * inter_y_2 +
                (inter_z_2 - z) / (inter_z_2-inter_z_1) * inter_y_1;
        return altitude;
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(float x, float z) {
        float y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        trees.add(tree);
    }


    /**
     * Add a road. 
     * 
     //* @param x
     //* @param z
     */
    public void addRoad(float width, List<Point2D> spine) {
        Point2D start = spine.get(0);
        float roadHeight = altitude(start.getX(), start.getY()) + 0.01f;
        Road road = new Road(width, spine, roadHeight);
        roads.add(road);        
    }

    public void genMesh(GL3 gl) throws IOException{
        genGrids();
        List<Integer> indices = new ArrayList<>();
        List<Point2D> texCoords = new ArrayList<>();
        for (int i=0; i<depth-1; i++) {
            for (int j=0; j<width-1; j++) {
                indices.add(j+width*i);
                indices.add(j+width*(i+1)+1);
                indices.add(j+width*i+1);

                indices.add(j+width*i);
                indices.add(j+width*(i+1));
                indices.add(j+width*(i+1)+1);

            }
        }
        for (int i=0; i<width; i++) {
            for (int j=0; j<depth/2; j++){
                if (i%2==0) {
                    texCoords.add(new Point2D(0, 1));
                    texCoords.add(new Point2D(0, 0));
                } else {
                    texCoords.add(new Point2D(1, 1));
                    texCoords.add(new Point2D(1, 0));
                }
            }
        }

        terrainMesh = new TriangleMesh(grids, indices, true, texCoords);
        treeMesh = new TriangleMesh("res/models/tree.ply", true, true);
        for (Road road : roads) {
            road.genMesh(gl);
        }

        terrainMesh.init(gl);
        treeMesh.init(gl);

        terrainTexture = new Texture(gl, "res/textures/grass.bmp", "bmp", false);
        treeTexture = new Texture(gl, "res/textures/BrightPurpleMarble.png", "png", false);

    }


    private void genGrids() {
        Point3D point;
        for (int z=0; z<depth; z++) {
            for (int x=0; x<width; x++) {
                point = new Point3D(x, altitudes[x][z], z);
                grids.add(point);
            }
        }
    }

    public int getGridsWidth() { return this.width; }

    public int getGridsDepth() { return this.depth; }

    public void drawTerrain(GL3 gl, CoordFrame3D frame) {
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, terrainTexture.getId());

        CoordFrame3D terrainFrame = frame
                .translate(0, 0, 0);//.rotateX(rotateY);

        terrainMesh.draw(gl, terrainFrame);

        for (Road road : roads) {
            road.drawRoad(gl, terrainFrame);
        }

        gl.glBindTexture(GL.GL_TEXTURE_2D, treeTexture.getId());

        for(Tree tree : trees) {
            CoordFrame3D treeFrame = terrainFrame.
                    translate(tree.getPosition()).translate(0,2,0).scale(0.4f, 0.4f, 0.4f);
            // System.out.println(treeFrame.getMatrix());
            // treeFrame.draw(gl);
            treeMesh.draw(gl, treeFrame);
        }

        rotateY += 0.3;
    }


}
