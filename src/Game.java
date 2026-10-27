import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.rms.RecordStore;
import java.io.*;

public class Game extends GameCanvas implements Runnable {
  static final int T = 16, MW = 30, MH = 20, N = 16, NA = 8;
  static Display disp;

  static final String[] M0 = {
    "##############################",
    "#P...........#...............#",
    "#.F..........#.......a.......#",
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
    "#..........h...............Q.N",
    "#............................#",
    "##############################"
  };
  static final String[] M1 = {
    "##############################",
    "V.P.........a.........a......#",
    "#............................#",
    "#.......e.........e..........#",
    "#..#######################..##",
    "#..#######################..##",
    "#GG#######################..##",
    "#..#######################..##",
    "#..#######################..##",
    "#............................#",
    "#.......##........##...a.....#",
    "#.L.....##...h....##.........#",
    "#.......................F....#",
    "#.....e........e.............#",
    "##############..##############",
    "#............................#",
    "#............................#",
    "#...................B........#",
    "#............................#",
    "##############################"
  };
  static final String[][] MAPS = { M0, M1 };
  // tipos: 0 hueco, 1 arquero, 2 pesado, 3 jefe
  static final int[] EH = { 6, 6, 8, 10 };
  static final int[] EHP = { 40, 25, 110, 350 };
  static final int[] ESOUL = { 60, 70, 120, 1000 };

  public boolean run = true;
  int W, H, tick;
  String[] map;
  int cur, rMap;
  boolean[] bossDead = new boolean[2], gate = new boolean[2];
  // estado
  boolean title = true;
  int menuSel, startReq;
  boolean hasSave;
  // jugador
  int px, py, fx = 1, fy = 0, hp = 100, maxHp = 100, st = 100, stDelay;
  int souls, level = 1, estus = 3, rollT, atkT, drinkT, hurtT, stunT, lock = -1;
  int rdx, rdy, rx, ry, deadT;
  boolean hitDone, dead, blockHeld;
  boolean atkReq, rollReq, lockReq, estusReq;
  // mancha de sangre
  int bsx, bsy, bsSouls, bsMap;
  // mapa actual
  int startX, startY, qX, qY, lvx, lvy;
  boolean hasLever;
  int[] bnx = new int[4], bny = new int[4];
  int nb;
  // enemigos
  int ne;
  int[] ex = new int[N], ey = new int[N], ehp = new int[N], emax = new int[N];
  int[] esx = new int[N], esy = new int[N], eh = new int[N], eT = new int[N];
  int[] eW = new int[N], eC = new int[N], eD = new int[N];
  int[] edx = new int[N], edy = new int[N], eMode = new int[N];
  // flechas
  int na;
  int[] arX = new int[NA], arY = new int[NA], arVX = new int[NA], arVY = new int[NA], arL = new int[NA];
  // mensajes
  String msg = ""; int msgT; boolean msgBig;
  // sprites
  Image[] sPl = new Image[2], sAr = new Image[2];
  Image sHo, sHe, sBo;

  public Game() {
    super(false);
    setFullScreenMode(true);
    W = getWidth(); H = getHeight();
    buildSprites();
    newGame();
    hasSave = saveExists();
  }

  // ---------- sprites ----------
  int pal(char c) {
    switch (c) {
      case 'h': return 0xFFC0C0C8;
      case 'v': return 0xFF18181C;
      case 'a': return 0xFF8890A0;
      case 'b': return 0xFF7A2A2A;
      case 'w': return 0xFFEEEEF4;
      case 'k': return 0xFF3A2A1A;
      case 'g': return 0xFF6E8B5A;
      case 'r': return 0xFFFF2020;
      case 'p': return 0xFF7A2AA0;
      case 'q': return 0xFF4A1A66;
      case 'y': return 0xFFE0C040;
      case 'n': return 0xFF2E6A2E;
      case 'o': return 0xFF8B5A2B;
      case 'd': return 0xFF6A6A72;
      default: return 0;
    }
  }

