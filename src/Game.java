import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;

public class Game extends GameCanvas implements Runnable {
  static final int T = 16, MW = 30, MH = 20, N = 16;
  static final String[] MAP = {
    "##############################",
    "#P...........#...............#",
    "#.F..........#.......e.......#",
    "#............#...............#",
    "#......e.....#######.#########",
    "#............................#",
    "#............................#",
    "#######.##########.###########",
    "#..........e.................#",
    "#............................#",
    "#..........#####.............#",
    "#..........#####.............#",
    "#............................#",
    "#..e.........................#",
    "######################.#######",
    "#............................#",
    "#.F..........................#",
    "#..................B.........#",
    "#............................#",
    "##############################"
  };

  public boolean run = true;
  int W, H;
  // jugador
  int px, py, fx = 0, fy = 1, hp = 100, maxHp = 100, st = 100, stDelay;
  int souls, level = 1, estus = 3, rollT, atkT, drinkT, hurtT, lock = -1;
  int rdx, rdy, rx, ry, deadT;
  boolean hitDone, dead, bossDead;
  boolean atkReq, rollReq, lockReq, estusReq;
  // mancha de sangre (almas perdidas)
  int bsx, bsy, bsSouls;
  // hogueras
  int[] bnx = new int[4], bny = new int[4];
  int nb;
  // enemigos
  int ne;
  int[] ex = new int[N], ey = new int[N], ehp = new int[N], emax = new int[N];
  int[] esx = new int[N], esy = new int[N], eh = new int[N];
  int[] eW = new int[N], eC = new int[N], eD = new int[N];
  int[] edx = new int[N], edy = new int[N], eMode = new int[N];
  boolean[] eBoss = new boolean[N];
  // mensajes
  String msg = ""; int msgT; boolean msgBig;

  public Game() {
    super(false);
    setFullScreenMode(true);
    W = getWidth(); H = getHeight();
    load();
    respawn();
  }

  void load() {
    for (int y = 0; y < MH; y++) {
      for (int x = 0; x < MAP[y].length(); x++) {
        char c = MAP[y].charAt(x);
        int cx = x * T + 8, cy = y * T + 8;
        if (c == 'P') { rx = cx; ry = cy; }
        else if (c == 'F' && nb < 4) { bnx[nb] = cx; bny[nb] = cy; nb++; }
        else if ((c == 'e' || c == 'B') && ne < N) {
          boolean b = (c == 'B');
          esx[ne] = cx; esy[ne] = cy; eBoss[ne] = b;
          eh[ne] = b ? 10 : 6; emax[ne] = b ? 300 : 40;
          ne++;
        }
      }
    }
  }

  void resetEnemies() {
    for (int i = 0; i < ne; i++) {
      ehp[i] = (eBoss[i] && bossDead) ? 0 : emax[i];
      ex[i] = esx[i]; ey[i] = esy[i];
      eW[i] = 0; eC[i] = 30; eD[i] = 0;
    }
  }

  void respawn() {
    px = rx; py = ry; hp = maxHp; st = 100; estus = 3;
    rollT = 0; atkT = 0; drinkT = 0; lock = -1; dead = false;
    resetEnemies();
  }

  void say(String s, boolean big, int t) { msg = s; msgBig = big; msgT = t; }

  // ---------- colisiones ----------
  boolean solid(int tx, int ty) {
    if (tx < 0 || ty < 0 || ty >= MH) return true;
    String r = MAP[ty];
    if (tx >= r.length()) return true;
    return r.charAt(tx) == '#';
  }

  boolean free(int x, int y, int h) {
    return !solid((x - h) / T, (y - h) / T) && !solid((x + h - 1) / T, (y - h) / T)
        && !solid((x - h) / T, (y + h - 1) / T) && !solid((x + h - 1) / T, (y + h - 1) / T);
  }

  void movePlayer(int dx, int dy) {
    if (free(px + dx, py, 6)) px += dx;
    if (free(px, py + dy, 6)) py += dy;
  }

  void moveEnemy(int i, int dx, int dy) {
    if (free(ex[i] + dx, ey[i], eh[i])) ex[i] += dx;
    if (free(ex[i], ey[i] + dy, eh[i])) ey[i] += dy;
  }

