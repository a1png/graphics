package unsw.graphics.world;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

import java.io.IOException;

public class Avatar {
    private Point3D position;
    private TriangleMesh avatar;
    private Texture texture;

    public Avatar() {
        position = new Point3D(0, 0, 0);
    }

    public void init(GL3 gl) throws IOException {
        avatar = new TriangleMesh("res/models/bunny.ply", true, true);
        texture = new Texture(gl, "res/textures/BrightPurpleMarble.png", "png", false);

        avatar.init(gl);
    }

    public void drawAvatar(GL3 gl, CoordFrame3D frame) {
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());

        avatar.draw(gl, frame);
    }
}