  Image mk(String[] rows, int sc, boolean mir) {
    int w = rows[0].length(), h = rows.length;
    int ww = w * sc, hh = h * sc;
    int[] data = new int[ww * hh];
    for (int y = 0; y < hh; y++) {
      for (int x = 0; x < ww; x++) {
        int sx = x / sc;
        if (mir) sx = w - 1 - sx;
        data[y * ww + x] = pal(rows[y / sc].charAt(sx));
      }
    }
    return Image.createRGBImage(data, ww, hh, true);
  }

  void buildSprites() {
    String[] pl = {
      "...hhhh.....",
      "..hhhhhh....",
      "..hhvvhh....",
      "..hhhhhh..w.",
      "...aaaa...w.",
      ".aaabbaaa.w.",
      ".aaabbaaaaw.",
      "..aabbbaa.w.",
      "...abbba..y.",
      "...bb.bb....",
      "...bb.bb....",
      "..kkk.kkk..."
    };
    String[] ho = {
      "....gggg....",
      "...gggggg...",
      "...grggrg...",
      "...gggggg...",
      "....gddg....",
      "..dgggggdd..",
      ".dd.gggg.dd.",
      ".d..gddg..d.",
      "....gddg....",
      "....g..g....",
      "...dg..gd...",
      "...dd..dd..."
    };
    String[] ar = {
      "....nnnn....",
      "...nnnnnn..o",
      "...nvvvvn.o.",
      "...nnnnnnoo.",
      "..nnnnnnn.oo",
      ".nnnnnnnn.o.",
      ".nn.nnnn..o.",
      "....nnnn..o.",
      "....nnnn..oo",
      "....n..n...o",
      "....n..n....",
      "...kk..kk..."
    };
    String[] he = {
      "..dddd..",
      ".dddddd.",
      ".drddrd.",
      ".dddddd.",
      "dddaaddd",
      "dd.aa.dd",
      ".dd..dd.",
      ".kk..kk."
    };
    String[] bo = {
      "p........p",
      "pp.qqqq.pp",
      ".pqqqqqqp.",
      ".pqrqqrqp.",
      "..qqqqqq..",
      ".ppyyyypp.",
      "pppyyyyppp",
      ".pp.pp.pp.",
      "..pq..qp..",
      "..kk..kk.."
    };
    sPl[0] = mk(pl, 1, false); sPl[1] = mk(pl, 1, true);
    sAr[0] = mk(ar, 1, false); sAr[1] = mk(ar, 1, true);
    sHo = mk(ho, 1, false);
    sHe = mk(he, 2, false);
    sBo = mk(bo, 2, false);
  }

  // ---------- partida ----------
  void newGame() {
    level = 1; maxHp = 100; souls = 0; bsSouls = 0;
    bossDead[0] = false; bossDead[1] = false;
    gate[0] = false; gate[1] = false;
    loadMap(0);
    rMap = 0; rx = startX; ry = startY;
    respawn();
  }

  void loadMap(int m) {
    cur = m; map = MAPS[m];
    nb = 0; ne = 0; hasLever = false; na = 0;
    startX = 24; startY = 24; qX = 24; qY = 24;
    for (int y = 0; y < MH; y++) {
      for (int x = 0; x < map[y].length(); x++) {
        char c = map[y].charAt(x);
        int cx = x * T + 8, cy = y * T + 8;
        if (c == 'P') { startX = cx; startY = cy; }
        else if (c == 'Q') { qX = cx; qY = cy; }
        else if (c == 'F' && nb < 4) { bnx[nb] = cx; bny[nb] = cy; nb++; }
        else if (c == 'L') { lvx = cx; lvy = cy; hasLever = true; }
        else {
          int t = -1;
          if (c == 'e') t = 0;
          else if (c == 'a') t = 1;
          else if (c == 'h') t = 2;
          else if (c == 'B') t = 3;
          if (t >= 0 && ne < N) {
            esx[ne] = cx; esy[ne] = cy; eT[ne] = t; eh[ne] = EH[t]; emax[ne] = EHP[t];
            ne++;
          }
        }
      }
    }
  }

