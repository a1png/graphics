package unsw.graphics.world;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL3;

import unsw.graphics.*;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.scene.MathUtil;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class World extends Application3D implements KeyListener {

    private Terrain terrain;
    private Shader shader;
    private float cameraPosX = 0;
    private float cameraPosY = 0;
    private float cameraPosZ = 0;
    private float cameraDis = 5;
    private float viewPointX;
    private float viewPointZ;
    private float rotateY = 0;

    public World(Terrain terrain) {
    	super("Assignment 2", 800, 600);
        this.terrain = terrain;
        viewPointX = 0;
        viewPointZ = terrain.getGridsDepth();
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

        CoordFrame3D viewFrame = frame
                .rotateY(rotateY)
                .translate(cameraPosX, -2 - cameraPosY, -cameraDis+cameraPosZ)
                ;
        Shader.setViewMatrix(gl, viewFrame.getMatrix());

        Shader.setPoint3D(gl, "lightVec", terrain.getSunlight().asPoint3D());
        Shader.setColor(gl, "lightIntensity", Color.WHITE);
        Shader.setColor(gl, "ambientIntensity", new Color(0.2f, 0.2f, 0.2f));

        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", Color.WHITE);
        Shader.setColor(gl, "diffuseCoeff", new Color(0.5f, 0.5f, 0.5f));
        Shader.setColor(gl, "specularCoeff", new Color(0.5f, 0.5f, 0.5f));
        Shader.setFloat(gl, "phongExp", 16f);

        // frame.draw(gl);
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
        getWindow().addKeyListener(this);

		shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
                //"shaders/fragment_tex_phong.glsl");
                        "shaders/fragment_tex_phong_directional.glsl");
        shader.use(gl);
        cameraDis = terrain.getGridsDepth()+5;
//        viewPointX = 0;
//        viewPointZ = terrain.getGridsDepth()-1;
//        cameraPosY = terrain.altitude(viewPointX, viewPointZ);

        try {
            terrain.genMesh(gl);
        } catch (IOException e) {
            System.out.println("PLY file not exists");
        }

    }

	@Override
	public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 1, 100));
	}

    @Override
    public void keyPressed(KeyEvent e) {
        double rotateYRad = Math.toRadians(rotateY);
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                cameraPosX -= 0.4 * Math.sin(rotateYRad);
                cameraPosZ += 0.4 * Math.cos(rotateYRad);
                viewPointX += 0.4 * Math.sin(rotateYRad);
                viewPointZ -= 0.4 * Math.cos(rotateYRad);
                break;
            case KeyEvent.VK_DOWN:
                cameraPosX += 0.4 * Math.sin(rotateYRad);
                cameraPosZ -= 0.4 * Math.cos(rotateYRad);
                viewPointX -= 0.4 * Math.sin(rotateYRad);
                viewPointZ += 0.4 * Math.cos(rotateYRad);
                break;
            case KeyEvent.VK_LEFT:
                rotateY = MathUtil.normaliseAngle(rotateY - 5);
                rotateYRad = Math.toRadians(rotateY);
                viewPointX -= cameraDis * Math.sin(rotateYRad);
                viewPointZ -= cameraDis * (1-Math.cos(rotateYRad));
                break;
            case KeyEvent.VK_RIGHT:
                rotateY = MathUtil.normaliseAngle(rotateY + 5);
                rotateYRad = Math.toRadians(rotateY);
                viewPointX += cameraDis * Math.sin(rotateYRad);
                viewPointZ -= cameraDis * (1-Math.cos(rotateYRad));
                break;
            default:
                break;
        }
//        if (0 <= viewPointX && viewPointX < terrain.getGridsWidth() - 1
//                && 0 <= viewPointZ && viewPointZ < terrain.getGridsDepth() - 1) {
//            cameraPosY = terrain.altitude(viewPointX, viewPointZ);
//        } else {
//            cameraPosY = 0;
//        }
//        System.out.println(String.format("%f, %f, %f, %f, %f, %f", cameraPosX, cameraPosY, cameraPosZ, viewPointX, viewPointZ, rotateY));

    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }
}