  int sg(int v) { return v > 0 ? 1 : (v < 0 ? -1 : 0); }
  boolean invul() { return rollT > 2; }

  // ---------- combate ----------
  void hurtPlayer(int d) {
    hp -= d; hurtT = 6;
    if (hp <= 0) {
      hp = 0; dead = true; deadT = 70;
      bsx = px; bsy = py; bsSouls = souls; souls = 0;
    }
  }

  void hurtEnemy(int i, int d) {
    ehp[i] -= d;
    if (!eBoss[i]) { eW[i] = 0; eC[i] = 20; }
    if (ehp[i] <= 0) {
      ehp[i] = 0;
      if (lock == i) lock = -1;
      if (eBoss[i]) {
        bossDead = true; souls += 1000;
        say("ENEMIGO ABATIDO", true, 90);
      } else souls += 60;
    }
  }

  int nearBonfire() {
    for (int i = 0; i < nb; i++)
      if (Math.abs(px - bnx[i]) + Math.abs(py - bny[i]) < 28) return i;
    return -1;
  }

  void rest(int b) {
    rx = bnx[b]; ry = bny[b];
    hp = maxHp; st = 100; estus = 3; lock = -1;
    resetEnemies();
    int up = 0;
    while (souls >= 100 + 40 * level) {
      souls -= 100 + 40 * level;
      level++; maxHp += 15; up++;
    }
    hp = maxHp;
    say(up > 0 ? "Subes a nivel " + level : "Descansas junto a la hoguera", false, 70);
  }

  void updateEnemies() {
    for (int i = 0; i < ne; i++) {
      if (ehp[i] <= 0) continue;
      int dx = px - ex[i], dy = py - ey[i];
      int d = Math.abs(dx) + Math.abs(dy);
      if (eC[i] > 0) eC[i]--;
      if (eD[i] > 0) { // embestida del jefe
        moveEnemy(i, edx[i] * 5, edy[i] * 5);
        eD[i]--;
        if (d < 14 + eh[i] && !invul() && !dead) { hurtPlayer(30); eD[i] = 0; }
        if (eD[i] == 0) eC[i] = 50;
        continue;
      }
      if (eW[i] > 0) { // preparando golpe
        eW[i]--;
        if (eW[i] == 0) {
          if (eMode[i] == 1) {
            edx[i] = sg(dx); edy[i] = sg(dy); eD[i] = 12;
          } else {
            if (d < 30 + eh[i] && !invul() && !dead) hurtPlayer(eBoss[i] ? 25 : 12);
            eC[i] = eBoss[i] ? 35 : 45;
          }
        }
        continue;
      }
      if (d < 140 && !dead) {
        if (eBoss[i] && d > 70 && eC[i] == 0) { eMode[i] = 1; eW[i] = 22; }
        else if (d < 24 + eh[i] && eC[i] == 0) { eMode[i] = 0; eW[i] = eBoss[i] ? 18 : 14; }
        else if (d > 18 + eh[i]) moveEnemy(i, sg(dx), sg(dy));
      }
    }
  }