  void enterMap(int m, boolean fromNext) {
    loadMap(m);
    px = fromNext ? qX : startX;
    py = fromNext ? qY : startY;
    lock = -1;
    resetEnemies();
  }

  void resetEnemies() {
    for (int i = 0; i < ne; i++) {
      ehp[i] = (eT[i] == 3 && bossDead[cur]) ? 0 : emax[i];
      ex[i] = esx[i]; ey[i] = esy[i];
      eW[i] = 0; eC[i] = 30; eD[i] = 0;
    }
    na = 0;
  }

  void respawn() {
    if (cur != rMap) loadMap(rMap);
    px = rx; py = ry; hp = maxHp; st = 100; estus = 3;
    rollT = 0; atkT = 0; drinkT = 0; stunT = 0; lock = -1; dead = false;
    resetEnemies();
  }

  void say(String s, boolean big, int t) { msg = s; msgBig = big; msgT = t; }

  void buzz(int ms) {
    try { if (disp != null) disp.vibrate(ms); } catch (Exception e) {}
  }

  // ---------- guardado ----------
  boolean saveExists() {
    try {
      RecordStore rs = RecordStore.openRecordStore("soulse72", false);
      int n = rs.getNumRecords();
      rs.closeRecordStore();
      return n > 0;
    } catch (Exception e) { return false; }
  }

  void deleteSave() {
    try { RecordStore.deleteRecordStore("soulse72"); } catch (Exception e) {}
  }

  void save() {
    try {
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      DataOutputStream d = new DataOutputStream(bo);
      d.writeInt(1);
      d.writeInt(level); d.writeInt(maxHp); d.writeInt(souls);
      d.writeInt(rMap); d.writeInt(rx); d.writeInt(ry);
      d.writeInt((bossDead[0] ? 1 : 0) | (bossDead[1] ? 2 : 0));
      d.writeInt((gate[0] ? 1 : 0) | (gate[1] ? 2 : 0));
      d.flush();
      byte[] b = bo.toByteArray();
      RecordStore rs = RecordStore.openRecordStore("soulse72", true);
      if (rs.getNumRecords() == 0) rs.addRecord(b, 0, b.length);
      else rs.setRecord(1, b, 0, b.length);
      rs.closeRecordStore();
    } catch (Exception e) {}
  }

  void loadSave() {
    try {
      RecordStore rs = RecordStore.openRecordStore("soulse72", false);
      byte[] b = rs.getRecord(1);
      rs.closeRecordStore();
      DataInputStream d = new DataInputStream(new ByteArrayInputStream(b));
      int v = d.readInt();
      level = d.readInt(); maxHp = d.readInt(); souls = d.readInt();
      rMap = d.readInt(); rx = d.readInt(); ry = d.readInt();
      int bm = d.readInt();
      int gm = d.readInt();
      bossDead[0] = (bm & 1) != 0; bossDead[1] = (bm & 2) != 0;
      gate[0] = (gm & 1) != 0; gate[1] = (gm & 2) != 0;
      if (rMap < 0 || rMap >= MAPS.length) rMap = 0;
      bsSouls = 0;
      respawn();
    } catch (Exception e) { newGame(); }
  }

  // ---------- colisiones ----------
  char tileAt(int tx, int ty) {
    if (tx < 0 || ty < 0 || ty >= MH || tx >= map[ty].length()) return '#';
    return map[ty].charAt(tx);
  }

