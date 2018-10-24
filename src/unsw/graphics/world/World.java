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
    private Avatar avatar;
    private float viewDis;
    private float cameraPosX = 0;
    private float cameraPosY = 0;
    private float cameraPosZ = 0;
    private float cameraRotateX = 0;
    private float cameraRotateY = 0;
    private float cameraRotateZ = 0;
    private float avatarPosX, avatarPosY, avatarPosZ;
    private int cameraStatus = 0; // 0: first person; 1: third person
    private float rotateY = 0;
    private double rotateYRad;
    private int daytime = 0; // 0: day; 1:night
    private Color dayLight = new Color(0.5f, 0.5f, 0.5f);
    private Color nightLight = new Color(0.01f, 0.01f, 0.01f);
    private Color lightColor = dayLight;


    public World(Terrain terrain) {
    	super("Assignment 2", 800, 600);
        this.terrain = terrain;
        this.avatar = new Avatar();
        avatarPosZ = terrain.getGridsDepth()-1;
//        viewPointX = 0;
//        viewPointZ = terrain.getGridsDepth();
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
        CoordFrame3D viewFrame;

        viewDis = terrain.getGridsDepth()+2;
        viewFrame = frame
                .translate(0,0, -viewDis)
                .rotateX(cameraRotateX)
                .rotateY(rotateY)
                .translate(-avatarPosX, -2 - avatarPosY, -avatarPosZ)
                ;

        Point3D torchDir = new Vector3((float) (1*Math.sin(rotateYRad)), 0 ,(float) (1*Math.cos(rotateYRad))).asPoint3D();
        //viewFrame.draw(gl);
        Shader.setViewMatrix(gl, viewFrame.getMatrix());
        Shader.setInt(gl, "lightType", daytime);
        Shader.setPoint3D(gl, "torchPos", new Point3D(avatarPosX, avatarPosY, avatarPosZ));
        Shader.setPoint3D(gl, "torchDir", torchDir);
        Shader.setPoint3D(gl, "lightVec", terrain.getSunlight().extend().asPoint3D());
        Shader.setColor(gl, "lightIntensity", Color.WHITE);
        Shader.setColor(gl, "ambientIntensity", lightColor);
        Shader.setColor(gl, "torchIntensity", new Color(0.5f, 0.5f, 0.5f));

        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", Color.WHITE);
        Shader.setColor(gl, "diffuseCoeff", new Color(0.5f, 0.5f, 0.5f));
        Shader.setColor(gl, "specularCoeff", new Color(0.5f, 0.5f, 0.5f));
        Shader.setFloat(gl, "phongExp", 16f);

        // frame.draw(gl);
        Shader.setPenColor(gl, Color.white);
		terrain.drawTerrain(gl, frame);

        Shader.setPenColor(gl, Color.white);
        if (cameraStatus==1) {
            //CoordFrame3D avatarFrame = frame.translate(-cameraPosX, cameraPosY, terrain.getGridsDepth()-cameraPosZ)
            CoordFrame3D avatarFrame = frame
                    .translate(avatarPosX, avatarPosY, avatarPosZ)
                    .rotateY(180)
                    .scale(5,5,5)
                    ;
            avatar.drawAvatar(gl, avatarFrame);
            //avatarFrame.draw(gl);
        }
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

        try {
            terrain.genMesh(gl);
            avatar.init(gl);
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
        rotateYRad = Math.toRadians(rotateY);
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                cameraPosX += 0.4 * Math.sin(rotateYRad);
                cameraPosZ -= 0.4 * Math.cos(rotateYRad);
                avatarPosX += 0.4 * Math.sin(rotateYRad);
                avatarPosZ -= 0.4 * Math.cos(rotateYRad);
                break;
            case KeyEvent.VK_DOWN:
                cameraPosX -= 0.4 * Math.sin(rotateYRad);
                cameraPosZ += 0.4 * Math.cos(rotateYRad);
                avatarPosX -= 0.4 * Math.sin(rotateYRad);
                avatarPosZ += 0.4 * Math.cos(rotateYRad);
                break;
            case KeyEvent.VK_LEFT:
                rotateY = MathUtil.normaliseAngle(rotateY - 5);
                rotateYRad = Math.toRadians(rotateY);
                //avatarPosX -= 3 * Math.sin(rotateYRad);
                //avatarPosZ -= 3 * (1 - Math.cos(rotateYRad));

                break;
            case KeyEvent.VK_RIGHT:
                rotateY = MathUtil.normaliseAngle(rotateY + 5);
                rotateYRad = Math.toRadians(rotateY);
                //avatarPosX += (3) * Math.sin(rotateYRad);
                //avatarPosZ -= (3) * (1-Math.cos(rotateYRad));
                break;
            case KeyEvent.VK_SPACE:
                if (cameraStatus==0) {
                    cameraStatus = 1;
                    cameraRotateX = 20;
                } else if (cameraStatus==1) {
                    cameraStatus = 0;
                    cameraRotateX = 0;
                }
                break;
            case KeyEvent.VK_C:
                if (daytime == 0) {
                    daytime = 1;
                    lightColor = nightLight;
                } else {
                    daytime = 0;
                    lightColor = dayLight;
                }
                break;
            default:
                break;
        }
        if (0 <= avatarPosX && avatarPosX <= terrain.getGridsWidth() - 1
                && 0 <= avatarPosZ && avatarPosZ <= terrain.getGridsDepth() - 1) {
            avatarPosY = terrain.altitude(avatarPosX, avatarPosZ);
        } else {
            avatarPosY = 0;
        }

    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }
}
