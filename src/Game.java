import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStore;
import java.io.*;
import java.util.Random;

public class Game extends GameCanvas implements Runnable {
  static final int T = 16, MW = 30, MH = 20, N = 16, NA = 16, NPA = 6, ND = 14, NF = 8;
  static final String STORE = "soulse72b";
  static Display disp;
  static MIDlet mid;

  // estados
  static final int ST_TITLE = 0, ST_CLASS = 1, ST_PLAY = 2, ST_PAUSE = 3, ST_INV = 4,
                   ST_BONFIRE = 5, ST_SLOTS = 6, ST_OPT = 7, ST_CTRL = 8;

  static final String[] M0 = {
    "##############################",
    "#............#...............#",
    "#.F..........#.......a.......#",
    "#..P.........#...............#",
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
    "#...................B......QDN",
    "#............................#",
    "##############################"
  };
  static final String[] M2 = {
    "##############################",
    "#.................##.........#",
    "#.........c...###.##.........#",
    "#....###......###.##...###...#",
    "#....###.s........##...###...#",
    "#.a##.............##.........#",
    "#..##.....###.....##.........#",
    "#.........###...c.##.........#",
    "#.........###.........h......#",
    "V.P.....#..F...s...........QDN",
    "#.......#.....e..........R...#",
    "#..###.......................#",
    "#..#a#.......###..##.........#",
    "#............###..##.........#",
    "#.....###....###..##.........#",
    "#.....###.s.......##..###....#",
    "#...e.###...###...##..###....#",
    "#...........###.c.##.........#",
    "#.................##.........#",
    "##############################"
  };
  static final String[] M3 = {
    "##############################",
    "##############################",
    "##############################",
    "#########............#.......#",
    "#########......s...F.#.......#",
    "#########.c##....##..#.......#",
    "#........#.##....##..#.......#",
    "#.....e..#...........#h......#",
    "#..F........s................#",
    "V.P...........##......Q...X..#",
    "#.............##.............#",
    "#...............s....#.......#",
    "#....a...#...h.......#.......#",
    "#........#.##....##..#.......#",
    "#########..##....##..#.......#",
    "#########.....s....c.#.......#",
    "#########............#.......#",
    "##############################",
    "##############################",
    "##############################"
  };
  static final String[][] MAPS = { M0, M1, M2, M3 };
  static final String[] ZNAME = { "Santuario", "Catacumbas", "Bosque Maldito", "Castillo del Rey Caído" };
  static final int[] ZMUL = { 100, 115, 135, 160 };

  // ----- clases del jugador: 0 caballero, 1 asesino, 2 clerigo, 3 ladron, 4 arquero -----
  static final String[] CNAME = { "Caballero", "Asesino", "Clérigo", "Ladrón", "Arquero" };
  static final String[] CDESC = { "Tanque con escudo", "Veloz y críticos", "Se regenera", "Más oro y botín", "Ataca de lejos" };
  static final int[] CHP = { 130, 90, 110, 95, 100 };
  static final int[] CDMG = { 13, 10, 9, 8, 9 };
  static final int[] CSPD = { 30, 38, 31, 36, 34 };
  static final int[] CATKLEN = { 13, 8, 13, 9, 11 };
  static final int[] CHIT = { 7, 4, 7, 5, 6 };
  static final int[] CATKCOST = { 22, 12, 20, 14, 14 };
  static final int[] CREACH = { 32, 25, 30, 25, 0 };
  static final int[] CCRIT = { 5, 30, 5, 15, 10 };
  static final int[] CROLL = { 25, 18, 25, 20, 22 };
  static final int[] CGOLD = { 100, 100, 100, 150, 100 };
  static final int[] CPOT = { 50, 50, 80, 50, 50 };
  static final int[] GCOST = { 10, 20, 15, 20, 20 };
  static final int[] GCHIP = { 6, 3, 4, 3, 3 };
  static final String[] TIERW = { "Hierro", "Acero", "Azur", "Oro", "Fuego" };
  static final String[] TIERA = { "Tela", "Cuero", "Hierro", "Acero", "Oro" };
  static final int[] ROLLSP = { 0, 2, 2, 3, 3, 4, 5, 5, 6, 6, 6 };

  // ----- enemigos: 0 hueco, 1 arquero, 2 pesado, 3 jefe -----
  // tipos: 0 hueco, 1 arquero, 2 pesado, 3 guardian(jefe), 4 cultista, 5 esqueleto, 6 druida(jefe), 7 rey caido(jefe)
  static final int[] EH = { 6, 6, 8, 10, 6, 6, 10, 11 };
  static final int[] EHP = { 40, 25, 110, 350, 45, 55, 520, 800 };
  static final int[] ESOUL = { 60, 70, 120, 1000, 90, 80, 1500, 3000 };
  static final int[] ESPD = { 20, 22, 14, 22, 18, 30, 20, 24 };
  static final int[] EG0 = { 3, 5, 15, 150, 8, 6, 250, 400 };
  static final int[] EG1 = { 7, 8, 16, 100, 10, 8, 120, 200 };
  static final int[] EIRON = { 20, 25, 60, 100, 35, 30, 100, 100 };
  static final int[] EPOT = { 10, 12, 25, 100, 15, 10, 100, 100 };
  static final int[] ESTYLE = { 5, 6, 7, 8, 9, 10, 11, 12 };
  static final int[] EWIND = { 14, 18, 26, 18, 24, 10, 20, 13 };
  static final int[] ECOOL = { 45, 70, 60, 35, 70, 35, 45, 28 };
  static final int[] EDMG = { 12, 14, 26, 25, 18, 14, 28, 34 };
  static final String[] BNAME = { "", "", "", "GUARDIÁN", "", "", "DRUIDA ANCESTRAL", "REY CAÍDO" };
  static final int[] SEQ6 = { 3, 0, 2, 3, 2 };
  static final int[] SEQ7 = { 0, 1, 2, 0, 4 };
  static final int[] RX = { 1, 1, 0, -1, -1, -1, 0, 1 };
  static final int[] RY = { 0, 1, 1, 1, 0, -1, -1, -1 };

  public boolean run = true;
  Random rng = new Random();
  int W, H, tick;
  int state = ST_TITLE, prevState, menuSel;
  String[] map;
  int cur, rMap;
  boolean[] bossDead = new boolean[4], gate = new boolean[4];
  int winT;

  // opciones y guardado
  boolean optVib = true, optHard = false;
  int lastSlot = -1, curSlot = 0;
  boolean[] slotUsed = new boolean[3];
  String[] slotInfo = new String[3];
  int slotMode, slotsFrom, optFrom, invFrom;

  // jugador
  int cls = 0, level = 1, souls, gold, iron, potions = 3, wLvl, aLvl, kills;
  int px, py, pfx, pfy, pvx, pvy, fx = 1, fy = 0, hp = 100, st = 100, stDelay;
  int rollT, atkT, drinkT, hurtT, stunT, iT, lock = -1, pAnim, regenT;
  int rdx, rdy, rx, ry, deadT;
  boolean faceL, hitDone, dead, blockHeld;
  boolean atkReq, rollReq, lockReq, potReq;
  boolean kUp, kDown, kLeft, kRight, kOk, kBack, kInv;
  int bsx, bsy, bsSouls, bsMap;
  int bfIdx;

  // mapa actual
  int startX, startY, qX, qY, lvx, lvy;
  boolean hasLever;
  int[] bnx = new int[4], bny = new int[4];
  int nb;

  // enemigos
  int ne;
  int[] ex = new int[N], ey = new int[N], ehp = new int[N], emax = new int[N];
  int[] esx = new int[N], esy = new int[N], eh = new int[N], eT = new int[N];
  int[] eW = new int[N], eC = new int[N], eD = new int[N], eS = new int[N], eHit = new int[N], eDie = new int[N];
  int[] edx = new int[N], edy = new int[N], eMode = new int[N];
  int[] evx = new int[N], evy = new int[N], efx = new int[N], efy = new int[N], eAnim = new int[N];
  int[] eP = new int[N], eK = new int[N], eB = new int[N], eRg = new int[N];

  // proyectiles
  int na;
  int[] arX = new int[NA], arY = new int[NA], arVX = new int[NA], arVY = new int[NA], arL = new int[NA], arT = new int[NA];
  int npa;
  int[] paX = new int[NPA], paY = new int[NPA], paVX = new int[NPA], paVY = new int[NPA], paL = new int[NPA], paD = new int[NPA];

  // botin y textos flotantes
  int[] lx = new int[ND], ly = new int[ND], lt = new int[ND], lv = new int[ND], lL = new int[ND];
  String[] ftS = new String[NF];
  int[] ftX = new int[NF], ftY = new int[NF], ftT = new int[NF], ftC = new int[NF];
  String msg = ""; int msgT; boolean msgBig;

  // camara
  int camx, camy;

  // sprites
  Image[] plFr, prevFr;
  Image[][] spr = new Image[8][];
  Image[] tiles = new Image[20];
  int[] starX = new int[36], starY = new int[36];
  int selClass;

  public Game() {
    super(false);
    setFullScreenMode(true);
    W = getWidth(); H = getHeight();
    int s = 12345;
    for (int i = 0; i < 36; i++) {
      s = s * 1103515245 + 12345;
      starX[i] = ((s >> 8) & 0x7FFF) % W;
      s = s * 1103515245 + 12345;
      starY[i] = ((s >> 8) & 0x7FFF) % (H * 2 / 3);
    }
    buildTiles();
    loadOptions();
    refreshSlots();
    openTitle();
  }

  // =====================================================================
  //  TEJAS
  // =====================================================================
  int shade(int c, int d) {
    int r = ((c >> 16) & 255) + d, g = ((c >> 8) & 255) + d, b = (c & 255) + d;
    if (r < 0) r = 0; if (r > 255) r = 255;
    if (g < 0) g = 0; if (g > 255) g = 255;
    if (b < 0) b = 0; if (b > 255) b = 255;
    return (r << 16) | (g << 8) | b;
  }

