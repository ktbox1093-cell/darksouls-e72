import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;
import java.io.ByteArrayInputStream;

// Sonido sintetizado en memoria (WAV de 8 kHz y 8 bits). No usa archivos externos.
public class Snd implements Runnable {
  static final int RATE = 8000;
  static final int S_SWING = 0, S_HIT = 1, S_CRIT = 2, S_HURT = 3, S_BLOCK = 4, S_ROLL = 5, S_COIN = 6,
      S_POTION = 7, S_UP = 8, S_REST = 9, S_BLAST = 10, S_FIRE = 11, S_DOOR = 12, S_DEATH = 13,
      S_WIN = 14, S_MENU = 15, S_OK = 16, S_SPECIAL = 17, S_ARROW = 18, S_BUY = 19, NS = 20;

  boolean sfxOn = true, musOn = true, alive = true, paused, broken, running;
  byte[][] fx = new byte[NS][];
  int[] q = new int[8];
  int qh, qt;
  int wantMusic = -1, curMusic = -1;
  Player pm, ps;
  String mime = "audio/x-wav";
  int fails;
  static int[] SIN;
  static float[] FREQ;

  static final int[][] MUS = {
    // raiz, bpm, escala, onda del lead, bajo (0 lento / 1 corcheas), densidad de melodia
    { 57, 96, 0, 0, 0, 1 },
    { 50, 72, 0, 2, 0, 1 },
    { 52, 64, 1, 1, 0, 0 },
    { 55, 84, 2, 1, 0, 2 },
    { 48, 92, 0, 0, 1, 1 },
    { 57, 138, 0, 0, 1, 3 }
  };
  static final int[][] SCALES = { { 0, 2, 3, 5, 7, 8, 10 }, { 0, 1, 3, 5, 7, 8, 10 }, { 0, 2, 4, 7, 9 } };
  static final int[] PROG = { 0, 5, 2, 6 };

  static void init() {
    if (SIN != null) return;
    SIN = new int[256];
    for (int i = 0; i < 256; i++) SIN[i] = (int) (Math.sin(i * 6.2831853 / 256.0) * 100.0);
    FREQ = new float[128];
    float f = 440f;
    for (int n = 69; n < 128; n++) { FREQ[n] = f; f *= 1.0594631f; }
    f = 440f;
    for (int n = 69; n >= 0; n--) { FREQ[n] = f; f /= 1.0594631f; }
  }

  static int hz(int note) {
    if (note < 0) note = 0;
    if (note > 127) note = 127;
    return (int) FREQ[note];
  }

  // ---------------- sintesis ----------------
  // wave: 0 cuadrada, 1 seno, 2 triangular, 3 sierra, 4 ruido. dec: 0 lineal, 1 cuadratica
  static void voice(byte[] b, int pos, int ms, int f0, int f1, int wave, int vol, int dec) {
    int n = ms * RATE / 1000;
    if (pos < 0 || pos >= b.length) return;
    if (pos + n > b.length) n = b.length - pos;
    if (n <= 0) return;
    int ph = 0, seed = 12345 + f0 * 7;
    for (int i = 0; i < n; i++) {
      int f = f0 + (f1 - f0) * i / n;
      ph += f * 65536 / RATE;
      int s;
      switch (wave) {
        case 0: s = ((ph >> 15) & 1) == 0 ? 100 : -100; break;
        case 1: s = SIN[(ph >> 8) & 255]; break;
        case 2: {
          int t = (ph >> 8) & 255;
          s = t < 128 ? (t - 64) * 100 / 64 : (192 - t) * 100 / 64;
          break; }
        case 3: s = (((ph >> 8) & 255) - 128) * 100 / 128; break;
        default:
          seed = seed * 1103515245 + 12345;
          s = (((seed >> 16) & 255) - 128) * 100 / 128;
          break;
      }
      int env = dec == 0 ? (n - i) * 100 / n : (n - i) * (n - i) / n * 100 / n;
      if (i < 24) env = env * i / 24;
      int v = s * vol / 100 * env / 100;
      int o = (b[pos + i] & 255) - 128 + v;
      if (o > 127) o = 127;
      if (o < -128) o = -128;
      b[pos + i] = (byte) (o + 128);
    }
  }

  static byte[] blank(int ms) {
    byte[] b = new byte[ms * RATE / 1000];
    for (int i = 0; i < b.length; i++) b[i] = (byte) 128;
    return b;
  }

  static int ms(int samples) { return samples * 1000 / RATE; }

