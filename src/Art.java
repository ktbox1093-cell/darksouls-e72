import javax.microedition.lcdui.*;

// Dibujo de personajes caricaturescos (estilo "chibi") y prerenderizado de animaciones.
public class Art {
  static final int OL = 0x1A1420;   // color del contorno
  static final int KEY = 0x000000;  // color clave (transparente)
  static final int NP = 10;         // poses por personaje
  static final int IDLE0 = 0, WALK0 = 2, WIND = 6, SWING = 7, ROLL = 8, GUARD = 9;
  static final int W_NONE = 0, W_SWORD = 1, W_DAGGER = 2, W_MACE = 3, W_KNIFE = 4,
                   W_BOW = 5, W_CLUB = 6, W_GREAT = 7, W_STAFF = 8;

  // estilos: 0 caballero, 1 asesino, 2 clerigo, 3 ladron, 4 arquero,
  //          5 hueco, 6 arquero enemigo, 7 pesado, 8 jefe
  static final int[] SC = { 4, 4, 4, 4, 4, 4, 4, 5, 6, 4, 4, 6, 6 };
  static final int CW = 44, CH = 44, OXU = 22, OYU = 39;
  static final int[] WPN = { W_SWORD, W_DAGGER, W_MACE, W_KNIFE, W_BOW, W_NONE, W_BOW, W_CLUB, W_GREAT, W_STAFF, W_SWORD, W_STAFF, W_GREAT };
  static final int[] SKIN  = { 0xF2C9A0, 0xE8B88A, 0xF5D0A8, 0xE9BE92, 0xF0C79B, 0x8FAF7A, 0xB8B8A8, 0x9A8F8F, 0x6A4A7A, 0xB89A8A, 0xE8E4D0, 0x8A6A4A, 0x9A9AA8 };
  static final int[] TORSO = { 0x6A7AA0, 0x5A5278, 0xEDE6CE, 0x6B4A2A, 0x4F7A3A, 0x5A4A3A, 0x2E4A2E, 0x6A5A5A, 0x5A2A7A, 0x9A2A2A, 0xC8C4B0, 0x2E6A3A, 0x3A3A4E };
  static final int[] PANTS = { 0x3A3F55, 0x3A3552, 0xC8BFA0, 0x3C3226, 0x4A3E2A, 0x3A3A44, 0x2A3A2A, 0x4A4044, 0x30183F, 0x7A2020, 0xB0AC98, 0x244A2C, 0x2A2A3A };
  static final int[] BOOT  = { 0x2A2A30, 0x1A1822, 0x6B4A2A, 0x2A2018, 0x3A2A1A, 0x2A2A2A, 0x1E1E1E, 0x2E2828, 0x1A1020, 0x2A1010, 0x9A9680, 0x3A2A1A, 0x1A1A24 };
  static final int[] CAPE  = { 0xB02A2A, 0x4A4470, 0, 0, 0x2E5A2E, 0, 0, 0, 0x3A1550, 0, 0, 0x2A4A2A, 0x6A1A1A };
  static final int[] ARM   = { 0, 0x8B5A2B, 0x9AA0AC, 0x5C86D0, 0xE0B83A };
  static final int[] WCOL  = { 0x9A9AA4, 0xCFD4E0, 0x66B0FF, 0xFFD24A, 0xFF6A2A };

  static final int[] NFX = {  2,  2, -4, 0, 4, 0, -3, 5, 5,  2 };
  static final int[] FFX = { -2, -2,  4, 0,-4, 0,  2,-4,-4, -3 };
  static final int[] NLF = {  0,  0,  0, 2, 0, 0,  0, 0, 0,  0 };
  static final int[] FLF = {  0,  0,  0, 0, 0, 2,  0, 0, 0,  0 };
  static final int[] NHX = {  7,  7,  7, 7, 7, 7, -2,11,12,  5 };
  static final int[] NHY = {-11,-12,-11,-12,-11,-12,-24,-11,-9,-12 };
  static final int[] FHX = { -5, -5, -6,-4,-4,-6,  7, 2, 3,  8 };
  static final int[] FHY = {-11,-12,-12,-11,-12,-11,-14,-13,-12,-12 };
  static final int[] WVX = {  3,  3,  3, 3, 3, 3, -3, 9,10,  4 };
  static final int[] WVY = { -9, -9, -9,-9,-9,-9, -9, 4, 2,  5 };