  Image mkTile(int col, int kind) {
    Image im = Image.createImage(T, T);
    Graphics g = im.getGraphics();
    g.setColor(col); g.fillRect(0, 0, T, T);
    switch (kind) {
      case 0:
        g.setColor(shade(col, -8)); g.fillRect(3, 4, 2, 2); g.fillRect(11, 11, 2, 1);
        break;
      case 1:
        g.setColor(shade(col, 7)); g.drawLine(0, 0, T - 1, 0); g.drawLine(0, 0, 0, T - 1);
        g.setColor(shade(col, -7)); g.fillRect(8, 9, 3, 2);
        break;
      case 2:
        g.setColor(shade(col, -14));
        g.drawLine(2, 3, 6, 7); g.drawLine(6, 7, 5, 11); g.drawLine(6, 7, 11, 8);
        break;
      case 5: case 6: {
        g.setColor(shade(col, -10)); g.fillRect(0, 0, T, T);
        g.setColor(0x4A3220); g.fillRect(6, 10, 4, 6);
        int o = kind == 5 ? 0 : 1;
        g.setColor(0x0E2A14); g.fillArc(0, 0 + o, 16, 14, 0, 360);
        g.setColor(0x1E5A2A); g.fillArc(1, 0 + o, 14, 12, 0, 360);
        g.setColor(0x2E8A3E); g.fillArc(3 + o, 2, 7, 5, 0, 360);
        g.setColor(0x8AD070); g.fillRect(5 + o, 3, 2, 1);
        break; }
      default: {
        int mor = shade(col, -26);
        g.setColor(shade(col, 14)); g.drawLine(0, 0, T - 1, 0);
        g.setColor(mor);
        g.drawLine(0, 7, T - 1, 7); g.drawLine(0, T - 1, T - 1, T - 1);
        int a = kind == 3 ? 4 : 10, b = kind == 3 ? 11 : 3;
        g.drawLine(a, 0, a, 6); g.drawLine(b, 8, b, T - 2);
        g.setColor(shade(col, -10)); g.fillRect(T - 3, 9, 2, 2);
        break; }
    }
    return im;
  }

  void buildTiles() {
    int[] fl0 = { 0x34323E, 0x2E2C38, 0x302E3A };
    int[] fl1 = { 0x26323A, 0x222E36, 0x242F38 };
    for (int k = 0; k < 3; k++) {
      tiles[k] = mkTile(fl0[k], k);
      tiles[5 + k] = mkTile(fl1[k], k);
    }
    tiles[3] = mkTile(0x5A5666, 3); tiles[4] = mkTile(0x5A5666, 4);
    tiles[8] = mkTile(0x48606C, 3); tiles[9] = mkTile(0x48606C, 4);
    int[] fl2 = { 0x24402A, 0x203824, 0x223C28 };
    int[] fl3 = { 0x3C3844, 0x363240, 0x383442 };
    for (int k = 0; k < 3; k++) {
      tiles[10 + k] = mkTile(fl2[k], k);
      tiles[15 + k] = mkTile(fl3[k], k);
    }
    tiles[13] = mkTile(0x24402A, 5); tiles[14] = mkTile(0x24402A, 6);
    tiles[18] = mkTile(0x5A566E, 3); tiles[19] = mkTile(0x5A566E, 4);
  }

  // =====================================================================
  //  PARTIDA
  // =====================================================================
  int maxHp() { return CHP[cls] + (level - 1) * 12 + aLvl * 8; }
  int dmgBase() { return CDMG[cls] + wLvl * 4 + level * 2; }
  int defPct() { return aLvl * 6; }
  int lvCost() { return 100 + 40 * level; }
  int upGold(int l) { return 40 * (l + 1); }
  int upIron(int l) { return l + 1; }

  void buildPlayer() {
    plFr = null;
    plFr = Art.build(cls, aLvl / 2, wLvl / 2);
  }

  void startNew(int c) {
    cls = c; level = 1; souls = 0; gold = 20; iron = 0; potions = 3; wLvl = 0; aLvl = 0; kills = 0;
    bsSouls = 0;
    for (int i = 0; i < 4; i++) { bossDead[i] = false; gate[i] = false; }
    curSlot = 0;
    for (int i = 0; i < 3; i++) if (!slotUsed[i]) { curSlot = i; break; }
    prevFr = null;
    buildPlayer();
    loadMap(0);
    rMap = 0; rx = startX; ry = startY;
    respawn();
    camSnap();
    state = ST_PLAY;
    say("Enciende una hoguera para guardar", false, 90);
  }

  void ensureSprites() {
    boolean[] need = new boolean[8];
    for (int i = 0; i < ne; i++) need[eT[i]] = true;
    for (int t = 0; t < 8; t++) {
      if (!need[t]) spr[t] = null;
    }
    for (int t = 0; t < 8; t++) {
      if (need[t] && spr[t] == null) spr[t] = Art.build(ESTYLE[t], 0, 0);
    }
  }