  static byte[] genFx(int id) {
    init();
    byte[] b;
    switch (id) {
      case S_SWING:
        b = blank(120);
        voice(b, 0, 110, 0, 0, 4, 34, 1);
        voice(b, 0, 100, 260, 120, 1, 22, 1);
        return b;
      case S_HIT:
        b = blank(120);
        voice(b, 0, 80, 0, 0, 4, 70, 1);
        voice(b, 0, 100, 190, 80, 0, 50, 1);
        return b;
      case S_CRIT:
        b = blank(260);
        voice(b, 0, 90, 0, 0, 4, 80, 1);
        voice(b, 0, 120, 200, 70, 0, 60, 1);
        voice(b, 40, 200, 900, 450, 0, 40, 1);
        voice(b, 40, 200, 1350, 700, 2, 30, 1);
        return b;
      case S_HURT:
        b = blank(260);
        voice(b, 0, 240, 320, 110, 3, 60, 1);
        voice(b, 0, 90, 0, 0, 4, 45, 1);
        return b;
      case S_BLOCK:
        b = blank(180);
        voice(b, 0, 60, 0, 0, 4, 45, 1);
        voice(b, 0, 170, 1250, 1200, 2, 50, 1);
        voice(b, 0, 150, 1880, 1800, 2, 35, 1);
        return b;
      case S_ROLL:
        b = blank(200);
        voice(b, 0, 190, 0, 0, 4, 26, 0);
        return b;
      case S_COIN:
        b = blank(220);
        voice(b, 0, 70, 988, 988, 0, 34, 0);
        voice(b, 480, 150, 1319, 1319, 0, 34, 1);
        return b;
      case S_POTION:
        b = blank(260);
        voice(b, 0, 80, 400, 420, 1, 50, 0);
        voice(b, 640, 80, 500, 520, 1, 50, 0);
        voice(b, 1280, 120, 640, 660, 1, 50, 1);
        return b;
      case S_UP:
        b = blank(420);
        voice(b, 0, 90, 523, 523, 2, 60, 0);
        voice(b, 720, 90, 659, 659, 2, 60, 0);
        voice(b, 1440, 90, 784, 784, 2, 60, 0);
        voice(b, 2160, 220, 1047, 1047, 2, 60, 1);
        return b;
      case S_REST:
        b = blank(520);
        voice(b, 0, 500, 262, 262, 1, 30, 1);
        voice(b, 0, 500, 330, 330, 1, 30, 1);
        voice(b, 0, 500, 392, 392, 1, 30, 1);
        return b;
      case S_BLAST:
        b = blank(360);
        voice(b, 0, 330, 0, 0, 4, 80, 1);
        voice(b, 0, 340, 90, 38, 1, 90, 1);
        return b;
      case S_FIRE:
        b = blank(200);
        voice(b, 0, 180, 0, 0, 4, 34, 1);
        voice(b, 0, 190, 520, 230, 0, 26, 1);
        return b;
      case S_DOOR:
        b = blank(320);
        voice(b, 0, 290, 110, 55, 0, 55, 1);
        voice(b, 0, 150, 0, 0, 4, 35, 1);
        return b;
      case S_DEATH:
        b = blank(760);
        voice(b, 0, 740, 330, 55, 1, 70, 1);
        voice(b, 0, 300, 0, 0, 4, 40, 1);
        return b;
      case S_WIN:
        b = blank(900);
        voice(b, 0, 130, 523, 523, 0, 40, 0);
        voice(b, 1040, 130, 659, 659, 0, 40, 0);
        voice(b, 2080, 130, 784, 784, 0, 40, 0);
        voice(b, 3120, 480, 1047, 1047, 0, 44, 1);
        voice(b, 3120, 480, 523, 523, 2, 40, 1);
        return b;
      case S_MENU:
        b = blank(40);
        voice(b, 0, 30, 880, 880, 0, 24, 0);
        return b;
      case S_OK:
        b = blank(120);
        voice(b, 0, 45, 660, 660, 0, 32, 0);
        voice(b, 360, 80, 990, 990, 0, 32, 1);
        return b;
      case S_SPECIAL:
        b = blank(300);
        voice(b, 0, 260, 300, 1200, 1, 55, 1);
        voice(b, 0, 260, 150, 600, 2, 35, 1);
        return b;
      case S_ARROW:
        b = blank(100);
        voice(b, 0, 70, 0, 0, 4, 22, 1);
        voice(b, 0, 90, 900, 500, 1, 30, 1);
        return b;
      default:
        b = blank(260);
        voice(b, 0, 70, 1047, 1047, 0, 34, 0);
        voice(b, 560, 70, 1319, 1319, 0, 34, 0);
        voice(b, 1120, 150, 1568, 1568, 0, 34, 1);
        return b;
    }
  }

  static int degNote(int root, int[] sc, int d) {
    return root + sc[d % sc.length] + 12 * (d / sc.length);
  }

