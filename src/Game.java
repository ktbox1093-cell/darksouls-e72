// src/Game.java
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;

public class Game extends GameCanvas implements Runnable {
  int px = 160, py = 120, ex = 40, ey = 40;
  int hp = 100, stamina = 100, ehp = 60;
  int rollT = 0, atkT = 0, dx = 0, dy = 1;
  boolean run = true;

  public Game() { super(true); }

  public void run() {
    Graphics g = getGraphics();
    while (run) {
      int k = getKeyStates();
      int sp = rollT > 0 ? 6 : 2;
      if (rollT == 0) { dx = 0; dy = 0; }
      if (rollT == 0) {
        if ((k & LEFT_PRESSED) != 0)  dx = -1;
        if ((k & RIGHT_PRESSED) != 0) dx = 1;
        if ((k & UP_PRESSED) != 0)    dy = -1;
        if ((k & DOWN_PRESSED) != 0)  dy = 1;
        if ((k & FIRE_PRESSED) != 0 && stamina >= 20 && atkT == 0) { atkT = 8; stamina -= 20; }
        if ((k & GAME_A_PRESSED) != 0 && stamina >= 30) { rollT = 8; stamina -= 30; }
      } else rollT--;
      if (atkT > 0) atkT--;
      px += dx * sp; py += dy * sp;
      // enemigo persigue
      if (ex < px) ex++; else ex--;
      if (ey < py) ey++; else ey--;
      int dist = Math.abs(ex - px) + Math.abs(ey - py);
      if (dist < 20 && rollT == 0 && hp > 0) hp--;
      if (atkT > 4 && dist < 30 && ehp > 0) ehp--;
      if (stamina < 100) stamina++;

      g.setColor(0x101010); g.fillRect(0, 0, 320, 240);
      g.setColor(0xCC3333); if (ehp > 0) g.fillRect(ex, ey, 14, 14);
      g.setColor(rollT > 0 ? 0x888888 : 0xDDDDDD); g.fillRect(px, py, 12, 12);
      if (atkT > 0) { g.setColor(0xFFFF00); g.fillRect(px + dx * 14, py + dy * 14, 10, 10); }
      g.setColor(0xAA0000); g.fillRect(5, 5, hp, 6);
      g.setColor(0x00AA00); g.fillRect(5, 14, stamina, 6);
      if (hp <= 0) { g.setColor(0xFFFFFF); g.drawString("YOU DIED", 120, 110, 0); }
      flushGraphics();
      try { Thread.sleep(40); } catch (Exception e) {}
    }
  }
}