  boolean solid(int tx, int ty) {
    char c = tileAt(tx, ty);
    return c == '#' || (c == 'G' && !gate[cur]);
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
  boolean guardOn() { return blockHeld && rollT == 0 && drinkT == 0 && atkT == 0 && stunT == 0; }

  // ---------- combate ----------
  // (hx,hy) = vector del jugador hacia el atacante
  void hurtPlayer(int d, int hx, int hy) {
    if (dead || invul()) return;
    if (guardOn() && hx * fx + hy * fy > 0) {
      int cost = d + d / 2;
      if (st >= cost) {
        st -= cost; stDelay = 30; hurtT = 3;
        d = d / 5;
        if (d < 1) d = 1;
      } else {
        st = 0; stunT = 25; stDelay = 40;
      }
    }
    hp -= d; hurtT = 6;
    buzz(60);
    if (hp <= 0) {
      hp = 0; dead = true; deadT = 70;
      bsx = px; bsy = py; bsSouls = souls; bsMap = cur; souls = 0;
      buzz(300);
    }
  }

  void hurtEnemy(int i, int d) {
    ehp[i] -= d;
    if (eT[i] != 3) { eW[i] = 0; eC[i] = 20; }
    if (ehp[i] <= 0) {
      ehp[i] = 0;
      if (lock == i) lock = -1;
      souls += ESOUL[eT[i]];
      if (eT[i] == 3) {
        bossDead[cur] = true;
        say("ENEMIGO ABATIDO", true, 90);
        save();
      }
    }
  }

  int nearBonfire() {
    for (int i = 0; i < nb; i++)
      if (Math.abs(px - bnx[i]) + Math.abs(py - bny[i]) < 28) return i;
    return -1;
  }

  boolean nearLever() {
    return hasLever && !gate[cur] && Math.abs(px - lvx) + Math.abs(py - lvy) < 28;
  }

  void rest(int b) {
    rMap = cur; rx = bnx[b]; ry = bny[b];
    st = 100; estus = 3; lock = -1;
    int up = 0;
    while (souls >= 100 + 40 * level) {
      souls -= 100 + 40 * level;
      level++; maxHp += 15; up++;
    }
    hp = maxHp;
    resetEnemies();
    say(up > 0 ? "Subes a nivel " + level : "Descansas junto a la hoguera", false, 70);
    save();
  }

  void fireArrow(int x, int y, int dx, int dy) {
    if (na >= NA) return;
    int n = Math.abs(dx) + Math.abs(dy);
    if (n == 0) n = 1;
    arX[na] = x; arY[na] = y;
    arVX[na] = dx * 5 / n; arVY[na] = dy * 5 / n;
    arL[na] = 60;
    na++;
  }

  void updateArrows() {
    int i = 0;
    while (i < na) {
      arX[i] += arVX[i]; arY[i] += arVY[i]; arL[i]--;
      boolean gone = arL[i] <= 0 || solid(arX[i] / T, arY[i] / T);
      if (!gone && !dead && Math.abs(arX[i] - px) < 8 && Math.abs(arY[i] - py) < 8 && !invul()) {
        hurtPlayer(14, -arVX[i], -arVY[i]);
        gone = true;
      }
      if (gone) {
        na--;
        arX[i] = arX[na]; arY[i] = arY[na];
        arVX[i] = arVX[na]; arVY[i] = arVY[na]; arL[i] = arL[na];
      } else i++;
    }
  }

  void updateEnemies() {
    tick++;
    for (int i = 0; i < ne; i++) {
      if (ehp[i] <= 0) continue;
      int t = eT[i];
      int dx = px - ex[i], dy = py - ey[i];
      int d = Math.abs(dx) + Math.abs(dy);
      if (eC[i] > 0) eC[i]--;
      if (eD[i] > 0) { // embestida del jefe
        moveEnemy(i, edx[i] * 5, edy[i] * 5);
        eD[i]--;
        if (d < 14 + eh[i] && !invul() && !dead) { hurtPlayer(30, -dx, -dy); eD[i] = 0; }
        if (eD[i] == 0) eC[i] = 50;
        continue;
      }
      if (eW[i] > 0) { // preparando ataque
        eW[i]--;
        if (eW[i] == 0) {
          if (eMode[i] == 1) {
            edx[i] = sg(dx); edy[i] = sg(dy); eD[i] = 12;
          } else if (t == 1) {
            fireArrow(ex[i], ey[i], dx, dy);
            eC[i] = 70;
          } else {
            if (d < 30 + eh[i]) hurtPlayer(t == 3 ? 25 : (t == 2 ? 26 : 12), -dx, -dy);
            eC[i] = t == 3 ? 35 : (t == 2 ? 60 : 45);
          }
        }
        continue;
      }
      if (dead || d >= 150) continue;
      if (t == 1) {
        if (d < 60) moveEnemy(i, -sg(dx), -sg(dy));
        else if (d > 120) moveEnemy(i, sg(dx), sg(dy));
        if (eC[i] == 0 && d >= 50) { eMode[i] = 0; eW[i] = 18; }
      } else if (t == 3 && d > 70 && eC[i] == 0) {
        eMode[i] = 1; eW[i] = 22;
      } else if (d < 24 + eh[i] && eC[i] == 0) {
        eMode[i] = 0; eW[i] = (t == 3 ? 18 : (t == 2 ? 26 : 14));
      } else if (d > 18 + eh[i]) {
        if (t != 2 || (tick & 1) == 0) moveEnemy(i, sg(dx), sg(dy));
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

    boolean guard = guardOn();
    if (rollT > 0) {
      movePlayer(rdx * 5, rdy * 5);
      rollT--;
    } else if (drinkT > 0) {
      drinkT--;
      movePlayer(mx, my);
    } else if (stunT > 0) {
      stunT--;
    } else {
      int sp = (guard || atkT > 0) ? 1 : 2;
      movePlayer(mx * sp, my * sp);
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
        else if (nearLever()) {
          gate[cur] = true;
          say("Se abre una puerta", false, 80);
          save();
        } else if (!guard && atkT == 0 && st >= 20) {
          atkT = 10; hitDone = false; st -= 20; stDelay = 25;
        }
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
    else if (st < 100) st += guard ? 1 : 2;
    if (st > 100) st = 100;
    if (hurtT > 0) hurtT--;
    if (bsSouls > 0 && bsMap == cur && Math.abs(px - bsx) + Math.abs(py - bsy) < 20) {
      souls += bsSouls; bsSouls = 0;
      say("Almas recuperadas", false, 60);
    }
    // puertas entre zonas
    char tc = tileAt(px / T, py / T);
    if (tc == 'N' && cur + 1 < MAPS.length) enterMap(cur + 1, false);
    else if (tc == 'V' && cur > 0) enterMap(cur - 1, true);
  }

  // ---------- teclas ----------
  protected void keyPressed(int kc) {
    int ga = 0;
    try { ga = getGameAction(kc); } catch (Exception e) {}
    if (title) {
      if (ga == UP || ga == DOWN) { if (hasSave) menuSel = 1 - menuSel; }
      else if (ga == FIRE) startReq = (hasSave && menuSel == 0) ? 1 : 2;
      return;
    }
    int c = kc;
    if (c >= 'A' && c <= 'Z') c += 32;
    if (ga == FIRE) atkReq = true;
    else if (c == '1' || c == 'q' || ga == GAME_A) rollReq = true;
    else if (c == '3' || c == 'e' || ga == GAME_B) lockReq = true;
    else if (c == '7' || c == 'r' || ga == GAME_C) estusReq = true;
    else if (c == '9' || c == 'w' || ga == GAME_D) blockHeld = true;
  }

  protected void keyReleased(int kc) {
    int ga = 0;
    try { ga = getGameAction(kc); } catch (Exception e) {}
    int c = kc;
    if (c >= 'A' && c <= 'Z') c += 32;
    if (c == '9' || c == 'w' || ga == GAME_D) blockHeld = false;
  }

  // ---------- dibujo ----------
  void drawTitle(Graphics g) {
    g.setColor(0x000000); g.fillRect(0, 0, W, H);
    Font big = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
    g.setFont(big); g.setColor(0xDDBB44);
    g.drawString("SOULS E72", W / 2, 40, Graphics.TOP | Graphics.HCENTER);
    g.setFont(Font.getDefaultFont());
    String[] items;
    if (hasSave) items = new String[] { "Continuar", "Nueva partida" };
    else items = new String[] { "Nueva partida" };
    for (int i = 0; i < items.length; i++) {
      g.setColor(i == menuSel ? 0xFFFFFF : 0x777777);
      g.drawString((i == menuSel ? "> " : "  ") + items[i], W / 2, 100 + i * 22, Graphics.TOP | Graphics.HCENTER);
    }
    g.setColor(0x777777);
    g.drawString("OK: elegir", W / 2, H - 56, Graphics.TOP | Graphics.HCENTER);
    g.drawString("1 esquiva  3 fijar  7 estus  9 guardia", W / 2, H - 38, Graphics.TOP | Graphics.HCENTER);
  }

  void draw(Graphics g) {
    int cx = Math.max(0, Math.min(px - W / 2, MW * T - W));
    int cy = Math.max(0, Math.min(py - H / 2, MH * T - H));
    g.setColor(0x000000); g.fillRect(0, 0, W, H);
    // tiles
    for (int y = cy / T; y <= (cy + H) / T; y++) {
      for (int x = cx / T; x <= (cx + W) / T; x++) {
        char tc = tileAt(x, y);
        int sx0 = x * T - cx, sy0 = y * T - cy;
        if (tc == '#') {
          g.setColor(0x3A3A44); g.fillRect(sx0, sy0, T, T);
          g.setColor(0x50505C); g.drawLine(sx0, sy0, sx0 + T - 1, sy0);
          g.setColor(0x26262E);
          g.drawLine(sx0, sy0 + T - 1, sx0 + T - 1, sy0 + T - 1);
          g.drawLine(sx0, sy0 + T / 2, sx0 + T - 1, sy0 + T / 2);
          g.drawLine(sx0 + ((y & 1) == 0 ? 4 : 11), sy0, sx0 + ((y & 1) == 0 ? 4 : 11), sy0 + T / 2);
        } else {
          g.setColor(((x + y) & 1) == 0 ? 0x1C1C20 : 0x18181C);
          g.fillRect(sx0, sy0, T, T);
          if (((x * 7 + y * 13) % 5) == 0) { g.setColor(0x26262C); g.fillRect(sx0 + 5, sy0 + 7, 2, 2); }
          if (tc == 'G' && !gate[cur]) {
            g.setColor(0x6A4A2A);
            g.fillRect(sx0 + 2, sy0, 3, T); g.fillRect(sx0 + 7, sy0, 3, T); g.fillRect(sx0 + 12, sy0, 3, T);
          } else if (tc == 'N' || tc == 'V') {
            g.setColor(0x050508); g.fillRect(sx0, sy0, T, T);
            g.setColor(0x887755); g.drawRect(sx0, sy0, T - 1, T - 1);
          }
        }
      }
    }
    // palanca
    if (hasLever) {
      int sx = lvx - cx, sy = lvy - cy;
      g.setColor(0x777777); g.fillRect(sx - 1, sy - 4, 3, 9);
      g.setColor(0xDD4444); g.fillRect(sx - 3, gate[cur] ? sy + 2 : sy - 7, 7, 4);
    }
    // hogueras
    for (int i = 0; i < nb; i++) {
      int sx = bnx[i] - cx, sy = bny[i] - cy;
      g.setColor(0x553311); g.fillRect(sx - 6, sy + 2, 12, 4);
      g.setColor(0xFF8800); g.fillArc(sx - 4, sy - 8, 8, 12, 0, 360);
      g.setColor(0xFFDD44); g.fillArc(sx - 2, sy - 4, 4, 7, 0, 360);
    }
    // mancha de sangre
    if (bsSouls > 0 && bsMap == cur) {
      g.setColor(0x44AAFF); g.fillArc(bsx - cx - 5, bsy - cy - 5, 10, 10, 0, 360);
    }
    // enemigos
    for (int i = 0; i < ne; i++) {
      if (ehp[i] <= 0) continue;
      int t = eT[i], h = eh[i], sx = ex[i] - cx, sy = ey[i] - cy;
      Image im;
      if (t == 0) im = sHo;
      else if (t == 1) im = sAr[px < ex[i] ? 1 : 0];
      else if (t == 2) im = sHe;
      else im = sBo;
      g.drawImage(im, sx - h, sy - h, Graphics.TOP | Graphics.LEFT);
      if (eD[i] > 0) {
        g.setColor(0xFF6600); g.drawRect(sx - h - 1, sy - h - 1, 2 * h + 1, 2 * h + 1);
      } else if (eW[i] > 0) {
        g.setColor(0xFFDD00); g.fillRect(sx - 2, sy - h - 12, 4, 6); g.fillRect(sx - 2, sy - h - 5, 4, 2);
      }
      if (t != 3) {
        g.setColor(0xFF0000); g.fillRect(sx - h, sy - h - 3, 2 * h * ehp[i] / emax[i], 2);
      }
      if (i == lock) { g.setColor(0xFFFFFF); g.drawRect(sx - h - 3, sy - h - 3, 2 * h + 6, 2 * h + 6); }
    }
    // flechas
    for (int i = 0; i < na; i++) {
      int sx = arX[i] - cx, sy = arY[i] - cy;
      g.setColor(0xCC9955); g.drawLine(sx, sy, sx - arVX[i], sy - arVY[i]);
      g.setColor(0xFFFFFF); g.fillRect(sx, sy, 2, 2);
    }
    // jugador
    int sx = px - cx, sy = py - cy;
    if (rollT > 0) {
      g.setColor(0x888888); g.fillRect(sx - 6, sy - 2, 12, 8);
    } else {
      g.drawImage(sPl[fx < 0 ? 1 : 0], sx - 6, sy - 6, Graphics.TOP | Graphics.LEFT);
      if (hurtT > 0) { g.setColor(0xFF4444); g.drawRect(sx - 7, sy - 7, 13, 13); }
    }
    if (guardOn()) {
      g.setColor(0x4466CC); g.fillRect(sx + fx * 9 - 4, sy + fy * 9 - 4, 8, 8);
      g.setColor(0xAABBEE); g.drawRect(sx + fx * 9 - 4, sy + fy * 9 - 4, 7, 7);
    }
    if (atkT > 0) {
      g.setColor(0xFFFF66);
      g.fillRect(sx + fx * 13 - 5, sy + fy * 13 - 5, 10, 10);
    }
    // HUD
    int bw = Math.min(maxHp, 150);
    g.setColor(0x440000); g.fillRect(5, 5, bw, 6);
    g.setColor(0xCC0000); g.fillRect(5, 5, hp * bw / maxHp, 6);
    g.setColor(0x003300); g.fillRect(5, 14, 100, 5);
    g.setColor(0x00BB00); g.fillRect(5, 14, st, 5);
    g.setColor(0xFFFFFF);
    g.drawString("Almas " + souls, W - 5, 4, Graphics.TOP | Graphics.RIGHT);
    g.drawString("Estus " + estus + "  Nv " + level, W - 5, 18, Graphics.TOP | Graphics.RIGHT);
    if (!dead && nearBonfire() >= 0)
      g.drawString("OK: descansar", W / 2, H - 20, Graphics.TOP | Graphics.HCENTER);
    else if (!dead && nearLever())
      g.drawString("OK: palanca", W / 2, H - 20, Graphics.TOP | Graphics.HCENTER);
    // barra del jefe
    for (int i = 0; i < ne; i++) {
      if (eT[i] == 3 && ehp[i] > 0 && Math.abs(ex[i] - px) + Math.abs(ey[i] - py) < 180) {
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
      if (startReq != 0) {
        if (startReq == 1) loadSave();
        else { deleteSave(); newGame(); }
        startReq = 0; title = false;
        atkReq = false; rollReq = false; lockReq = false; estusReq = false; blockHeld = false;
      }
      if (title) {
        drawTitle(g);
      } else {
        if (dead) {
          deadT--;
          if (deadT <= 0) respawn();
        } else {
          updatePlayer();
        }
        updateEnemies();
        updateArrows();
        atkReq = false; rollReq = false; lockReq = false; estusReq = false;
        if (msgT > 0) msgT--;
        draw(g);
      }
      flushGraphics();
      long d = 40 - (System.currentTimeMillis() - t0);
      try { Thread.sleep(d > 5 ? d : 5); } catch (Exception e) {}
    }
  }
}