  static Graphics fg;
  static int sc, ox, oy;

  // ---------- utilidades de dibujo (coordenadas "unidad", origen en los pies) ----------
  static int r(int v) { return (v * sc + (v >= 0 ? 2 : -2)) / 4; }
  static int X(int v) { return ox + r(v); }
  static int Y(int v) { return oy + r(v); }
  static int sz(int v) { int s = r(v); return (v != 0 && s == 0) ? 1 : s; }

  static void rect(int x, int y, int w, int h, int col) {
    int a = X(x), b = Y(y), c = X(x + w), d = Y(y + h);
    if (c <= a) c = a + 1;
    if (d <= b) d = b + 1;
    fg.setColor(col); fg.fillRect(a, b, c - a, d - b);
  }

  static void rectO(int x, int y, int w, int h, int col) {
    int a = X(x), b = Y(y), c = X(x + w), d = Y(y + h);
    if (c <= a) c = a + 1;
    if (d <= b) d = b + 1;
    fg.setColor(OL); fg.fillRect(a - 1, b - 1, c - a + 2, d - b + 2);
    fg.setColor(col); fg.fillRect(a, b, c - a, d - b);
  }

  static void oval(int cx, int cy, int rx, int ry, int col) {
    int a = X(cx - rx), b = Y(cy - ry), c = X(cx + rx), d = Y(cy + ry);
    fg.setColor(col); fg.fillArc(a, b, c - a, d - b, 0, 360);
  }

  static void ovalO(int cx, int cy, int rx, int ry, int col) {
    int a = X(cx - rx), b = Y(cy - ry), c = X(cx + rx), d = Y(cy + ry);
    fg.setColor(OL); fg.fillArc(a - 1, b - 1, c - a + 2, d - b + 2, 0, 360);
    fg.setColor(col); fg.fillArc(a, b, c - a, d - b, 0, 360);
  }

  static void tri(int x1, int y1, int x2, int y2, int x3, int y3, int col) {
    int a = X(x1), b = Y(y1), c = X(x2), d = Y(y2), e = X(x3), f = Y(y3);
    fg.setColor(col); fg.fillTriangle(a, b, c, d, e, f);
    fg.setColor(OL);
    fg.drawLine(a, b, c, d); fg.drawLine(c, d, e, f); fg.drawLine(e, f, a, b);
  }

  static void thick(int a, int b, int c, int d, int t, int col) {
    fg.setColor(col);
    int o = t / 2;
    for (int i = 0; i < t; i++)
      for (int j = 0; j < t; j++)
        fg.drawLine(a + i - o, b + j - o, c + i - o, d + j - o);
  }

  static void lineO(int x1, int y1, int x2, int y2, int th, int col) {
    int t = sz(th);
    int a = X(x1), b = Y(y1), c = X(x2), d = Y(y2);
    thick(a, b, c, d, t + 2, OL);
    thick(a, b, c, d, t, col);
  }

  static void line1(int x1, int y1, int x2, int y2, int col) {
    fg.setColor(col); fg.drawLine(X(x1), Y(y1), X(x2), Y(y2));
  }

  static void halfDome(int cx, int cy, int rad, int col) {
    int a = X(cx - rad), b = Y(cy - rad), c = X(cx + rad), d = Y(cy + rad);
    fg.setColor(col); fg.fillArc(a, b, c - a, d - b, 0, 180);
    fg.setColor(OL); fg.drawArc(a, b, c - a, d - b, 0, 180);
  }

