import javax.microedition.lcdui.*;

// Dibujo de personajes en Pixel Art Auténtico (Bloques sólidos optimizados para J2ME / Nokia E72)
public class Art {
  static final int OL = 0x1A1420;   // color del contorno
  static final int KEY = 0x000000;  // color clave (transparente)
  static final int NP = 10;         // poses por personaje
  static final int IDLE0 = 0, WALK0 = 2, WIND = 6, SWING = 7, ROLL = 8, GUARD = 9;
  static final int W_NONE = 0, W_SWORD = 1, W_DAGGER = 2, W_MACE = 3, W_KNIFE = 4,
                   W_BOW = 5, W_CLUB = 6, W_GREAT = 7, W_STAFF = 8;

  // estilos de personajes y enemigos
  static final int[] SC = { 4, 4, 4, 4, 4, 4, 4, 5, 6, 4, 4, 6, 6, 4 };
  static final int CW = 44, CH = 44, OXU = 22, OYU = 39;
  static final int[] WPN = { W_SWORD, W_DAGGER, W_MACE, W_KNIFE, W_BOW, W_NONE, W_BOW, W_CLUB, W_GREAT, W_STAFF, W_SWORD, W_STAFF, W_GREAT, W_NONE };
  static final int[] SKIN  = { 0xF2C9A0, 0xE8B88A, 0xF5D0A8, 0xE9BE92, 0xF0C79B, 0x8FAF7A, 0xB8B8A8, 0x9A8F8F, 0x6A4A7A, 0xB89A8A, 0xE8E4D0, 0x8A6A4A, 0x9A9AA8, 0xF0C79B };
  static final int[] TORSO = { 0x6A7AA0, 0x5A5278, 0xEDE6CE, 0x6B4A2A, 0x4F7A3A, 0x5A4A3A, 0x2E4A2E, 0x6A5A5A, 0x5A2A7A, 0x9A2A2A, 0xC8C4B0, 0x2E6A3A, 0x3A3A4E, 0x7A5A2A };
  static final int[] PANTS = { 0x3A3F55, 0x3A3552, 0xC8BFA0, 0x3C3226, 0x4A3E2A, 0x3A3A44, 0x2A3A2A, 0x4A4044, 0x30183F, 0x7A2020, 0xB0AC98, 0x244A2C, 0x2A2A3A, 0x4A3A28 };
  static final int[] BOOT  = { 0x2A2A30, 0x1A1822, 0x6B4A2A, 0x2A2018, 0x3A2A1A, 0x2A2A2A, 0x1E1E1E, 0x2E2828, 0x1A1020, 0x2A1010, 0x9A9680, 0x3A2A1A, 0x1A1A24, 0x2A2018 };
  static final int[] CAPE  = { 0xB02A2A, 0x4A4470, 0, 0, 0x2E5A2E, 0, 0, 0, 0x3A1550, 0, 0, 0x2A4A2A, 0x6A1A1A, 0 };
  static final int[] ARM   = { 0, 0x8B5A2B, 0x9AA0AC, 0x5C86D0, 0xE0B83A };
  static final int[] WCOL  = { 0x9A9AA4, 0xCFD4E0, 0x66B0FF, 0xFFD24A, 0xFF6A2A,
                              0xFF7A1A, 0x66E040, 0x9AE4FF, 0xCC2244, 0xFFEE55 };

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

  static int r(int v) { return (v * sc + (v >= 0 ? 2 : -2)) / 4; }
  static int X(int v) { return ox + r(v); }
  static int Y(int v) { return oy + r(v); }
  static int sz(int v) { int s = r(v); return (v != 0 && s == 0) ? 1 : s; }

  // Dibujo estricto por bloques (Pixel Art puro sin suavizado)
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

  static void line1(int x1, int y1, int x2, int y2, int col) {
    fg.setColor(col); fg.drawLine(X(x1), Y(y1), X(x2), Y(y2));
  }

  static void lineO(int x1, int y1, int x2, int y2, int th, int col) {
    rectO(x1, y1, sz(th), sz(th), col);
  }

  // ---------- armas pixel art ----------
  static void weapon(int wt, int hx, int hy, int vx, int vy, int col, boolean pull) {
    switch (wt) {
      case W_SWORD:
        rectO(hx, hy, 2, 8, col);
        rect(hx, hy - 2, 4, 1, 0xC89B3C); // empuñadura sólida
        break;
      case W_DAGGER:
      case W_KNIFE:
        rectO(hx, hy, 2, 5, col);
        break;
      case W_MACE:
      case W_CLUB:
        rectO(hx, hy, 3, 3, 0x7A5028);
        rectO(hx - 1, hy - 3, 4, 3, col);
        break;
      case W_GREAT:
        rectO(hx - 1, hy - 4, 3, 12, col);
        rect(hx - 2, hy, 5, 2, 0xE0B83A);
        break;
      case W_STAFF:
        rectO(hx, hy - 4, 2, 10, 0x7A5028);
        rect(hx - 1, hy - 5, 4, 2, col);
        break;
      case W_BOW:
        rectO(hx, hy - 4, 1, 8, 0x8B5A2B);
        if (pull) rect(hx + 2, hy, 3, 1, 0xF0F0F0);
        break;
      default:
        break;
    }
  }

