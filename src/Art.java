import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import java.io.IOException;

public class Art {
    // Recursos gráficos para los nuevos sprites
    public static Image knightSpritesheet;
    public static Image enemySpritesheet;
    public static Image tileset;

    // Dimensiones de cada frame del sprite sheet (ajustado a tu pixel art)
    public static final int FRAME_WIDTH = 16;
    public static final int FRAME_HEIGHT = 16;

    // Método para cargar los archivos .png al iniciar la aplicación
    public static void loadResources() {
        try {
            // Nota la barra diagonal al inicio para buscar en la raíz del JAR / recursos
            knightSpritesheet = Image.createImage("/knight_spritesheet.png");
            enemySpritesheet  = Image.createImage("/enemy_spritesheet.png");
            tileset           = Image.createImage("/tileset.png");
        } catch (IOException e) {
            System.out.println("Error al cargar los nuevos sprites: " + e.getMessage());
        }
    }

    // Método optimizado para dibujar el frame deseado sin suavizado (pixel art nítido)
    public static void drawSprite(Graphics g, Image spritesheet, int x, int y, int frameCol, int frameRow) {
        if (spritesheet != null) {
            g.setClip(x, y, FRAME_WIDTH, FRAME_HEIGHT);
            g.drawImage(spritesheet, x - (frameCol * FRAME_WIDTH), y - (frameRow * FRAME_HEIGHT), Graphics.TOP | Graphics.LEFT);
            g.setClip(0, 0, g.getClipWidth(), g.getClipHeight());
        }
    }
}
