package unsw.graphics.world;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jogamp.opengl.GL3;

import unsw.graphics.*;
import unsw.graphics.geometry.Point3D;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class World extends Application3D {

    private Terrain terrain;
    private Shader shader;

    public World(Terrain terrain) {
    	super("Assignment 2", 800, 600);
        this.terrain = terrain;
    }
   
    /**
     * Load a level file and display it.
     * 
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        World world = new World(terrain);
        world.start();
    }

	@Override
	public void display(GL3 gl) {
    	super.display(gl);
        CoordFrame3D frame = CoordFrame3D.identity();

        CoordFrame3D viewFrame = CoordFrame3D.identity()
                .translate(-terrain.getGridsWidth()/2, 0, -15)
                .rotateX(-10);
        Shader.setViewMatrix(gl, viewFrame.getMatrix());

        frame.draw(gl);
        Shader.setPenColor(gl, Color.white);
		terrain.drawTerrain(gl, frame);

	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);
		
	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);
		shader = new Shader(gl, "shaders/vertex_tex_3d.glsl",
                "shaders/fragment_tex_3d.glsl");
        shader.use(gl);

        try {
            terrain.genMesh(gl);
        } catch (IOException e) {
            System.out.println("file not exists");
        }

    }

	@Override
	public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 1, 100));
	}
}