  static byte[] music(int kind) {
    init();
    if (kind < 0 || kind >= MUS.length) kind = 0;
    int[] m = MUS[kind];
    int[] sc = SCALES[m[2]];
    int beatMs = 60000 / m[1];
    int spb = beatMs * RATE / 1000;
    byte[] b = blank(beatMs * 16);
    int seed = 777 + kind * 31;
    for (int bar = 0; bar < 4; bar++) {
      int deg = PROG[bar];
      int bs = bar * 4 * spb;
      for (int k = 0; k < 3; k++)
        voice(b, bs, beatMs * 4, hz(degNote(m[0], sc, deg + 2 * k)), hz(degNote(m[0], sc, deg + 2 * k)), 1, 9, 0);
      int bass = degNote(m[0] - 12, sc, deg);
      int fifth = degNote(m[0] - 12, sc, deg + 4);
      if (m[4] == 0) {
        voice(b, bs, beatMs * 2, hz(bass), hz(bass), 2, 30, 1);
        voice(b, bs + 2 * spb, beatMs * 2, hz(bass), hz(bass), 2, 26, 1);
      } else {
        for (int e = 0; e < 8; e++) {
          int nn = (e & 1) == 0 ? bass : fifth;
          voice(b, bs + e * spb / 2, beatMs / 2, hz(nn), hz(nn), 2, 28, 1);
        }
      }
      for (int beat = 0; beat < 4; beat++) {
        seed = seed * 1103515245 + 12345;
        int r = (seed >> 8) & 127;
        if (r % 100 < m[5] * 25 + 8) {
          seed = seed * 1103515245 + 12345;
          int pick = ((seed >> 10) & 3) * 2;
          int nn = degNote(m[0] + 12, sc, deg + pick);
          int len = (((seed >> 14) & 1) == 0) ? 1 : 2;
          if (beat == 3) len = 1;
          voice(b, bs + beat * spb, beatMs * len, hz(nn), hz(nn), m[3], 15, 1);
        }
      }
    }
    return b;
  }

  static byte[] wav(byte[] pcm) {
    int n = pcm.length;
    byte[] w = new byte[44 + n];
    put(w, 0, "RIFF"); le32(w, 4, 36 + n); put(w, 8, "WAVE"); put(w, 12, "fmt ");
    le32(w, 16, 16); w[20] = 1; w[22] = 1; le32(w, 24, RATE); le32(w, 28, RATE);
    w[32] = 1; w[34] = 8; put(w, 36, "data"); le32(w, 40, n);
    System.arraycopy(pcm, 0, w, 44, n);
    return w;
  }

  static void put(byte[] w, int p, String s) {
    for (int i = 0; i < s.length(); i++) w[p + i] = (byte) s.charAt(i);
  }

  static void le32(byte[] w, int p, int v) {
    w[p] = (byte) v; w[p + 1] = (byte) (v >> 8); w[p + 2] = (byte) (v >> 16); w[p + 3] = (byte) (v >> 24);
  }

  // ---------------- reproduccion (hilo propio) ----------------
  public void play(int id) {
    if (!sfxOn || broken || id < 0 || id >= NS) return;
    synchronized (this) {
      int nt = (qt + 1) % 8;
      if (nt != qh) { q[qt] = id; qt = nt; }
    }
  }

  public void music(int kind, boolean on) { wantMusic = kind; musOn = on; }
  public void pause(boolean p) { paused = p; }
  public void quit() { alive = false; }

  Player make(byte[] w) throws Exception {
    Player p;
    try {
      p = Manager.createPlayer(new ByteArrayInputStream(w), mime);
    } catch (Exception e) {
      mime = "audio/wav";
      p = Manager.createPlayer(new ByteArrayInputStream(w), mime);
    }
    p.realize();
    return p;
  }

  void vol(Player p, int level) {
    try {
      VolumeControl v = (VolumeControl) p.getControl("VolumeControl");
      if (v != null) v.setLevel(level);
    } catch (Throwable t) {}
  }

  void closeP(Player p) {
    if (p == null) return;
    try { p.stop(); } catch (Throwable t) {}
    try { p.close(); } catch (Throwable t) {}
  }

  void playSfx(int id) throws Exception {
    byte[] w = fx[id];
    if (w == null) { w = wav(genFx(id)); fx[id] = w; }
    closeP(ps); ps = null;
    Player p = make(w);
    vol(p, 80);
    p.start();
    ps = p;
  }

  void syncMusic() throws Exception {
    boolean want = musOn && !paused && wantMusic >= 0;
    if (!musOn && pm != null) { closeP(pm); pm = null; curMusic = -1; running = false; return; }
    if (!want) {
      if (pm != null && running) { try { pm.stop(); } catch (Throwable t) {} running = false; }
      return;
    }
    if (pm == null || curMusic != wantMusic) {
      closeP(pm); pm = null; running = false;
      Player p = make(wav(music(wantMusic)));
      p.setLoopCount(-1);
      vol(p, 45);
      p.start();
      pm = p; curMusic = wantMusic; running = true;
    } else if (!running) {
      pm.start(); running = true;
    }
  }

  public void run() {
    init();
    while (alive) {
      int id = -1;
      try {
        synchronized (this) {
          if (qh != qt) { id = q[qh]; qh = (qh + 1) % 8; }
        }
        if (id >= 0 && !paused && !broken) playSfx(id);
        if (!broken) syncMusic();
      } catch (Throwable t) {
        fails++;
        if (fails > 8) broken = true;
      }
      try { Thread.sleep(id >= 0 ? 5 : 30); } catch (Exception e) {}
    }
    closeP(pm); closeP(ps);
  }
}