  // ---------- armas ----------
  static void weapon(int wt, int hx, int hy, int vx, int vy, int col, boolean pull) {
    int px = -vy / 3, py = vx / 3;
    switch (wt) {
      case W_SWORD:
        lineO(hx, hy, hx + vx, hy + vy, 2, col);
        lineO(hx - px, hy - py, hx + px, hy + py, 1, 0xC89B3C);
        break;
      case W_DAGGER:
        lineO(hx, hy, hx + vx * 5 / 9, hy + vy * 5 / 9, 2, col);
        break;
      case W_KNIFE:
        lineO(hx, hy, hx + vx * 4 / 9, hy + vy * 4 / 9, 2, col);
        break;
      case W_MACE:
        lineO(hx, hy, hx + vx * 7 / 9, hy + vy * 7 / 9, 1, 0x7A4A24);
        ovalO(hx + vx * 8 / 9, hy + vy * 8 / 9, 3, 3, col);
        break;
      case W_CLUB:
        lineO(hx, hy, hx + vx, hy + vy, 3, 0x7A5028);
        ovalO(hx + vx, hy + vy, 4, 4, 0x6B4A2A);
        break;
      case W_GREAT:
        lineO(hx, hy, hx + vx * 3 / 2, hy + vy * 3 / 2, 3, col);
        lineO(hx + vx * 3 / 2, hy + vy * 3 / 2, hx + vx * 3 / 2 + vx / 6, hy + vy * 3 / 2 + vy / 6, 1, 0xFF3A3A);
        lineO(hx - px * 2, hy - py * 2, hx + px * 2, hy + py * 2, 2, 0xE0B83A);
        break;
      case W_STAFF:
        lineO(hx - vx / 3, hy - vy / 3, hx + vx * 5 / 4, hy + vy * 5 / 4, 1, 0x7A5028);
        ovalO(hx + vx * 5 / 4, hy + vy * 5 / 4, 3, 3, col);
        oval(hx + vx * 5 / 4, hy + vy * 5 / 4, 1, 1, 0xFFFFFF);
        break;
      case W_BOW: {
        int top = hy - 8, bot = hy + 8, bx = hx + 2;
        lineO(bx, top, bx + 3, hy, 1, 0x8B5A2B);
        lineO(bx + 3, hy, bx, bot, 1, 0x8B5A2B);
        int sx = pull ? hx - 5 : bx + 1;
        line1(bx, top, sx, hy, 0xE8E8E8);
        line1(sx, hy, bx, bot, 0xE8E8E8);
        if (pull) {
          line1(sx, hy, hx + 9, hy, 0xF0F0F0);
          line1(sx, hy - 1, hx + 9, hy - 1, 0xA08060);
          tri(hx + 9, hy - 2, hx + 9, hy + 2, hx + 12, hy, 0xDDDDDD);
        }
        break; }
      default:
        break;
    }
  }