  // ---------- jugador ----------
  void updatePlayer() {
    int k = getKeyStates();
    int mx = 0, my = 0;
    if ((k & LEFT_PRESSED) != 0) mx = -1;
    if ((k & RIGHT_PRESSED) != 0) mx = 1;
    if ((k & UP_PRESSED) != 0) my = -1;
    if ((k & DOWN_PRESSED) != 0) my = 1;

    if (lockReq) {
      if (lock >= 0) lock = -1;
      else {
        int best = 130;
        for (int i = 0; i < ne; i++) {
          if (ehp[i] <= 0) continue;
          int d = Math.abs(ex[i] - px) + Math.abs(ey[i] - py);
          if (d < best) { best = d; lock = i; }
        }
      }
    }
    if (lock >= 0 && (ehp[lock] <= 0 || Math.abs(ex[lock] - px) + Math.abs(ey[lock] - py) > 150)) lock = -1;

    if (rollT > 0) {
      movePlayer(rdx * 5, rdy * 5);
      rollT--;
    } else if (drinkT > 0) {
      drinkT--;
      movePlayer(mx, my);
    } else {
      movePlayer(mx * (atkT > 0 ? 1 : 2), my * (atkT > 0 ? 1 : 2));
      if ((mx != 0 || my != 0) && lock < 0) { fx = mx; fy = my; }
      if (rollReq && st >= 25) {
        rollT = 8; st -= 25; stDelay = 25; atkT = 0;
        rdx = mx; rdy = my;
        if (mx == 0 && my == 0) { rdx = -fx; rdy = -fy; }
      } else if (estusReq && estus > 0 && hp < maxHp) {
        hp = Math.min(maxHp, hp + 45); estus--; drinkT = 18;
      } else if (atkReq) {
        int b = nearBonfire();
        if (b >= 0) rest(b);
        else if (atkT == 0 && st >= 20) { atkT = 10; hitDone = false; st -= 20; stDelay = 25; }
      }
    }
    if (lock >= 0) {
      fx = sg(ex[lock] - px); fy = sg(ey[lock] - py);
      if (fx == 0 && fy == 0) fy = 1;
    }
    if (atkT > 0) {
      atkT--;
      if (atkT == 5 && !hitDone) {
        hitDone = true;
        for (int i = 0; i < ne; i++) {
          if (ehp[i] <= 0) continue;
          int dx = ex[i] - px, dy = ey[i] - py;
          if (Math.abs(dx) + Math.abs(dy) < 30 + eh[i] && dx * fx + dy * fy >= 0)
            hurtEnemy(i, 12 + level * 3);
        }
      }
    }
    if (stDelay > 0) stDelay--;
    else if (st < 100) st += 2;
    if (st > 100) st = 100;
    if (hurtT > 0) hurtT--;
    if (bsSouls > 0 && Math.abs(px - bsx) + Math.abs(py - bsy) < 20) {
      souls += bsSouls; bsSouls = 0;
      say("Almas recuperadas", false, 60);
    }
  }

  // ---------- teclas ----------
  protected void keyPressed(int kc) {
    int ga = 0;
    try { ga = getGameAction(kc); } catch (Exception e) {}
    int c = kc;
    if (c >= 'A' && c <= 'Z') c += 32;
    if (ga == FIRE) atkReq = true;
    else if (c == '1' || c == 'q' || ga == GAME_A) rollReq = true;
    else if (c == '3' || c == 'e' || ga == GAME_B) lockReq = true;
    else if (c == '7' || c == 'r' || ga == GAME_C) estusReq = true;
  }