  // ---------- figura completa por bloques de pixel art ----------
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
    if (p == WIND) lean = -1;
    if (p == SWING) lean = 1;
    if (p == GUARD) lean = 1;

    int tw = 8;
    int nhx = NHX[p], nhy = NHY[p] + bob;
    int fhx = FHX[p], fhy = FHY[p] + bob;
    int wvx = WVX[p], wvy = WVY[p];

    // Capa sólida
    if (cape != 0) {
      rectO(-3 + lean, -14 + bob, 4, 8, cape);
    }

    // Piernas (Bloques rígidos retro)
    rectO(-3, -6, 3, 6, pants);
    rectO(-3, -2, 4, 3, boot);
    rectO(1, -6, 3, 6, pants);
    rectO(1, -2, 4, 3, boot);

    // Torso sólido
    rectO(-4 + lean, -15 + bob, 8, 8, torso);
    rect(-4 + lean, -9 + bob, 8, 1, 0x1A1420); // Línea de cintura

    // Cabeza / Casco en bloques de píxeles puros
    head(fs, 1 + lean, -19 + bob, sk, armT);

    // Arma y extremidades
    rectO(2 + lean, -13 + bob, 2, 6, torso);
    if (wt != W_NONE) {
      weapon(wt, nhx, nhy, wvx, wvy, wcol, (wt == W_BOW && p == WIND));
    }

    // Escudo clásico por bloques
    if (fs == 0) {
      if (p == GUARD) {
        rectO(5, -18, 5, 10, 0xB8BEC8);
        rect(6, -15, 3, 4, 0xB02A2A);
      } else {
        rectO(2, -12, 4, 7, 0xB8BEC8);
      }
    }
  }

  static void drawRoll(int fs, int torso, int sk, int cape) {
    rectO(-4, -10, 8, 6, torso);
    rectO(2, -8, 4, 4, sk);
  }

  // ---------- Cascos y cabezas detallados en Pixel Art Puro ----------
  static void head(int fs, int hx, int hy, int sk, int armT) {
    switch (fs) {
      case 0: // Caballero: yelmo cuadrado con visera oscura clásica
        rectO(hx - 3, hy - 4, 7, 7, 0xB8BEC8);
        rect(hx - 2, hy - 2, 5, 1, 0x1A1420); // Rendija de la visera
        rect(hx, hy - 2, 1, 1, 0xFFFFFF);     // Brillo en visera
        rectO(hx - 2, hy - 7, 5, 3, 0xC03030); // Penacho rojo superior
        break;
      case 1: // Asesino: capucha oscura con ojos brillantes
        rectO(hx - 3, hy - 4, 7, 7, 0x4A4470);
        rect(hx - 2, hy - 2, 4, 2, 0x1A1420);
        rect(hx - 1, hy - 2, 1, 1, 0xFF3030);
        rect(hx + 1, hy - 2, 1, 1, 0xFF3030);
        break;
      case 2: // Clérigo: capucha clara con aureola de bloques
        rectO(hx - 3, hy - 4, 7, 7, 0xEDE6CE);
        rect(hx - 3, hy - 7, 7, 1, 0xFFD84A);
        break;
      case 3: // Ladrón: pañuelo rojo y rostro sombrío
        rectO(hx - 3, hy - 4, 7, 7, sk);
        rect(hx - 3, hy - 5, 7, 3, 0xD03A3A);
        rect(hx - 1, hy - 2, 3, 2, 0x111111);
        break;
      case 4: // Arquero: sombrero puntiagudo verde
        rectO(hx - 3, hy - 4, 7, 6, sk);
        rectO(hx - 4, hy - 5, 9, 2, 0x2E5A2E);
        rect(hx - 2, hy - 9, 5, 4, 0x3E7A3E);
        break;
      case 5: // Hueco / Zombi: calavera expuesta con ojos rojos
        rectO(hx - 3, hy - 4, 7, 7, 0x5A4A3A);
        rect(hx - 2, hy - 2, 4, 3, 0x2A1A0A);
        rect(hx - 1, hy - 2, 1, 1, 0xFF2020);
        rect(hx + 1, hy - 2, 1, 1, 0xFF2020);
        break;
      case 8: // Jefe / Guardián: corona imponente en bloques dorados/morados
        rectO(hx - 4, hy - 5, 8, 8, 0x3A1A50);
        rect(hx - 3, hy - 7, 6, 2, 0xE0B83A);
        rect(hx - 2, hy - 2, 4, 2, 0xFF3030);
        break;
      default: // Genérico para el resto de enemigos y NPCs
        rectO(hx - 3, hy - 4, 7, 7, sk);
        rect(hx - 2, hy - 2, 4, 2, 0x1A1420);
        break;
    }
  }

  static int w(int fs, int scl) { return CW * scl / 4; }
  static int h(int fs, int scl) { return CH * scl / 4; }
  static int footX(int fs, int scl) { return OXU * scl / 4; }
  static int footY(int fs, int scl) { return OYU * scl / 4; }

  static Image[] build(int fs, int armT, int wpnT) {
    return buildAt(fs, armT, wpnT, SC[fs], null);
  }

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