  // ---------- figura completa ----------
  static void fig(int fs, int pose, int armT, int wpnT) {
    int sk = SKIN[fs], torso = TORSO[fs], pants = PANTS[fs], boot = BOOT[fs], cape = CAPE[fs];
    if (fs < 5 && armT > 0) torso = ARM[armT];
    int wcol = WCOL[wpnT];
    if (fs == 7) wcol = 0x6B4A2A;
    if (fs == 8) wcol = 0x7A3AA0;
    if (fs == 9) wcol = 0xFF8A20;
    if (fs == 10) wcol = 0x8A8A7A;
    if (fs == 11) wcol = 0x44E080;
    if (fs == 12) wcol = 0x6A7ADA;
    int wt = WPN[fs];
    boolean enemy = fs >= 5;
    if (pose == ROLL && !enemy) { drawRoll(fs, torso, sk, cape); return; }
    int p = pose;
    int lean = 0, bob = 0;
    boolean walk = p >= WALK0 && p < WALK0 + 4;
    if (p == IDLE0 + 1) bob = -1;
    if (walk && ((p - WALK0) & 1) == 1) bob = -1;
    if (p == WIND) lean = -2;
    if (p == SWING) lean = 2;
    if (p == ROLL) lean = 4;   // embestida (solo enemigos)
    if (p == GUARD) lean = 1;
    int tw = (fs == 7 || fs == 8 || fs == 11 || fs == 12) ? 10 : 8;
    int nhx = NHX[p], nhy = NHY[p] + (walk || p < 2 ? bob : 0);
    int fhx = FHX[p], fhy = FHY[p] + (walk || p < 2 ? bob : 0);
    int wvx = WVX[p], wvy = WVY[p];
    if (fs == 5) { // zombi: brazos al frente
      if (p == WIND) { nhx = 4; nhy = -25; fhx = 2; fhy = -23; }
      else if (p == SWING || p == ROLL) { nhx = 11; nhy = -8; fhx = 9; fhy = -10; }
      else { nhx = 9; nhy = -13 + bob; fhx = 7; fhy = -11 + bob; }
    }
    int sway = walk ? ((p - WALK0) & 1) * 2 : 0;

    // capa (detras)
    if (cape != 0) {
      tri(-3 + lean, -15 + bob, -11 + lean - sway, -3, -2 + lean, -6, cape);
    }
    // brazo lejano
    lineO(-2 + lean, -15 + bob, fhx, fhy, 2, torso);
    oval(fhx, fhy, 1, 1, sk);
    if (fs == 1) weapon(W_DAGGER, fhx, fhy, 4, -1, WCOL[wpnT], false);   // asesino: 2 dagas
    // piernas
    int nl = NLF[p], fl = FLF[p];
    lineO(-2, -8, FFX[p], -fl - 1, 3, pants);
    rectO(FFX[p] - 1, -3 - fl, 5, 3, boot);
    lineO(2, -8, NFX[p], -nl - 1, 3, pants);
    rectO(NFX[p] - 1, -3 - nl, 5, 3, boot);
    // torso
    rectO(-tw / 2 + lean, -16 + bob, tw, 8, torso);
    rect(-tw / 2 + lean, -10 + bob, tw, 1, 0x3A2A1A);
    if (fs == 2) { rect(lean, -15 + bob, 1, 4, 0xE0B83A); rect(-1 + lean, -14 + bob, 3, 1, 0xE0B83A); }
    if (fs == 0 && armT == 0) rect(lean - 1, -15 + bob, 2, 5, 0xB02A2A);
    if (fs == 8 || fs == 12) { rect(-tw / 2 + lean, -10 + bob, tw, 2, 0xE0B83A); }
    if (fs == 10) { for (int i = 0; i < 3; i++) rect(-tw / 2 + 1 + lean, -15 + bob + i * 2, tw - 2, 1, 0x5A5648); }
    if (fs == 11) { rect(-tw / 2 + lean, -10 + bob, tw, 1, 0x8A6A3A); rect(lean - 1, -15 + bob, 2, 3, 0x66DD88); }
    if (!enemy && armT >= 2) {
      int pc = (armT == 2) ? 0xC0C6D2 : (armT == 3 ? 0x8FB0F0 : 0xFFD866);
      ovalO(-tw / 2 + 1 + lean, -15 + bob, 3, 3, pc);
      ovalO(tw / 2 - 1 + lean, -15 + bob, 3, 3, pc);
    }
    // cabeza
    int hx = 1 + lean, hy = -21 + bob;
    ovalO(hx, hy, 5, 5, sk);
    head(fs, hx, hy, sk, armT);
    // slash
    if (p == SWING && wt != W_BOW && wt != W_NONE) {
      int slc = (wt == W_GREAT) ? 0xFFB0B0 : 0xFFFFFF;
      line1(6, -28, 17, -18, slc); line1(7, -28, 19, -17, slc);
      line1(17, -18, 20, -6, slc); line1(19, -17, 22, -7, slc);
    }
    // brazo cercano + arma
    lineO(2 + lean, -15 + bob, nhx, nhy, 2, torso);
    oval(nhx, nhy, 1, 1, sk);
    if (wt != W_NONE) {
      boolean pull = (wt == W_BOW && p == WIND);
      if (wt == W_BOW && p == WIND) { nhx = 8; nhy = -15; }
      weapon(wt, nhx, nhy, wvx, wvy, wcol, pull);
    }
    // escudo
    if (fs == 0) {
      if (p == GUARD) {
        rectO(7, -21, 7, 16, 0xB8BEC8);
        rect(9, -17, 3, 8, 0xB02A2A); rect(8, -14, 5, 2, 0xB02A2A);
      } else {
        rectO(fhx - 2, fhy - 4, 4, 9, 0xB8BEC8);
        rect(fhx - 1, fhy - 1, 2, 3, 0xB02A2A);
      }
    } else if (p == GUARD && !enemy) {
      rectO(7, -18, 5, 11, 0x8B5A2B);
      rect(8, -14, 3, 3, 0xC8A050);
    }
  }

  static void drawRoll(int fs, int torso, int sk, int cape) {
    line1(-10, -12, -17, -12, 0xCCCCCC); line1(-9, -8, -16, -8, 0xAAAAAA);
    line1(-10, -4, -15, -4, 0xCCCCCC); line1(-8, -15, -14, -16, 0xAAAAAA);
    if (cape != 0) oval(-5, -8, 5, 5, cape);
    ovalO(0, -8, 7, 7, torso);
    ovalO(4, -10, 4, 4, sk);
    if (fs == 0) halfDome(4, -10, 4, 0xB8BEC8);
    if (fs == 1) oval(4, -10, 4, 4, 0x2A2A3A);
    if (fs == 3) rect(0, -12, 8, 2, 0xD03A3A);
    if (fs == 4) tri(0, -12, 8, -12, 3, -18, 0x3E7A3E);
    if (fs == 2) oval(4, -10, 4, 4, 0xEDE6CE);
    rect(5, -10, 2, 1, 0x1A1420);
  }

