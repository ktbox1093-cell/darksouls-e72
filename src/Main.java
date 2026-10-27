import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;

public class Main extends MIDlet {
  Game g;
  protected void startApp() {
    if (g == null) { g = new Game(); Display.getDisplay(this).setCurrent(g); new Thread(g).start(); }
  }
  protected void pauseApp() {}
  protected void destroyApp(boolean u) { g.run = false; }
}