  // ---------- dibujo ----------
  void draw(Graphics g) {
    int cx = Math.max(0, Math.min(px - W / 2, MW * T - W));
    int cy = Math.max(0, Math.min(py - H / 2, MH * T - H));
    g.setColor(0x000000); g.fillRect(0, 0, W, H);
    for (int y = cy / T; y <= (cy + H) / T; y++) {
      for (int x = cx / T; x <= (cx + W) / T; x++) {
        if (solid(x, y)) g.setColor(0x3A3A44);
        else g.setColor(((x + y) & 1) == 0 ? 0x1C1C20 : 0x18181C);
        g.fillRect(x * T - cx, y * T - cy, T, T);
      }
    }
    // hogueras
    for (int i = 0; i < nb; i++) {
      int sx = bnx[i] - cx, sy = bny[i] - cy;
      g.setColor(0x553311); g.fillRect(sx - 6, sy + 2, 12, 4);
      g.setColor(0xFF8800); g.fillArc(sx - 4, sy - 8, 8, 12, 0, 360);
      g.setColor(0xFFDD44); g.fillArc(sx - 2, sy - 4, 4, 7, 0, 360);
    }
    // mancha de sangre
    if (bsSouls > 0) {
      g.setColor(0x44AAFF); g.fillArc(bsx - cx - 5, bsy - cy - 5, 10, 10, 0, 360);
    }
    // enemigos
    for (int i = 0; i < ne; i++) {
      if (ehp[i] <= 0) continue;
      int h = eh[i], sx = ex[i] - cx, sy = ey[i] - cy;
      if (eD[i] > 0) g.setColor(0xFF6600);
      else if (eW[i] > 0) g.setColor(0xFFDD00);
      else g.setColor(eBoss[i] ? 0x8822AA : 0xBB3333);
      g.fillRect(sx - h, sy - h, 2 * h, 2 * h);
      if (!eBoss[i]) {
        g.setColor(0xFF0000); g.fillRect(sx - h, sy - h - 4, 2 * h * ehp[i] / emax[i], 2);
      }
      if (i == lock) { g.setColor(0xFFFFFF); g.drawRect(sx - h - 3, sy - h - 3, 2 * h + 6, 2 * h + 6); }
    }
    // jugador
    int sx = px - cx, sy = py - cy;
    if (rollT > 0) g.setColor(0x777777);
    else if (hurtT > 0) g.setColor(0xFF4444);
    else g.setColor(0xE0E0E0);
    g.fillRect(sx - 6, sy - 6, 12, 12);
    if (atkT > 0) {
      g.setColor(0xFFFF00);
      g.fillRect(sx + fx * 13 - 5, sy + fy * 13 - 5, 10, 10);
    }
    // HUD
    g.setColor(0x440000); g.fillRect(5, 5, maxHp, 6);
    g.setColor(0xCC0000); g.fillRect(5, 5, hp, 6);
    g.setColor(0x003300); g.fillRect(5, 14, 100, 5);
    g.setColor(0x00BB00); g.fillRect(5, 14, st, 5);
    g.setColor(0xFFFFFF);
    g.drawString("Almas " + souls, W - 5, 4, Graphics.TOP | Graphics.RIGHT);
    g.drawString("Estus " + estus + "  Nv " + level, W - 5, 18, Graphics.TOP | Graphics.RIGHT);
    if (nearBonfire() >= 0 && !dead)
      g.drawString("OK: descansar", W / 2, H - 20, Graphics.TOP | Graphics.HCENTER);
    // barra del jefe
    for (int i = 0; i < ne; i++) {
      if (eBoss[i] && ehp[i] > 0 && Math.abs(ex[i] - px) + Math.abs(ey[i] - py) < 180) {
        g.setColor(0x440000); g.fillRect(20, H - 12, W - 40, 6);
        g.setColor(0xCC0000); g.fillRect(20, H - 12, (W - 40) * ehp[i] / emax[i], 6);
        g.setColor(0xFFFFFF); g.drawString("GUARDIAN", W / 2, H - 28, Graphics.TOP | Graphics.HCENTER);
      }
    }
    Font big = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
    if (dead) {
      g.setColor(0x000000); g.fillRect(0, H / 2 - 22, W, 44);
      g.setFont(big); g.setColor(0xCC0000);
      g.drawString("HAS MUERTO", W / 2, H / 2 - 10, Graphics.TOP | Graphics.HCENTER);
      g.setFont(Font.getDefaultFont());
    } else if (msgT > 0) {
      if (msgBig) {
        g.setColor(0x000000); g.fillRect(0, H / 2 - 22, W, 44);
        g.setFont(big); g.setColor(0xDDBB44);
        g.drawString(msg, W / 2, H / 2 - 10, Graphics.TOP | Graphics.HCENTER);
        g.setFont(Font.getDefaultFont());
      } else {
        g.setColor(0xDDBB44);
        g.drawString(msg, W / 2, 40, Graphics.TOP | Graphics.HCENTER);
      }
    }
  }

  public void run() {
    Graphics g = getGraphics();
    while (run) {
      long t0 = System.currentTimeMillis();
      if (dead) {
        deadT--;
        if (deadT <= 0) respawn();
      } else {
        updatePlayer();
      }
      updateEnemies();
      atkReq = false; rollReq = false; lockReq = false; estusReq = false;
      if (msgT > 0) msgT--;
      draw(g);
      flushGraphics();
      long d = 40 - (System.currentTimeMillis() - t0);
      try { Thread.sleep(d > 5 ? d : 5); } catch (Exception e) {}
    }
  }
}