  static void eyes(int hx, int hy, int col) {
    rect(hx + 2, hy - 2, 1, 2, col);
    rect(hx + 4, hy - 2, 1, 2, col);
  }

  static void head(int fs, int hx, int hy, int sk, int armT) {
    switch (fs) {
      case 0: // caballero
        halfDome(hx, hy - 1, 5, 0xB8BEC8);
        rect(hx, hy - 1, 6, 2, 0x2A2A34);
        rect(hx + 3, hy - 1, 1, 1, 0xFFFFFF);
        rectO(hx - 4, hy - 9, 6, 3, 0xC03030);
        break;
      case 1: // asesino
        ovalO(hx, hy, 5, 5, 0x4A4470);
        oval(hx + 3, hy, 3, 3, sk);
        rect(hx + 0, hy + 1, 6, 3, 0x4A4470);
        rect(hx + 2, hy - 1, 2, 1, 0xFF3030);
        rect(hx + 4, hy - 1, 2, 1, 0xFF3030);
        tri(hx - 3, hy - 3, hx - 10, hy + 3, hx - 2, hy + 3, 0x4A4470);
        break;
      case 2: // clerigo
        ovalO(hx, hy, 5, 5, 0xEDE6CE);
        oval(hx + 3, hy + 1, 3, 3, sk);
        eyes(hx, hy + 1, 0x1A1420);
        fg.setColor(0xFFD84A);
        fg.drawArc(X(hx - 5), Y(hy - 11), X(hx + 5) - X(hx - 5), sz(4), 0, 360);
        fg.drawArc(X(hx - 5), Y(hy - 11) + 1, X(hx + 5) - X(hx - 5), sz(4), 0, 360);
        break;
      case 3: // ladron
        eyes(hx, hy, 0x1A1420);
        rect(hx - 5, hy - 5, 10, 3, 0xD03A3A);
        lineO(hx - 5, hy - 3, hx - 10, hy - 1, 1, 0xD03A3A);
        lineO(hx - 5, hy - 4, hx - 10, hy - 5, 1, 0xD03A3A);
        rect(hx + 0, hy - 1, 6, 2, 0x111111);
        rect(hx + 3, hy - 1, 1, 1, 0xFFFFFF);
        break;
      case 4: // arquero
        eyes(hx, hy, 0x1A1420);
        tri(hx - 6, hy - 3, hx + 6, hy - 3, hx - 1, hy - 14, 0x3E7A3E);
        rectO(hx - 7, hy - 4, 14, 2, 0x2E5A2E);
        lineO(hx + 1, hy - 12, hx + 5, hy - 16, 1, 0xE04040);
        break;
      case 5: // hueco (zombi)
        lineO(hx - 3, hy - 5, hx - 4, hy - 8, 1, 0x3A2A1A);
        lineO(hx, hy - 5, hx, hy - 9, 1, 0x3A2A1A);
        lineO(hx + 3, hy - 5, hx + 4, hy - 8, 1, 0x3A2A1A);
        eyes(hx, hy, 0xFF2020);
        rect(hx + 3, hy + 2, 3, 2, 0x401010);
        break;
      case 6: // arquero enemigo
        ovalO(hx, hy, 5, 5, 0x2E4A2E);
        oval(hx + 3, hy, 3, 3, 0x101414);
        rect(hx + 2, hy - 1, 1, 1, 0xFF2020);
        rect(hx + 4, hy - 1, 1, 1, 0xFF2020);
        tri(hx - 3, hy - 3, hx - 10, hy + 3, hx - 2, hy + 3, 0x2E4A2E);
        break;
      case 7: // pesado
        eyes(hx, hy, 0xFF8020);
        lineO(hx + 1, hy - 4, hx + 5, hy - 2, 1, 0x2A2224);
        tri(hx + 3, hy + 2, hx + 5, hy + 2, hx + 4, hy + 6, 0xEEEEDD);
        break;
      case 8: // jefe
        ovalO(hx, hy, 5, 5, 0x3A1A50);
        tri(hx - 4, hy - 3, hx - 9, hy - 13, hx - 1, hy - 5, 0xE8E0C8);
        tri(hx + 2, hy - 4, hx + 7, hy - 14, hx + 5, hy - 2, 0xE8E0C8);
        rect(hx + 1, hy - 1, 5, 2, 0xFF3030);
        break;
      case 9: // cultista
        ovalO(hx, hy, 5, 5, 0x5A1A1A);
        oval(hx + 3, hy, 3, 3, 0x120808);
        rect(hx + 2, hy - 1, 1, 1, 0xFF8A20);
        rect(hx + 4, hy - 1, 1, 1, 0xFF8A20);
        tri(hx - 3, hy - 3, hx - 10, hy + 3, hx - 2, hy + 3, 0x5A1A1A);
        break;
      case 10: // esqueleto
        eyes(hx, hy - 1, 0x1A1420);
        rect(hx + 3, hy + 2, 3, 2, 0x1A1420);
        rect(hx + 3, hy + 2, 1, 2, 0xE8E4D0);
        rect(hx + 5, hy + 2, 1, 2, 0xE8E4D0);
        break;
      case 11: // druida
        ovalO(hx, hy, 5, 5, 0x8A6A4A);
        rect(hx + 1, hy - 1, 5, 2, 0x66FF99);
        rect(hx - 4, hy + 2, 8, 3, 0xCFCFC0);
        lineO(hx - 2, hy - 5, hx - 5, hy - 11, 1, 0x6B4A2A);
        lineO(hx - 5, hy - 11, hx - 8, hy - 10, 1, 0x6B4A2A);
        lineO(hx - 4, hy - 8, hx - 1, hy - 12, 1, 0x6B4A2A);
        lineO(hx + 2, hy - 5, hx + 5, hy - 11, 1, 0x6B4A2A);
        lineO(hx + 5, hy - 11, hx + 8, hy - 10, 1, 0x6B4A2A);
        lineO(hx + 3, hy - 8, hx + 0, hy - 12, 1, 0x6B4A2A);
        break;
      case 12: // rey caido
        ovalO(hx, hy, 5, 5, 0x3A3A4E);
        rect(hx + 1, hy - 1, 5, 2, 0xFF3030);
        tri(hx - 5, hy - 4, hx - 3, hy - 4, hx - 4, hy - 9, 0xE0B83A);
        tri(hx - 2, hy - 5, hx + 1, hy - 5, hx - 1, hy - 11, 0xE0B83A);
        tri(hx + 2, hy - 4, hx + 5, hy - 4, hx + 4, hy - 9, 0xE0B83A);
        rect(hx - 5, hy - 4, 10, 2, 0xE0B83A);
        break;
      default:
        break;
    }
  }