  void loadMap(int m) {
    cur = m; map = MAPS[m];
    nb = 0; ne = 0; hasLever = false; na = 0; npa = 0;
    startX = 24; startY = 24; qX = 24; qY = 24;
    for (int i = 0; i < ND; i++) lL[i] = 0;
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
          else if (c == 'c') t = 4;
          else if (c == 's') t = 5;
          else if (c == 'R') t = 6;
          else if (c == 'X') t = 7;
          if (t >= 0 && ne < N) {
            esx[ne] = cx; esy[ne] = cy; eT[ne] = t; eh[ne] = EH[t]; emax[ne] = EHP[t] * ZMUL[m] / 100;
            ne++;
          }
        }
      }
    }
    ensureSprites();
  }

  void enterMap(int m, boolean fromNext) {
    loadMap(m);
    px = fromNext ? qX : startX;
    py = fromNext ? qY : startY;
    pfx = 0; pfy = 0; pvx = 0; pvy = 0;
    lock = -1;
    resetEnemies();
    camSnap();
    say(ZNAME[m], true, 80);
  }

  void resetEnemies() {
    for (int i = 0; i < ne; i++) {
      ehp[i] = (isBoss(eT[i]) && bossDead[cur]) ? 0 : emax[i];
      ex[i] = esx[i]; ey[i] = esy[i];
      eW[i] = 0; eC[i] = 30; eD[i] = 0; eS[i] = 0; eHit[i] = 0; eDie[i] = 0;
      evx[i] = 0; evy[i] = 0; efx[i] = 0; efy[i] = 0; eP[i] = 0; eK[i] = 0; eB[i] = 0; eRg[i] = 0;
    }
    na = 0; npa = 0;
    for (int i = 0; i < ND; i++) lL[i] = 0;
  }

  void respawn() {
    if (cur != rMap) loadMap(rMap);
    px = rx; py = ry; pfx = 0; pfy = 0; pvx = 0; pvy = 0;
    hp = maxHp(); st = 100;
    rollT = 0; atkT = 0; drinkT = 0; stunT = 0; iT = 0; lock = -1; dead = false;
    resetEnemies();
    camSnap();
  }

  void camSnap() {
    camx = Math.max(0, Math.min(px - W / 2, MW * T - W));
    camy = Math.max(0, Math.min(py - H / 2, MH * T - H));
  }

  void say(String s, boolean big, int t) { msg = s; msgBig = big; msgT = t; }

  void buzz(int ms) {
    if (!optVib) return;
    try { if (disp != null) disp.vibrate(ms); } catch (Exception e) {}
  }

  void float_(int x, int y, String s, int col) {
    int k = 0;
    for (int i = 0; i < NF; i++) if (ftT[i] <= 0) { k = i; break; }
    ftS[k] = s; ftX[k] = x; ftY[k] = y; ftT[k] = 28; ftC[k] = col;
  }

  // =====================================================================
  //  GUARDADO (3 ranuras + opciones)
  // =====================================================================
  RecordStore openStore() throws Exception {
    RecordStore rs = RecordStore.openRecordStore(STORE, true);
    byte[] z = new byte[1];
    while (rs.getNumRecords() < 4) rs.addRecord(z, 0, 1);
    return rs;
  }

  void refreshSlots() {
    for (int i = 0; i < 3; i++) { slotUsed[i] = false; slotInfo[i] = "Vacío"; }
    try {
      RecordStore rs = openStore();
      for (int i = 0; i < 3; i++) {
        byte[] b = rs.getRecord(i + 1);
        if (b.length > 8 && b[0] == 1) {
          DataInputStream d = new DataInputStream(new ByteArrayInputStream(b));
          d.readByte();
          int c = d.readInt(), lv = d.readInt(), g = d.readInt();
          slotUsed[i] = true;
          slotInfo[i] = CNAME[c] + " Nv" + lv + "  " + g + " oro";
        }
      }
      rs.closeRecordStore();
    } catch (Exception e) {}
  }

  void saveSlot(int i) {
    try {
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      DataOutputStream d = new DataOutputStream(bo);
      d.writeByte(1);
      d.writeInt(cls); d.writeInt(level); d.writeInt(gold); d.writeInt(souls); d.writeInt(iron);
      d.writeInt(potions); d.writeInt(wLvl); d.writeInt(aLvl); d.writeInt(kills);
      d.writeInt(rMap); d.writeInt(rx); d.writeInt(ry);
      int bmk = 0, gmk = 0;
      for (int q = 0; q < 4; q++) { if (bossDead[q]) bmk |= 1 << q; if (gate[q]) gmk |= 1 << q; }
      d.writeInt(bmk);
      d.writeInt(gmk);
      d.flush();
      byte[] b = bo.toByteArray();
      RecordStore rs = openStore();
      rs.setRecord(i + 1, b, 0, b.length);
      rs.closeRecordStore();
      curSlot = i; lastSlot = i;
      saveOptions();
      refreshSlots();
    } catch (Exception e) {}
  }

  boolean loadSlot(int i) {
    try {
      RecordStore rs = openStore();
      byte[] b = rs.getRecord(i + 1);
      rs.closeRecordStore();
      if (b.length < 9 || b[0] != 1) return false;
      DataInputStream d = new DataInputStream(new ByteArrayInputStream(b));
      d.readByte();
      cls = d.readInt(); level = d.readInt(); gold = d.readInt(); souls = d.readInt(); iron = d.readInt();
      potions = d.readInt(); wLvl = d.readInt(); aLvl = d.readInt(); kills = d.readInt();
      rMap = d.readInt(); rx = d.readInt(); ry = d.readInt();
      int bm = d.readInt(), gm = d.readInt();
      for (int q = 0; q < 4; q++) { bossDead[q] = (bm & (1 << q)) != 0; gate[q] = (gm & (1 << q)) != 0; }
      if (cls < 0 || cls > 4) cls = 0;
      if (rMap < 0 || rMap >= MAPS.length) rMap = 0;
      bsSouls = 0;
      curSlot = i; lastSlot = i;
      prevFr = null;
      buildPlayer();
      loadMap(rMap);
      respawn();
      state = ST_PLAY;
      say("Partida cargada", false, 60);
      return true;
    } catch (Exception e) { return false; }
  }

  void loadOptions() {
    try {
      RecordStore rs = openStore();
      byte[] b = rs.getRecord(4);
      rs.closeRecordStore();
      if (b.length > 4 && b[0] == 1) {
        DataInputStream d = new DataInputStream(new ByteArrayInputStream(b));
        d.readByte();
        optVib = d.readInt() != 0; optHard = d.readInt() != 0; lastSlot = d.readInt();
      }
    } catch (Exception e) {}
  }

  void saveOptions() {
    try {
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      DataOutputStream d = new DataOutputStream(bo);
      d.writeByte(1);
      d.writeInt(optVib ? 1 : 0); d.writeInt(optHard ? 1 : 0); d.writeInt(lastSlot);
      d.flush();
      byte[] b = bo.toByteArray();
      RecordStore rs = openStore();
      rs.setRecord(4, b, 0, b.length);
      rs.closeRecordStore();
    } catch (Exception e) {}
  }

  void quit() {
    saveOptions();
    run = false;
    try { if (mid != null) mid.notifyDestroyed(); } catch (Exception e) {}
  }

  // =====================================================================
  //  COLISIONES Y MOVIMIENTO
  // =====================================================================
  char tileAt(int tx, int ty) {
    if (tx < 0 || ty < 0 || ty >= MH || tx >= map[ty].length()) return '#';
    return map[ty].charAt(tx);
  }

  boolean solid(int tx, int ty) {
    char c = tileAt(tx, ty);
    return c == '#' || (c == 'G' && !gate[cur]) || (c == 'D' && !bossDead[cur]);
  }

  boolean free(int x, int y, int h) {
    return !solid((x - h) / T, (y - h) / T) && !solid((x + h - 1) / T, (y - h) / T)
        && !solid((x - h) / T, (y + h - 1) / T) && !solid((x + h - 1) / T, (y + h - 1) / T);
  }

  int sg(int v) { return v > 0 ? 1 : (v < 0 ? -1 : 0); }
  int rnd(int n) { return (rng.nextInt() & 0x7FFFFFFF) % n; }

  int approach(int v, int t, int a) {
    if (v < t) { v += a; if (v > t) v = t; }
    else if (v > t) { v -= a; if (v < t) v = t; }
    return v;
  }

  void stepP(int sx, int sy) {
    int s = sg(sx);
    for (int i = 0; i != sx; i += s) { if (free(px + s, py, 6)) px += s; else { pvx = 0; break; } }
    s = sg(sy);
    for (int i = 0; i != sy; i += s) { if (free(px, py + s, 6)) py += s; else { pvy = 0; break; } }
  }

  void moveSmooth(int tvx, int tvy) {
    pvx = approach(pvx, tvx, 7); pvy = approach(pvy, tvy, 7);
    int tx = pfx + pvx; pfx = tx & 15;
    int ty = pfy + pvy; pfy = ty & 15;
    stepP(tx >> 4, ty >> 4);
  }

  void moveE(int i, int tvx, int tvy, int acc) {
    evx[i] = approach(evx[i], tvx, acc); evy[i] = approach(evy[i], tvy, acc);
    int tx = efx[i] + evx[i]; efx[i] = tx & 15;
    int ty = efy[i] + evy[i]; efy[i] = ty & 15;
    int sx = tx >> 4, sy = ty >> 4;
    int s = sg(sx);
    for (int k = 0; k != sx; k += s) { if (free(ex[i] + s, ey[i], eh[i])) ex[i] += s; else { evx[i] = 0; break; } }
    s = sg(sy);
    for (int k = 0; k != sy; k += s) { if (free(ex[i], ey[i] + s, eh[i])) ey[i] += s; else { evy[i] = 0; break; } }
  }

  void steer(int i, int dx, int dy, int spd) {
    int n = Math.abs(dx) + Math.abs(dy);
    if (n == 0) { moveE(i, 0, 0, 3); return; }
    moveE(i, dx * spd * 9 / (n * 8), dy * spd * 9 / (n * 8), 3);
  }

  boolean invul() { return rollT > 4 || iT > 0; }
  boolean guardOn() { return blockHeld && rollT == 0 && drinkT == 0 && atkT == 0 && stunT == 0; }

  // =====================================================================
  //  COMBATE
  // =====================================================================
  void hurtPlayer(int d, int hx, int hy) {
    if (dead || invul()) return;
    d = d * ZMUL[cur] / 100;
    if (optHard) d = d * 3 / 2;
    d = d * (100 - defPct()) / 100;
    if (d < 1) d = 1;
    if (guardOn() && hx * fx + hy * fy > 0) {
      int cost = d * GCOST[cls] / 10;
      if (st >= cost) {
        st -= cost; stDelay = 30; hurtT = 3; iT = 6;
        d = d / GCHIP[cls];
        if (d < 1) d = 1;
        float_(px, py - 22, "block", 0x88BBFF);
      } else {
        st = 0; stunT = 25; stDelay = 40;
      }
    }
    hp -= d; hurtT = 6; iT = Math.max(iT, 12);
    float_(px, py - 22, "-" + d, 0xFF5555);
    buzz(60);
    if (hp <= 0) {
      hp = 0; dead = true; deadT = 80; blockHeld = false;
      bsx = px; bsy = py; bsSouls = souls; bsMap = cur; souls = 0;
      buzz(300);
    }
  }

  void hurtEnemy(int i, int d, boolean crit, int kx, int ky) {
    ehp[i] -= d;
    float_(ex[i], ey[i] - eh[i] - 24, "" + d, crit ? 0xFFE040 : 0xFFFFFF);
    eHit[i] = isBoss(eT[i]) ? 3 : 9;
    if (!isBoss(eT[i])) { eW[i] = 0; eD[i] = 0; eC[i] = Math.max(eC[i], 20); }
    int kb = isBoss(eT[i]) ? 20 : (eT[i] == 2 ? 36 : 56);
    evx[i] = kx * kb; evy[i] = ky * kb;
    if (ehp[i] <= 0) killEnemy(i);
  }

  boolean isBoss(int t) { return t == 3 || t == 6 || t == 7; }

  void killEnemy(int i) {
    ehp[i] = 0; eDie[i] = 16; kills++;
    if (lock == i) lock = -1;
    souls += ESOUL[eT[i]];
    float_(ex[i], ey[i] - 30, "+" + ESOUL[eT[i]] + " almas", 0x88DDFF);
    int t = eT[i];
    int g = (EG0[t] + rnd(EG1[t])) * CGOLD[cls] / 100 * (100 + 25 * cur) / 100;
    spawnDrop(ex[i], ey[i], 0, g);
    int bonus = cls == 3 ? 15 : 0;
    if (rnd(100) < EIRON[t] + bonus) spawnDrop(ex[i], ey[i], 2, t == 3 ? 3 + rnd(3) : 1 + (t == 2 ? rnd(2) : 0));
    if (rnd(100) < EPOT[t] + bonus / 2) spawnDrop(ex[i], ey[i], 1, t == 3 ? 2 : 1);
    if (isBoss(t)) {
      bossDead[cur] = true;
      if (t == 7) { say("¡VICTORIA!", true, 160); winT = 160; }
      else say("ENEMIGO ABATIDO", true, 100);
      saveSlot(curSlot);
    }
  }

  void spawnDrop(int x, int y, int type, int val) {
    int k = -1;
    for (int i = 0; i < ND; i++) if (lL[i] <= 0) { k = i; break; }
    if (k < 0) return;
    lx[k] = x + rnd(11) - 5; ly[k] = y + rnd(9) - 4; lt[k] = type; lv[k] = val; lL[k] = 900;
  }

  void updateDrops() {
    for (int i = 0; i < ND; i++) {
      if (lL[i] <= 0) continue;
      lL[i]--;
      if (dead) continue;
      int dx = px - lx[i], dy = py - ly[i];
      int d = Math.abs(dx) + Math.abs(dy);
      if (d < 56 && lL[i] < 880) {
        lx[i] += sg(dx) * 2; ly[i] += sg(dy) * 2;
      }
      if (d < 12) {
        if (lt[i] == 0) { gold += lv[i]; float_(px, py - 26, "+" + lv[i] + " oro", 0xFFD84A); }
        else if (lt[i] == 1) {
          if (potions < 9) { potions += lv[i]; float_(px, py - 26, "+poción", 0xFF6A6A); }
          else float_(px, py - 26, "llevas 9", 0xAAAAAA);
        } else { iron += lv[i]; float_(px, py - 26, "+" + lv[i] + " hierro", 0xC0C8D8); }
        lL[i] = 0;
      }
    }
  }

  void updateFloats() {
    for (int i = 0; i < NF; i++) if (ftT[i] > 0) { ftT[i]--; if ((ftT[i] & 1) == 0) ftY[i]--; }
  }

  int nearBonfire() {
    for (int i = 0; i < nb; i++)
      if (Math.abs(px - bnx[i]) + Math.abs(py - bny[i]) < 30) return i;
    return -1;
  }

  boolean nearLever() {
    return hasLever && !gate[cur] && Math.abs(px - lvx) + Math.abs(py - lvy) < 28;
  }

  void restAt(int b) {
    rMap = cur; rx = bnx[b]; ry = bny[b];
    hp = maxHp(); st = 100; lock = -1;
    resetEnemies();
    saveSlot(curSlot);
    say("Descansas junto a la hoguera", false, 70);
  }

  void firePlayerArrow() {
    if (npa >= NPA) return;
    int vx, vy;
    if (lock >= 0 && ehp[lock] > 0) {
      int dx = ex[lock] - px, dy = ey[lock] - py;
      int n = Math.abs(dx) + Math.abs(dy);
      if (n == 0) n = 1;
      vx = dx * 8 / n; vy = dy * 8 / n;
    } else {
      int n = Math.abs(fx) + Math.abs(fy);
      if (n == 0) { fx = faceL ? -1 : 1; n = 1; }
      if (n == 1) { vx = fx * 7; vy = fy * 7; } else { vx = fx * 5; vy = fy * 5; }
    }
    paX[npa] = px; paY[npa] = py - 2; paVX[npa] = vx; paVY[npa] = vy; paL[npa] = 30; paD[npa] = dmgBase();
    npa++;
  }

  void updatePArrows() {
    int i = 0;
    while (i < npa) {
      paX[i] += paVX[i]; paY[i] += paVY[i]; paL[i]--;
      boolean gone = paL[i] <= 0 || solid(paX[i] / T, paY[i] / T);
      for (int j = 0; j < ne && !gone; j++) {
        if (ehp[j] <= 0) continue;
        if (Math.abs(paX[i] - ex[j]) < eh[j] + 3 && Math.abs(paY[i] - ey[j]) < eh[j] + 4) {
          boolean crit = rnd(100) < CCRIT[cls];
          int d = crit ? paD[i] * 2 : paD[i];
          hurtEnemy(j, d, crit, sg(paVX[i]), sg(paVY[i]));
          gone = true;
        }
      }
      if (gone) {
        npa--;
        paX[i] = paX[npa]; paY[i] = paY[npa]; paVX[i] = paVX[npa]; paVY[i] = paVY[npa];
        paL[i] = paL[npa]; paD[i] = paD[npa];
      } else i++;
    }
  }

  void meleeHit() {
    int reach = CREACH[cls];
    boolean any = false;
    for (int i = 0; i < ne; i++) {
      if (ehp[i] <= 0) continue;
      int dx = ex[i] - px, dy = ey[i] - py;
      if (Math.abs(dx) + Math.abs(dy) < reach + eh[i] && dx * fx + dy * fy >= 0) {
        boolean crit = rnd(100) < CCRIT[cls];
        int d = dmgBase();
        if (crit) d = cls == 1 ? d * 5 / 2 : d * 2;
        hurtEnemy(i, d, crit, sg(dx), sg(dy));
        any = true;
      }
    }
    if (any) buzz(25);
  }

  void fireArrow(int x, int y, int dx, int dy, int type) {
    if (na >= NA) return;
    int ax = Math.abs(dx), ay = Math.abs(dy);
    int n = 2 * Math.max(ax, ay) + Math.min(ax, ay);
    if (n == 0) n = 1;
    int sp = type == 1 ? 3 : 5;
    arX[na] = x; arY[na] = y;
    arVX[na] = dx * sp * 2 / n; arVY[na] = dy * sp * 2 / n;
    arL[na] = type == 1 ? 90 : 60; arT[na] = type;
    na++;
  }

  void updateArrows() {
    int i = 0;
    while (i < na) {
      arX[i] += arVX[i]; arY[i] += arVY[i]; arL[i]--;
      boolean gone = arL[i] <= 0 || solid(arX[i] / T, arY[i] / T);
      if (!gone && !dead && Math.abs(arX[i] - px) < 8 && Math.abs(arY[i] - py) < 8 && !invul()) {
        hurtPlayer(arT[i] == 1 ? 18 : 14, -arVX[i], -arVY[i]);
        gone = true;
      }
      if (gone) {
        na--;
        arX[i] = arX[na]; arY[i] = arY[na];
        arVX[i] = arVX[na]; arVY[i] = arVY[na]; arL[i] = arL[na]; arT[i] = arT[na];
      } else i++;
    }
  }

  int bossWind(int t, int m, boolean rage) {
    int w = 20;
    if (m == 0) w = t == 6 ? 20 : (rage ? 9 : 13);
    else if (m == 1) w = rage ? 16 : 22;
    else if (m == 2) w = rage ? 24 : 30;
    else if (m == 3) w = 20;
    else w = rage ? 22 : 28;
    return w;
  }

  void finishAttack(int i, int t, int dx, int dy, int d, boolean rage) {
    int m = eMode[i];
    if (m == 1) { edx[i] = sg(dx); edy[i] = sg(dy); eD[i] = 12; return; }
    if (m == 2) { // explosion de area
      eB[i] = 10; eS[i] = 8;
      int R = 60 + eh[i];
      if (dx * dx + 4 * dy * dy < R * R) hurtPlayer(t == 7 ? 32 : 26, -dx, -dy);
      eC[i] = rage ? 30 : 50;
      buzz(40);
      return;
    }
    if (m == 3) { // salva de tres proyectiles
      eS[i] = 6;
      fireArrow(ex[i], ey[i] - 4, dx, dy, 0);
      fireArrow(ex[i], ey[i] - 4, dx - dy / 3, dy + dx / 3, 0);
      fireArrow(ex[i], ey[i] - 4, dx + dy / 3, dy - dx / 3, 0);
      eC[i] = 40;
      return;
    }
    if (m == 4) { // anillo de fuego
      eS[i] = 8;
      for (int k = 0; k < 8; k++) fireArrow(ex[i], ey[i] - 4, RX[k] * 8, RY[k] * 8, 1);
      eC[i] = rage ? 24 : 40;
      return;
    }
    if (t == 1 || t == 4) {
      fireArrow(ex[i], ey[i] - 4, dx, dy, t == 4 ? 1 : 0);
      eS[i] = 6; eC[i] = ECOOL[t];
      return;
    }
    eS[i] = 7;
    if (d < 30 + eh[i]) hurtPlayer(EDMG[t], -dx, -dy);
    if (t == 7 && eK[i] == 0) { eK[i] = 1; eW[i] = 8; eMode[i] = 0; return; }  // combo
    eK[i] = 0;
    eC[i] = Math.max(10, ECOOL[t] - (rage ? 10 : 0));
  }

  void updateEnemies() {
    for (int i = 0; i < ne; i++) {
      if (eDie[i] > 0) { eDie[i]--; continue; }
      if (ehp[i] <= 0) continue;
      int t = eT[i];
      int dx = px - ex[i], dy = py - ey[i];
      int d = Math.abs(dx) + Math.abs(dy);
      boolean rage = t == 7 && ehp[i] * 2 < emax[i];
      if (rage && eRg[i] == 0) { eRg[i] = 1; say("¡El Rey se enfurece!", false, 90); buzz(150); }
      if (eC[i] > 0) eC[i]--;
      if (eS[i] > 0) eS[i]--;
      if (eB[i] > 0) eB[i]--;
      if (eHit[i] > 0) { eHit[i]--; moveE(i, 0, 0, 5); continue; }
      if (eD[i] > 0) { // embestida
        moveE(i, edx[i] * 80, edy[i] * 80, 100);
        eD[i]--;
        if (d < 14 + eh[i] && !invul() && !dead) { hurtPlayer(t == 7 ? 36 : 30, -dx, -dy); eD[i] = 0; }
        if (eD[i] == 0) eC[i] = 50;
        continue;
      }
      if (eW[i] > 0) { // preparando ataque
        moveE(i, 0, 0, 5);
        eW[i]--;
        if (eW[i] == 0) finishAttack(i, t, dx, dy, d, rage);
        continue;
      }
      if (dead || d >= 150) {
        int sdx = esx[i] - ex[i], sdy = esy[i] - ey[i];
        if (!dead && d > 180 && Math.abs(sdx) + Math.abs(sdy) > 6) steer(i, sdx, sdy, 12);
        else moveE(i, 0, 0, 3);
        continue;
      }
      int spd = ESPD[t] + (rage ? 8 : 0);
      if (t == 1 || t == 4) {          // arquero y cultista: atacan de lejos
        if (d < 60) steer(i, -dx, -dy, spd);
        else if (d > 120) steer(i, dx, dy, spd);
        else moveE(i, 0, 0, 3);
        if (eC[i] == 0 && d >= 50) { eMode[i] = 0; eW[i] = EWIND[t]; }
      } else if (t == 3) {             // guardian
        if (d > 70 && eC[i] == 0) { eMode[i] = 1; eW[i] = 22; }
        else if (d < 24 + eh[i] && eC[i] == 0) { eMode[i] = 0; eW[i] = EWIND[t]; }
        else if (d > 18 + eh[i]) steer(i, dx, dy, spd);
        else moveE(i, 0, 0, 3);
      } else if (t == 6 || t == 7) {   // druida y rey: secuencia de ataques
        int[] seq = t == 6 ? SEQ6 : SEQ7;
        int m = seq[eP[i] % seq.length];
        if (eC[i] == 0) {
          if ((m == 0 && d > 100) || (m == 2 && d > 110)) eP[i]++;   // jugador lejos: pasa a otro ataque
          else if (m == 0 && d >= 24 + eh[i]) steer(i, dx, dy, spd);
          else { eMode[i] = m; eW[i] = bossWind(t, m, rage); eP[i]++; eK[i] = 0; }
        } else if (d > 40 + eh[i]) steer(i, dx, dy, spd / 2 + 4);
        else moveE(i, 0, 0, 3);
      } else if (d < 24 + eh[i] && eC[i] == 0) {
        eMode[i] = 0; eW[i] = EWIND[t];
      } else if (d > 18 + eh[i]) {
        if (t != 2 || (tick & 1) == 0) steer(i, dx, dy, spd);
      } else moveE(i, 0, 0, 3);
      if (Math.abs(evx[i]) + Math.abs(evy[i]) > 8) eAnim[i]++;
    }
    // separacion entre enemigos
    for (int i = 0; i < ne; i++) {
      if (ehp[i] <= 0) continue;
      for (int j = i + 1; j < ne; j++) {
        if (ehp[j] <= 0) continue;
        int dx = ex[j] - ex[i], dy = ey[j] - ey[i];
        if (Math.abs(dx) + Math.abs(dy) < 12) {
          int sx = sg(dx), sy = sg(dy);
          if (sx == 0 && sy == 0) sx = 1;
          if (free(ex[j] + sx, ey[j] + sy, eh[j])) { ex[j] += sx; ey[j] += sy; }
          if (free(ex[i] - sx, ey[i] - sy, eh[i])) { ex[i] -= sx; ey[i] -= sy; }
        }
      }
    }
  }

  // =====================================================================
  //  JUGADOR
  // =====================================================================
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
        int best = 140;
        for (int i = 0; i < ne; i++) {
          if (ehp[i] <= 0) continue;
          int d = Math.abs(ex[i] - px) + Math.abs(ey[i] - py);
          if (d < best) { best = d; lock = i; }
        }
      }
    }
    if (lock >= 0 && (ehp[lock] <= 0 || Math.abs(ex[lock] - px) + Math.abs(ey[lock] - py) > 160)) lock = -1;

    boolean guard = guardOn();
    int spd = CSPD[cls];
    if (rollT > 0) {
      int sp = ROLLSP[rollT];
      stepP(rdx * sp, rdy * sp);
      pvx = rdx * 28; pvy = rdy * 28;
      rollT--;
    } else if (drinkT > 0) {
      drinkT--;
      moveSmooth(mx * 12, my * 12);
    } else if (stunT > 0) {
      stunT--;
      moveSmooth(0, 0);
    } else {
      int tv = (guard || atkT > 0) ? spd / 2 : spd;
      int tx = mx * tv, ty = my * tv;
      if (mx != 0 && my != 0) { tx = tx * 11 / 16; ty = ty * 11 / 16; }
      moveSmooth(tx, ty);
      if (mx != 0 || my != 0) { if (lock < 0) { fx = mx; fy = my; } pAnim++; }
      if (rollReq && st >= CROLL[cls]) {
        rollT = 10; st -= CROLL[cls]; stDelay = 25; atkT = 0;
        rdx = mx; rdy = my;
        if (mx == 0 && my == 0) { rdx = -fx; rdy = -fy; if (rdx == 0 && rdy == 0) rdx = faceL ? 1 : -1; }
      } else if (potReq && potions > 0 && hp < maxHp()) {
        int heal = CPOT[cls];
        hp = Math.min(maxHp(), hp + heal); potions--; drinkT = 14;
        float_(px, py - 26, "+" + heal, 0x66FF88);
      } else if (atkReq) {
        int b = nearBonfire();
        if (b >= 0) { bfIdx = b; state = ST_BONFIRE; menuSel = 0; }
        else if (nearLever()) {
          gate[cur] = true;
          say("Se abre una reja", false, 80);
          saveSlot(curSlot);
        } else if (!guard && atkT == 0 && st >= CATKCOST[cls]) {
          atkT = CATKLEN[cls]; hitDone = false; st -= CATKCOST[cls]; stDelay = 25;
          if (CREACH[cls] > 0) { pvx = fx * 22; pvy = fy * 22; }
        }
      }
    }
    if (lock >= 0) {
      fx = sg(ex[lock] - px); fy = sg(ey[lock] - py);
      if (fx == 0 && fy == 0) fy = 1;
    }
    if (rollT > 0) { if (rdx != 0) faceL = rdx < 0; }
    else if (fx != 0) faceL = fx < 0;

    if (atkT > 0) {
      int p = CATKLEN[cls] - atkT;
      if (p == CHIT[cls] && !hitDone) {
        hitDone = true;
        if (cls == 4) firePlayerArrow(); else meleeHit();
      }
      atkT--;
    }
    if (stDelay > 0) stDelay--;
    else if (st < 100) st += guard ? 1 : 2;
    if (st > 100) st = 100;
    if (hurtT > 0) hurtT--;
    if (iT > 0) iT--;
    if (cls == 2 && hp < maxHp()) { regenT++; if (regenT >= 45) { regenT = 0; hp++; } }
    if (bsSouls > 0 && bsMap == cur && Math.abs(px - bsx) + Math.abs(py - bsy) < 20) {
      souls += bsSouls; bsSouls = 0;
      say("Almas recuperadas", false, 60);
    }
    char tc = tileAt(px / T, py / T);
    if (tc == 'N' && cur + 1 < MAPS.length) enterMap(cur + 1, false);
    else if (tc == 'V' && cur > 0) enterMap(cur - 1, true);
  }

  void updateCamera() {
    int tx = Math.max(0, Math.min(px - W / 2, MW * T - W));
    int ty = Math.max(0, Math.min(py - H / 2, MH * T - H));
    camx += (tx - camx) / 4; camy += (ty - camy) / 4;
    if (Math.abs(tx - camx) < 2) camx = tx;
    if (Math.abs(ty - camy) < 2) camy = ty;
  }

  void updatePlay() {
    if (dead) {
      deadT--;
      if (deadT <= 0) respawn();
    } else {
      updatePlayer();
    }
    updateEnemies(); updateArrows(); updatePArrows(); updateDrops(); updateFloats();
    updateCamera();
    if (msgT > 0) msgT--;
    if (winT > 0) { winT--; if (winT == 0) say("Completaste Souls E72. ¡Sigue farmeando!", false, 300); }
  }

  // =====================================================================
  //  TECLAS
  // =====================================================================
  boolean isBackKey(int kc) {
    return kc == -6 || kc == -7 || kc == -8 || kc == -11 || kc == 42 || kc == 'p' || kc == 'P';
  }

  protected void keyPressed(int kc) { handleKey(kc, true); }
  protected void keyRepeated(int kc) { handleKey(kc, false); }

  void handleKey(int kc, boolean first) {
    int ga = 0;
    try { ga = getGameAction(kc); } catch (Exception e) {}
    if (ga == UP) kUp = true;
    else if (ga == DOWN) kDown = true;
    else if (ga == LEFT) kLeft = true;
    else if (ga == RIGHT) kRight = true;
    if (!first) return;
    if (ga == FIRE) { kOk = true; atkReq = true; }
    if (isBackKey(kc)) kBack = true;
    if (kc == 35) kInv = true;
    int c = kc;
    if (c >= 'A' && c <= 'Z') c += 32;
    if (c == '1' || c == 'q' || ga == GAME_A) rollReq = true;
    else if (c == '3' || c == 'e' || ga == GAME_B) lockReq = true;
    else if (c == '7' || c == 'r' || ga == GAME_C) potReq = true;
    else if (c == '9' || c == 'w' || ga == GAME_D) blockHeld = true;
  }

  protected void keyReleased(int kc) {
    int ga = 0;
    try { ga = getGameAction(kc); } catch (Exception e) {}
    int c = kc;
    if (c >= 'A' && c <= 'Z') c += 32;
    if (c == '9' || c == 'w' || ga == GAME_D) blockHeld = false;
  }

  protected void hideNotify() {
    if (state == ST_PLAY) kBack = true;
  }

  void clearKeys() {
    kUp = false; kDown = false; kLeft = false; kRight = false; kOk = false; kBack = false; kInv = false;
    atkReq = false; rollReq = false; lockReq = false; potReq = false;
  }

  // =====================================================================
  //  MENUS (logica)
  // =====================================================================
  int[] tid = new int[5];
  int tcount;

  void openTitle() {
    for (int q = 0; q < 8; q++) spr[q] = null;
    plFr = null; prevFr = null;
    refreshSlots();
    state = ST_TITLE; menuSel = 0;
    boolean any = slotUsed[0] || slotUsed[1] || slotUsed[2];
    tcount = 0;
    if (any) tid[tcount++] = 0;
    tid[tcount++] = 1;
    if (any) tid[tcount++] = 2;
    tid[tcount++] = 3;
    tid[tcount++] = 4;
  }

  String[] titleItems() {
    String[] it = new String[tcount];
    for (int i = 0; i < tcount; i++) {
      int id = tid[i];
      it[i] = id == 0 ? "Continuar" : (id == 1 ? "Nueva partida" : (id == 2 ? "Cargar partida" : (id == 3 ? "Opciones" : "Salir")));
    }
    return it;
  }

  void nav(int n) {
    if (kUp) menuSel = (menuSel + n - 1) % n;
    if (kDown) menuSel = (menuSel + 1) % n;
  }

  void openClass() {
    state = ST_CLASS; selClass = 0; rebuildPreview();
  }

  void rebuildPreview() {
    prevFr = null;
    prevFr = Art.buildAt(selClass, 0, 0, 7, new int[] { 0, 1, 2, 3, 4, 5, 6, 7 });
  }

  void openSlots(int mode, int from) {
    refreshSlots();
    slotMode = mode; slotsFrom = from; state = ST_SLOTS; menuSel = 0;
  }

  void openOpt(int from) { optFrom = from; state = ST_OPT; menuSel = 0; }

  String[] bfItems() {
    String[] it = new String[6];
    it[0] = "Descansar (guarda)";
    it[1] = "Subir nivel (" + lvCost() + " almas)";
    it[2] = wLvl >= 9 ? "Arma al máximo" : "Mejorar arma +" + (wLvl + 1) + " (" + upGold(wLvl) + " oro, " + upIron(wLvl) + " hierro)";
    it[3] = aLvl >= 9 ? "Armadura al máximo" : "Mejorar armadura +" + (aLvl + 1) + " (" + upGold(aLvl) + " oro, " + upIron(aLvl) + " hierro)";
    it[4] = "Comprar poción (30 oro)";
    it[5] = "Salir";
    return it;
  }

  void doBonfire(int sel) {
    switch (sel) {
      case 0:
        restAt(bfIdx); state = ST_PLAY; break;
      case 1:
        if (souls >= lvCost()) {
          souls -= lvCost(); level++; hp = maxHp();
          say("¡Nivel " + level + "!", false, 70);
        } else say("Faltan almas", false, 60);
        break;
      case 2:
        if (wLvl >= 9) say("Ya es perfecta", false, 60);
        else if (gold >= upGold(wLvl) && iron >= upIron(wLvl)) {
          gold -= upGold(wLvl); iron -= upIron(wLvl); wLvl++; buildPlayer();
          say("Arma mejorada +" + wLvl, false, 70);
        } else say("Faltan oro o hierro", false, 60);
        break;
      case 3:
        if (aLvl >= 9) say("Ya es perfecta", false, 60);
        else if (gold >= upGold(aLvl) && iron >= upIron(aLvl)) {
          gold -= upGold(aLvl); iron -= upIron(aLvl); aLvl++; hp = Math.min(maxHp(), hp + 8); buildPlayer();
          say("Armadura mejorada +" + aLvl, false, 70);
        } else say("Faltan oro o hierro", false, 60);
        break;
      case 4:
        if (potions >= 9) say("Ya llevas 9", false, 60);
        else if (gold >= 30) { gold -= 30; potions++; say("Compraste una poción", false, 60); }
        else say("Faltan 30 de oro", false, 60);
        break;
      default:
        state = ST_PLAY; break;
    }
  }

  void updateMenus() {
    switch (state) {
      case ST_TITLE:
        nav(tcount);
        if (kOk) {
          int id = tid[menuSel];
          if (id == 0) {
            int s = lastSlot;
            if (s < 0 || s > 2 || !slotUsed[s]) { s = 0; for (int i = 0; i < 3; i++) if (slotUsed[i]) { s = i; break; } }
            loadSlot(s);
          } else if (id == 1) openClass();
          else if (id == 2) openSlots(1, ST_TITLE);
          else if (id == 3) openOpt(ST_TITLE);
          else quit();
        }
        break;
      case ST_CLASS:
        if (kLeft) { selClass = (selClass + 4) % 5; rebuildPreview(); }
        if (kRight) { selClass = (selClass + 1) % 5; rebuildPreview(); }
        if (kOk) startNew(selClass);
        else if (kBack) { prevFr = null; openTitle(); }
        break;
      case ST_PAUSE:
        nav(7);
        if (kBack) state = ST_PLAY;
        else if (kOk) {
          switch (menuSel) {
            case 0: state = ST_PLAY; break;
            case 1: invFrom = ST_PAUSE; state = ST_INV; break;
            case 2: openSlots(0, ST_PAUSE); break;
            case 3: openSlots(1, ST_PAUSE); break;
            case 4: openOpt(ST_PAUSE); break;
            case 5: openTitle(); break;
            default: quit(); break;
          }
        }
        break;
      case ST_SLOTS:
        nav(4);
        if (kBack) { if (slotsFrom == ST_TITLE) openTitle(); else { state = ST_PAUSE; menuSel = 0; } }
        else if (kOk) {
          if (menuSel == 3) { if (slotsFrom == ST_TITLE) openTitle(); else { state = ST_PAUSE; menuSel = 0; } }
          else if (slotMode == 0) {
            saveSlot(menuSel);
            say("Partida guardada en ranura " + (menuSel + 1), false, 80);
            state = ST_PAUSE; menuSel = 0;
          } else if (slotUsed[menuSel]) {
            loadSlot(menuSel);
          }
        }
        break;
      case ST_OPT:
        nav(4);
        if (kBack) { saveOptions(); if (optFrom == ST_TITLE) openTitle(); else { state = ST_PAUSE; menuSel = 0; } }
        else if (kOk) {
          if (menuSel == 0) { optVib = !optVib; if (optVib) buzz(80); }
          else if (menuSel == 1) optHard = !optHard;
          else if (menuSel == 2) state = ST_CTRL;
          else { saveOptions(); if (optFrom == ST_TITLE) openTitle(); else { state = ST_PAUSE; menuSel = 0; } }
        }
        break;
      case ST_CTRL:
        if (kOk || kBack) { state = ST_OPT; menuSel = 2; }
        break;
      case ST_INV:
        if (kOk && potions > 0 && hp < maxHp()) {
          hp = Math.min(maxHp(), hp + CPOT[cls]); potions--;
          say("Bebes una poción", false, 40);
        }
        if (kBack || kInv) { if (invFrom == ST_PLAY) state = ST_PLAY; else { state = ST_PAUSE; menuSel = 1; } }
        break;
      case ST_BONFIRE:
        nav(6);
        if (kBack) state = ST_PLAY;
        else if (kOk) doBonfire(menuSel);
        break;
      default:
        break;
    }
  }

  // =====================================================================
  //  DIBUJO
  // =====================================================================
  static final int GOLD = 0xDDBB44;

  void box(Graphics g, int x, int y, int w, int h) {
    g.setColor(0x0E0E16); g.fillRect(x, y, w, h);
    g.setColor(0x887755); g.drawRect(x, y, w - 1, h - 1);
    g.setColor(0x3A3446); g.drawRect(x + 2, y + 2, w - 5, h - 5);
  }

  void text(Graphics g, String s, int x, int y, int col, int anchor) {
    g.setColor(0x000000); g.drawString(s, x + 1, y + 1, anchor);
    g.setColor(col); g.drawString(s, x, y, anchor);
  }

  void menuList(Graphics g, String title, String[] items, int sel, int x, int y, int w) {
    int rows = items.length;
    int h = 34 + rows * 20;
    box(g, x, y, w, h);
    Font big = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    g.setFont(big);
    text(g, title, x + w / 2, y + 8, GOLD, Graphics.TOP | Graphics.HCENTER);
    g.setFont(Font.getDefaultFont());
    for (int i = 0; i < rows; i++) {
      int yy = y + 30 + i * 20;
      if (i == sel) { g.setColor(0x3A2E58); g.fillRect(x + 6, yy - 2, w - 12, 19); g.setColor(GOLD); g.drawRect(x + 6, yy - 2, w - 13, 18); }
      text(g, items[i], x + 14, yy + 1, i == sel ? 0xFFFFFF : 0xAAAAAA, Graphics.TOP | Graphics.LEFT);
    }
  }

  void bar(Graphics g, int x, int y, int w, int h, int v, int max, int col, int back) {
    g.setColor(0x000000); g.fillRect(x - 1, y - 1, w + 2, h + 2);
    g.setColor(back); g.fillRect(x, y, w, h);
    int f = max > 0 ? w * v / max : 0;
    if (f > w) f = w;
    g.setColor(col); g.fillRect(x, y, f, h);
    g.setColor(shade(col, 50)); g.drawLine(x, y, x + f - 1 > x ? x + f - 1 : x, y);
  }

  void blit(Graphics g, Image im, int fs, int cx, int feetY, boolean left) {
    int w = im.getWidth(), hh = im.getHeight();
    int fxo = Art.OXU * Art.SC[fs] / 4, fyo = Art.OYU * Art.SC[fs] / 4;
    if (left) g.drawRegion(im, 0, 0, w, hh, Sprite.TRANS_MIRROR, cx - (w - fxo), feetY - fyo, Graphics.TOP | Graphics.LEFT);
    else g.drawImage(im, cx - fxo, feetY - fyo, Graphics.TOP | Graphics.LEFT);
  }

  int playerPose() {
    if (rollT > 0) return Art.ROLL;
    if (atkT > 0) { int p = CATKLEN[cls] - atkT; return p < CHIT[cls] ? Art.WIND : Art.SWING; }
    if (guardOn()) return Art.GUARD;
    if (Math.abs(pvx) + Math.abs(pvy) > 6) return Art.WALK0 + ((pAnim / 3) & 3);
    return Art.IDLE0 + ((tick / 24) & 1);
  }

  void drawPlayer(Graphics g, int cx, int cy) {
    if (dead && deadT < 70 && (deadT & 2) == 0) return;
    if (dead && deadT < 40) return;
    if (iT > 0 && !dead && (iT & 2) == 0 && rollT == 0) return;
    g.setColor(0x0A0A0E); g.fillArc(cx - 8, cy + 3, 16, 6, 0, 360);
    blit(g, plFr[playerPose()], cls, cx, cy + 6, faceL);
    if (hurtT > 0 && rollT == 0) { g.setColor(0xFFFFFF); g.fillRect(cx - 1, cy - 12, 3, 3); }
  }

  void drawEnemy(Graphics g, int i) {
    if (ehp[i] <= 0 && eDie[i] <= 0) return;
    if (eDie[i] > 0 && (eDie[i] & 2) == 0) return;
    int t = eT[i], h = eh[i];
    int sx = ex[i] - camx, sy = ey[i] - camy;
    if (sx < -40 || sx > W + 40 || sy < -40 || sy > H + 50) return;
    Image[] fr = spr[t];
    if (fr == null) return;
    int pose;
    if (eD[i] > 0) pose = Art.ROLL;
    else if (eS[i] > 0) pose = Art.SWING;
    else if (eW[i] > 0) pose = Art.WIND;
    else if (eHit[i] > 0) pose = Art.IDLE0 + 1;
    else if (Math.abs(evx[i]) + Math.abs(evy[i]) > 8) pose = Art.WALK0 + ((eAnim[i] / 4) & 3);
    else pose = Art.IDLE0 + (((tick + i * 7) / 26) & 1);
    int jit = eHit[i] > 0 ? ((eHit[i] & 1) == 0 ? 2 : -2) : 0;
    g.setColor(0x0A0A0E); g.fillArc(sx - h - 2, sy + h - 3, 2 * h + 4, 7, 0, 360);
    blit(g, fr[pose], ESTYLE[t], sx + jit, sy + h, px < ex[i]);
    if (eW[i] > 0 && (eMode[i] == 2 || eMode[i] == 4)) {
      int rr = 60 + h;
      g.setColor(eMode[i] == 2 ? 0xFF4422 : 0xFF9922);
      if (((tick / 3) & 1) == 0) g.drawArc(sx - rr, sy - rr / 2, 2 * rr, rr, 0, 360);
      else g.drawArc(sx - rr + 2, sy - rr / 2 + 1, 2 * rr - 4, rr - 2, 0, 360);
      g.setColor(0xFF4422); g.fillRect(sx - 2, sy - h - 38, 4, 7); g.fillRect(sx - 2, sy - h - 29, 4, 3);
    } else if (eW[i] > 0 && eMode[i] == 1) { g.setColor(0xFF6600); g.fillRect(sx - 2, sy - h - 38, 4, 7); g.fillRect(sx - 2, sy - h - 29, 4, 3); }
    else if (eW[i] > 0 && t != 1 && t != 4) { g.setColor(0xFFDD00); g.fillRect(sx - 2, sy - h - 34, 4, 7); g.fillRect(sx - 2, sy - h - 25, 4, 3); }
    if (eB[i] > 0) {
      int rr = (10 - eB[i]) * 7 + 8 + h;
      g.setColor(0xFFCC44); g.drawArc(sx - rr, sy - rr / 2, 2 * rr, rr, 0, 360);
      g.setColor(0xFF6622); g.drawArc(sx - rr + 3, sy - rr / 2 + 2, 2 * rr - 6, rr - 4, 0, 360);
    }
    if (eRg[i] > 0 && eDie[i] == 0) { g.setColor(0xFF2200); g.fillRect(sx - 8 + ((tick / 2) & 3) * 4, sy - h - 40 - (tick & 3), 3, 4); g.fillRect(sx + 4 - ((tick / 3) & 3) * 3, sy - h - 38 - ((tick / 2) & 3), 3, 4); }
    if (!isBoss(t) && eDie[i] == 0) {
      int bw = 2 * h + 4;
      g.setColor(0x000000); g.fillRect(sx - bw / 2 - 1, sy - h - 24, bw + 2, 4);
      g.setColor(0xCC2222); g.fillRect(sx - bw / 2, sy - h - 23, bw * ehp[i] / emax[i], 2);
    }
    if (i == lock) {
      g.setColor(0xFFFFFF);
      g.drawRect(sx - h - 3, sy - h - 3, 2 * h + 6, 2 * h + 6);
    }
  }

  void drawDrops(Graphics g) {
    for (int i = 0; i < ND; i++) {
      if (lL[i] <= 0) continue;
      if (lL[i] < 120 && (lL[i] & 4) == 0) continue;
      int sx = lx[i] - camx, sy = ly[i] - camy + ((((tick / 5) + i) & 3) == 0 ? -1 : 0);
      if (lt[i] == 0) {
        g.setColor(0x7A5A10); g.fillArc(sx - 3, sy - 3, 8, 8, 0, 360);
        g.setColor(0xFFD84A); g.fillArc(sx - 3, sy - 4, 7, 7, 0, 360);
        g.setColor(0xFFF2A0); g.fillRect(sx - 1, sy - 3, 2, 2);
      } else if (lt[i] == 1) {
        g.setColor(0x000000); g.fillRect(sx - 3, sy - 5, 7, 9);
        g.setColor(0xDD3344); g.fillRect(sx - 2, sy - 2, 5, 6);
        g.setColor(0xCCCCDD); g.fillRect(sx - 1, sy - 5, 3, 3);
        g.setColor(0xFF99AA); g.fillRect(sx - 1, sy - 1, 1, 2);
      } else {
        g.setColor(0x000000); g.fillTriangle(sx - 5, sy + 3, sx + 5, sy + 3, sx, sy - 5);
        g.setColor(0xB8C0D0); g.fillTriangle(sx - 4, sy + 2, sx + 4, sy + 2, sx, sy - 4);
        g.setColor(0xFFFFFF); g.fillRect(sx - 1, sy - 1, 2, 2);
      }
    }
  }

  void drawWorld(Graphics g) {
    int cx = camx, cy = camy;
    g.setColor(0x000000); g.fillRect(0, 0, W, H);
    int set = cur * 5;
    int wallA = set + 3, wallB = set + 4;
    for (int y = cy / T; y <= (cy + H) / T; y++) {
      for (int x = cx / T; x <= (cx + W) / T; x++) {
        char tc = tileAt(x, y);
        int sx0 = x * T - cx, sy0 = y * T - cy;
        if (tc == '#') {
          g.drawImage(tiles[((x + y) & 1) == 0 ? wallA : wallB], sx0, sy0, Graphics.TOP | Graphics.LEFT);
        } else {
          int k = ((x * 7 + y * 13) % 7 == 0) ? 2 : (((x + y) & 1) == 0 ? 0 : 1);
          g.drawImage(tiles[set + k], sx0, sy0, Graphics.TOP | Graphics.LEFT);
          if (tc == 'G' && !gate[cur]) {
            g.setColor(0x000000); g.fillRect(sx0 + 1, sy0, 14, T);
            g.setColor(0x7A5A30); g.fillRect(sx0 + 2, sy0, 3, T); g.fillRect(sx0 + 7, sy0, 3, T); g.fillRect(sx0 + 12, sy0, 3, T);
            g.setColor(0xA08050); g.drawLine(sx0, sy0 + 5, sx0 + 15, sy0 + 5); g.drawLine(sx0, sy0 + 11, sx0 + 15, sy0 + 11);
          } else if (tc == 'D' && !bossDead[cur]) {
            g.setColor(0x000000); g.fillRect(sx0, sy0, T, T);
            g.setColor(0x4A3018); g.fillRect(sx0 + 1, sy0 + 1, T - 2, T - 2);
            g.setColor(0x2A1A0C); g.drawLine(sx0 + 8, sy0 + 1, sx0 + 8, sy0 + T - 2);
            g.setColor(0x9898A8); g.fillRect(sx0 + 1, sy0 + 4, T - 2, 2); g.fillRect(sx0 + 1, sy0 + 10, T - 2, 2);
            g.setColor(0xE0B83A); g.fillRect(sx0 + 6, sy0 + 7, 4, 3);
          } else if (tc == 'N' || tc == 'V') {
            g.setColor(0x040406); g.fillRect(sx0, sy0, T, T);
            g.setColor(0x887755); g.drawRect(sx0, sy0, T - 1, T - 1);
            g.setColor(0x554430); g.drawRect(sx0 + 2, sy0 + 2, T - 5, T - 5);
          }
        }
      }
    }
    if (hasLever) {
      int sx = lvx - cx, sy = lvy - cy;
      g.setColor(0x000000); g.fillRect(sx - 3, sy - 2, 7, 8);
      g.setColor(0x777788); g.fillRect(sx - 1, sy - 4, 3, 9);
      g.setColor(0xDD4444); g.fillRect(sx - 3, gate[cur] ? sy + 2 : sy - 7, 7, 4);
    }
    for (int i = 0; i < nb; i++) {
      int sx = bnx[i] - cx, sy = bny[i] - cy;
      g.setColor(0x3A2814); g.fillArc(sx - 15, sy - 4, 30, 14, 0, 360);
      g.setColor(0x2A1A0C); g.fillArc(sx - 11, sy - 1, 22, 9, 0, 360);
      g.setColor(0x553311); g.fillRect(sx - 7, sy + 2, 14, 4);
      g.setColor(0x7A4A24); g.fillRect(sx - 6, sy + 1, 12, 2);
      int fh = 9 + (((tick / 3) + i) % 3) * 2;
      g.setColor(0xFF6A00); g.fillArc(sx - 5, sy + 3 - fh, 10, fh, 0, 360);
      g.setColor(0xFFAA22); g.fillArc(sx - 3, sy + 3 - fh * 2 / 3, 6, fh * 2 / 3, 0, 360);
      g.setColor(0xFFEE88); g.fillArc(sx - 1, sy + 2 - fh / 3, 3, fh / 3, 0, 360);
      if (((tick / 4) + i) % 4 == 0) { g.setColor(0xFFCC44); g.fillRect(sx + 3 - ((tick / 4) % 3), sy - fh - 1, 1, 1); }
    }
    if (bsSouls > 0 && bsMap == cur) {
      int sx = bsx - cx, sy = bsy - cy;
      g.setColor(0x1A3A66); g.fillArc(sx - 8, sy - 8, 16, 16, 0, 360);
      g.setColor(0x66BBFF); g.fillArc(sx - 5, sy - 5 - ((tick / 6) & 1), 10, 10, 0, 360);
      g.setColor(0xFFFFFF); g.fillRect(sx - 1, sy - 3, 2, 2);
    }
    drawDrops(g);
    for (int i = 0; i < ne; i++) if (ey[i] < py) drawEnemy(g, i);
    drawPlayer(g, px - cx, py - cy);
    for (int i = 0; i < ne; i++) if (ey[i] >= py) drawEnemy(g, i);
    for (int i = 0; i < na; i++) {
      int sx = arX[i] - cx, sy = arY[i] - cy;
      if (arT[i] == 1) {
        g.setColor(0xFF5500); g.fillArc(sx - 4, sy - 4, 9, 9, 0, 360);
        g.setColor(0xFFAA22); g.fillArc(sx - 2, sy - 3, 6, 6, 0, 360);
        g.setColor(0xFFEE99); g.fillRect(sx - 1, sy - 2, 2, 2);
        g.setColor(0xAA3300); g.drawLine(sx, sy, sx - arVX[i] * 3, sy - arVY[i] * 3);
      } else {
        g.setColor(0xCC9955); g.drawLine(sx, sy, sx - arVX[i] * 2, sy - arVY[i] * 2);
        g.setColor(0xFFFFFF); g.fillRect(sx, sy, 2, 2);
      }
    }
    for (int i = 0; i < npa; i++) {
      int sx = paX[i] - cx, sy = paY[i] - cy;
      g.setColor(0xE8D8A0); g.drawLine(sx, sy, sx - paVX[i] * 2, sy - paVY[i] * 2);
      g.setColor(0xFFFFFF); g.fillRect(sx, sy, 2, 2);
    }
    for (int i = 0; i < NF; i++) {
      if (ftT[i] > 0 && (ftT[i] > 6 || (ftT[i] & 1) == 0)) text(g, ftS[i], ftX[i] - cx, ftY[i] - cy, ftC[i], Graphics.TOP | Graphics.HCENTER);
    }
  }

  void drawHud(Graphics g) {
    int mh = maxHp();
    int bw = Math.min(mh + 20, 150);
    bar(g, 6, 6, bw, 9, hp, mh, 0xCC2222, 0x3A0A0A);
    g.setColor(0xFFFFFF);
    text(g, hp + "/" + mh, 9, 4, 0xFFFFFF, Graphics.TOP | Graphics.LEFT);
    bar(g, 6, 18, 100, 5, st, 100, 0x33BB44, 0x0A3A10);
    // iconos
    g.setColor(0xFFD84A); g.fillArc(7, 28, 9, 9, 0, 360);
    text(g, "" + gold, 20, 26, 0xFFFFFF, Graphics.TOP | Graphics.LEFT);
    g.setColor(0xDD3344); g.fillRect(62, 28, 6, 9); g.setColor(0xCCCCDD); g.fillRect(63, 26, 4, 3);
    text(g, "x" + potions, 71, 26, 0xFFFFFF, Graphics.TOP | Graphics.LEFT);
    g.setColor(0xB8C0D0); g.fillTriangle(99, 36, 107, 36, 103, 28);
    text(g, "" + iron, 111, 26, 0xFFFFFF, Graphics.TOP | Graphics.LEFT);
    text(g, "Nv " + level + "  " + CNAME[cls], W - 6, 4, 0xFFFFFF, Graphics.TOP | Graphics.RIGHT);
    text(g, "Almas " + souls, W - 6, 18, 0x88DDFF, Graphics.TOP | Graphics.RIGHT);
    if (!dead && nearBonfire() >= 0) text(g, "OK: hoguera", W / 2, H - 22, 0xFFFFFF, Graphics.TOP | Graphics.HCENTER);
    else if (!dead && nearLever()) text(g, "OK: palanca", W / 2, H - 22, 0xFFFFFF, Graphics.TOP | Graphics.HCENTER);
    for (int i = 0; i < ne; i++) {
      if (isBoss(eT[i]) && ehp[i] > 0 && Math.abs(ex[i] - px) + Math.abs(ey[i] - py) < 190) {
        bar(g, 30, H - 14, W - 60, 7, ehp[i], emax[i], eRg[i] > 0 ? 0xFF6600 : 0xCC2222, 0x3A0A0A);
        text(g, BNAME[eT[i]], W / 2, H - 30, 0xFFFFFF, Graphics.TOP | Graphics.HCENTER);
      }
    }
    Font big = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
    if (dead) {
      g.setColor(0x000000); g.fillRect(0, H / 2 - 24, W, 48);
      g.setFont(big);
      text(g, "HAS MUERTO", W / 2, H / 2 - 10, 0xCC2222, Graphics.TOP | Graphics.HCENTER);
      g.setFont(Font.getDefaultFont());
    } else if (msgT > 0) {
      if (msgBig) {
        g.setColor(0x000000); g.fillRect(0, H / 2 - 24, W, 48);
        g.setFont(big);
        text(g, msg, W / 2, H / 2 - 10, GOLD, Graphics.TOP | Graphics.HCENTER);
        g.setFont(Font.getDefaultFont());
      } else {
        text(g, msg, W / 2, H - 52, GOLD, Graphics.TOP | Graphics.HCENTER);
      }
    }
  }

  void drawSky(Graphics g) {
    for (int y = 0; y < H; y += 4) {
      int t = y * 40 / H;
      g.setColor(((10 + t) << 16) | ((8 + t / 2) << 8) | (24 + t));
      g.fillRect(0, y, W, 4);
    }
    for (int i = 0; i < 36; i++) {
      g.setColor((((tick / 12) + i) % 9 == 0) ? 0x666677 : 0xCCCCDD);
      g.fillRect(starX[i], starY[i], 1, 1);
    }
    g.setColor(0xE8E4C8); g.fillArc(W - 70, 22, 34, 34, 0, 360);
    g.setColor(0x0E0A1C); g.fillArc(W - 60, 18, 32, 32, 0, 360);
    // suelo y silueta
    g.setColor(0x08060E); g.fillRect(0, H - 44, W, 44);
    g.setColor(0x0E0C18);
    g.fillTriangle(0, H - 44, 60, H - 70, 120, H - 44);
    g.fillTriangle(200, H - 44, 262, H - 78, 320, H - 44);
    g.fillRect(150, H - 88, 22, 44); g.fillTriangle(146, H - 88, 176, H - 88, 161, H - 104);
  }

  void drawFire(Graphics g, int sx, int sy) {
    g.setColor(0x3A2814); g.fillArc(sx - 16, sy - 4, 32, 14, 0, 360);
    g.setColor(0x553311); g.fillRect(sx - 8, sy + 1, 16, 4);
    int fh = 12 + ((tick / 3) % 3) * 3;
    g.setColor(0xFF6A00); g.fillArc(sx - 6, sy + 3 - fh, 12, fh, 0, 360);
    g.setColor(0xFFAA22); g.fillArc(sx - 4, sy + 3 - fh * 2 / 3, 8, fh * 2 / 3, 0, 360);
    g.setColor(0xFFEE88); g.fillArc(sx - 2, sy + 2 - fh / 3, 4, fh / 3, 0, 360);
  }

  void drawTitle(Graphics g) {
    drawSky(g);
    drawFire(g, 40, H - 16); drawFire(g, W - 40, H - 16);
    Font big = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
    g.setFont(big);
    text(g, "SOULS E72", W / 2, 14, GOLD, Graphics.TOP | Graphics.HCENTER);
    g.setFont(Font.getDefaultFont());
    text(g, "una aventura de bolsillo", W / 2, 38, 0x8888AA, Graphics.TOP | Graphics.HCENTER);
    String[] it = titleItems();
    menuList(g, "Menú", it, menuSel, W / 2 - 80, 58, 160);
  }

  void drawClassSelect(Graphics g) {
    drawSky(g);
    Font big = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    g.setFont(big);
    text(g, "Elige tu héroe", W / 2, 6, GOLD, Graphics.TOP | Graphics.HCENTER);
    g.setFont(Font.getDefaultFont());
    box(g, 10, 28, W - 20, H - 60);
    int[] seq = { 0, 0, 1, 1, 2, 3, 4, 5, 2, 3, 4, 5, 6, 6, 7, 7 };
    int pose = seq[(tick / 6) & 15];
    if (prevFr != null && prevFr[pose] != null) {
      Image im = prevFr[pose];
      int w = im.getWidth(), hh = im.getHeight();
      int fxo = Art.OXU * 7 / 4, fyo = Art.OYU * 7 / 4;
      g.setColor(0x1A1626); g.fillArc(W / 2 - 100 - 30, 152, 60, 14, 0, 360);
      g.drawImage(im, W / 2 - 100 - fxo, 160 - fyo, Graphics.TOP | Graphics.LEFT);
    }
    text(g, CNAME[selClass], W / 2 + 40, 42, 0xFFFFFF, Graphics.TOP | Graphics.HCENTER);
    text(g, CDESC[selClass], W / 2 + 40, 60, 0xAAAACC, Graphics.TOP | Graphics.HCENTER);
    int bx = W / 2 - 4, by = 84;
    text(g, "Vida", bx, by, 0xCCCCCC, Graphics.TOP | Graphics.LEFT);
    bar(g, bx + 56, by + 3, 80, 7, CHP[selClass], 140, 0xCC2222, 0x3A0A0A);
    text(g, "Daño", bx, by + 16, 0xCCCCCC, Graphics.TOP | Graphics.LEFT);
    bar(g, bx + 56, by + 19, 80, 7, CDMG[selClass], 14, 0xDD9922, 0x3A2A0A);
    text(g, "Velocidad", bx, by + 32, 0xCCCCCC, Graphics.TOP | Graphics.LEFT);
    bar(g, bx + 56, by + 35, 80, 7, CSPD[selClass], 40, 0x33BB44, 0x0A3A10);
    text(g, "Alcance", bx, by + 48, 0xCCCCCC, Graphics.TOP | Graphics.LEFT);
    bar(g, bx + 56, by + 51, 80, 7, selClass == 4 ? 40 : CREACH[selClass], 40, 0x4488DD, 0x0A1A3A);
    text(g, "<  " + (selClass + 1) + "/5  >", W / 2, H - 52, GOLD, Graphics.TOP | Graphics.HCENTER);
    text(g, "OK: elegir    *: volver", W / 2, H - 26, 0x8888AA, Graphics.TOP | Graphics.HCENTER);
  }

  void drawSlots(Graphics g) {
    String[] it = new String[4];
    for (int i = 0; i < 3; i++) it[i] = (i + 1) + ". " + slotInfo[i];
    it[3] = "Volver";
    menuList(g, slotMode == 0 ? "Guardar partida" : "Cargar partida", it, menuSel, 20, 40, W - 40);
  }

  void drawOpt(Graphics g) {
    String[] it = { "Vibración: " + (optVib ? "Sí" : "No"), "Dificultad: " + (optHard ? "Difícil" : "Normal"), "Ver controles", "Volver" };
    menuList(g, "Opciones", it, menuSel, 40, 40, W - 80);
  }

  void drawCtrl(Graphics g) {
    box(g, 12, 12, W - 24, H - 24);
    Font big = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    g.setFont(big);
    text(g, "Controles", W / 2, 18, GOLD, Graphics.TOP | Graphics.HCENTER);
    g.setFont(Font.getDefaultFont());
    String[] l = {
      "Cruceta: moverte (en menús, elegir)",
      "OK: atacar / hoguera / palanca",
      "1 (o Q): esquivar",
      "3 (o E): fijar enemigo",
      "7 (o R): beber poción",
      "9 (o W): mantener para guardia",
      "# : inventario",
      "* (o tecla izq./der.): pausa y atrás",
    };
    for (int i = 0; i < l.length; i++) text(g, l[i], 24, 42 + i * 18, 0xDDDDDD, Graphics.TOP | Graphics.LEFT);
  }

  void drawInv(Graphics g) {
    box(g, 6, 6, W - 12, H - 12);
    Font big = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    g.setFont(big);
    text(g, "Inventario", W / 2, 10, GOLD, Graphics.TOP | Graphics.HCENTER);
    g.setFont(Font.getDefaultFont());
    g.setColor(0x1A1626); g.fillArc(24, 150, 56, 14, 0, 360);
    blit(g, plFr[Art.IDLE0 + ((tick / 24) & 1)], cls, 52, 160, false);
    text(g, CNAME[cls], 52, 168, 0xFFFFFF, Graphics.TOP | Graphics.HCENTER);
    int x = 100, y = 34;
    text(g, "Nivel " + level, x, y, 0xFFFFFF, Graphics.TOP | Graphics.LEFT);
    text(g, "Vida " + hp + "/" + maxHp(), x + 100, y, 0xFF8888, Graphics.TOP | Graphics.LEFT);
    text(g, "Daño " + dmgBase(), x, y + 18, 0xFFCC66, Graphics.TOP | Graphics.LEFT);
    text(g, "Defensa " + defPct() + "%", x + 100, y + 18, 0x88BBFF, Graphics.TOP | Graphics.LEFT);
    text(g, "Arma +" + wLvl + " (" + TIERW[wLvl / 2] + ")", x, y + 40, 0xDDDDDD, Graphics.TOP | Graphics.LEFT);
    text(g, "Armadura +" + aLvl + " (" + TIERA[aLvl / 2] + ")", x, y + 58, 0xDDDDDD, Graphics.TOP | Graphics.LEFT);
    g.setColor(0x3A3446); g.drawLine(x, y + 80, W - 16, y + 80);
    g.setColor(0xFFD84A); g.fillArc(x, y + 88, 10, 10, 0, 360);
    text(g, "Oro  " + gold, x + 16, y + 86, 0xFFFFFF, Graphics.TOP | Graphics.LEFT);
    g.setColor(0xB8C0D0); g.fillTriangle(x + 110, y + 98, x + 120, y + 98, x + 115, y + 88);
    text(g, "Hierro  " + iron, x + 126, y + 86, 0xFFFFFF, Graphics.TOP | Graphics.LEFT);
    g.setColor(0xDD3344); g.fillRect(x + 2, y + 108, 7, 10);
    text(g, "Pociones  " + potions + "/9   (OK: beber)", x + 16, y + 106, 0xFFFFFF, Graphics.TOP | Graphics.LEFT);
    text(g, "Almas  " + souls, x, y + 126, 0x88DDFF, Graphics.TOP | Graphics.LEFT);
    text(g, "Enemigos derrotados  " + kills, x, y + 144, 0xAAAAAA, Graphics.TOP | Graphics.LEFT);
    if (msgT > 0) text(g, msg, W / 2, H - 38, GOLD, Graphics.TOP | Graphics.HCENTER);
    text(g, "* / #: volver", W / 2, H - 20, 0x8888AA, Graphics.TOP | Graphics.HCENTER);
  }

  void drawBonfire(Graphics g) {
    drawWorld(g);
    String[] it = bfItems();
    int w = W - 24, x = 12, y = 14;
    menuList(g, "Hoguera", it, menuSel, x, y, w);
    int by = y + 34 + it.length * 20 + 4;
    box(g, x, by, w, 34);
    text(g, "Nv " + level + "   Almas " + souls, x + 10, by + 6, 0x88DDFF, Graphics.TOP | Graphics.LEFT);
    text(g, "Oro " + gold + "   Hierro " + iron + "   Pociones " + potions, x + 10, by + 19, 0xFFFFFF, Graphics.TOP | Graphics.LEFT);
    if (msgT > 0) text(g, msg, W / 2, H - 20, GOLD, Graphics.TOP | Graphics.HCENTER);
  }

  void drawFrame(Graphics g) {
    switch (state) {
      case ST_TITLE: drawTitle(g); break;
      case ST_CLASS: drawClassSelect(g); break;
      case ST_PLAY: drawWorld(g); drawHud(g); break;
      case ST_PAUSE: {
        drawWorld(g); drawHud(g);
        String[] it = { "Continuar", "Inventario", "Guardar partida", "Cargar partida", "Opciones", "Salir al menú", "Salir del juego" };
        menuList(g, "Pausa", it, menuSel, W / 2 - 90, 20, 180);
        if (msgT > 0) text(g, msg, W / 2, H - 20, GOLD, Graphics.TOP | Graphics.HCENTER);
        break; }
      case ST_INV: drawInv(g); break;
      case ST_BONFIRE: drawBonfire(g); break;
      case ST_SLOTS: drawBackdrop(g); drawSlots(g); break;
      case ST_OPT: drawBackdrop(g); drawOpt(g); break;
      case ST_CTRL: drawBackdrop(g); drawCtrl(g); break;
      default: break;
    }
  }

  void drawBackdrop(Graphics g) {
    int from = (state == ST_SLOTS) ? slotsFrom : optFrom;
    if (from == ST_TITLE) drawSky(g);
    else { drawWorld(g); drawHud(g); }
  }

  // =====================================================================
  //  BUCLE
  // =====================================================================
  void step(Graphics g) {
    tick++;
    if (state == ST_PLAY) {
      if (kInv) { invFrom = ST_PLAY; state = ST_INV; }
      else if (kBack) { state = ST_PAUSE; menuSel = 0; blockHeld = false; }
      else updatePlay();
    } else {
      updateMenus();
      if (msgT > 0 && state != ST_PLAY) msgT--;
      if (state == ST_PLAY) camSnap();
    }
    clearKeys();
    drawFrame(g);
  }

  public void run() {
    Graphics g = getGraphics();
    while (run) {
      long t0 = System.currentTimeMillis();
      step(g);
      flushGraphics();
      long d = 40 - (System.currentTimeMillis() - t0);
      try { Thread.sleep(d > 5 ? d : 5); } catch (Exception e) {}
    }
  }
}