  // ---------- prerenderizado ----------
  static int w(int fs, int scl) { return CW * scl / 4; }
  static int h(int fs, int scl) { return CH * scl / 4; }
  static int footX(int fs, int scl) { return OXU * scl / 4; }
  static int footY(int fs, int scl) { return OYU * scl / 4; }

  static Image[] build(int fs, int armT, int wpnT) {
    return buildAt(fs, armT, wpnT, SC[fs], null);
  }

  // poses == null -> todas. Devuelve un arreglo de NP (con null en las no pedidas)
  static Image[] buildAt(int fs, int armT, int wpnT, int scl, int[] poses) {
    sc = scl;
    int cw = w(fs, scl), ch = h(fs, scl);
    ox = footX(fs, scl); oy = footY(fs, scl);
    Image[] out = new Image[NP];
    int n = poses == null ? NP : poses.length;
    for (int q = 0; q < n; q++) {
      int p = poses == null ? q : poses[q];
      Image im = Image.createImage(cw, ch);
      fg = im.getGraphics();
      fg.setColor(KEY); fg.fillRect(0, 0, cw, ch);
      fig(fs, p, armT, wpnT);
      int[] buf = new int[cw * ch];
      im.getRGB(buf, 0, cw, 0, 0, cw, ch);
      for (int i = 0; i < buf.length; i++)
        buf[i] = ((buf[i] & 0xFFFFFF) == KEY) ? 0 : (buf[i] | 0xFF000000);
      out[p] = Image.createRGBImage(buf, cw, ch, true);
    }
    return out;
  }
}
