# Genera res/icon.png (32x32) sin librerias externas.
import zlib, struct, os

S = 32
px = [[(0, 0, 0, 0) for _ in range(S)] for _ in range(S)]

def put(x, y, c):
    if 0 <= x < S and 0 <= y < S:
        px[y][x] = c + (255,)

def rect(x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            put(x, y, c)

def disc(cx, cy, r, c):
    for y in range(S):
        for x in range(S):
            if (x - cx) ** 2 + (y - cy) ** 2 <= r * r:
                put(x, y, c)

# fondo: noche degradada con marco dorado
for y in range(S):
    for x in range(S):
        t = y / (S - 1)
        put(x, y, (int(14 + 30 * t), int(10 + 14 * t), int(34 + 30 * t)))
rect(0, 0, 31, 0, (221, 187, 68)); rect(0, 31, 31, 31, (221, 187, 68))
rect(0, 0, 0, 31, (221, 187, 68)); rect(31, 0, 31, 31, (221, 187, 68))
rect(1, 1, 30, 1, (110, 90, 40)); rect(1, 30, 30, 30, (110, 90, 40))
rect(1, 1, 1, 30, (110, 90, 40)); rect(30, 1, 30, 30, (110, 90, 40))
# luna
disc(25, 7, 3, (232, 228, 200)); disc(26, 6, 3, (22, 14, 44))
# estrellas
for (x, y) in [(5, 5), (11, 3), (17, 7), (8, 10), (21, 12), (4, 15)]:
    put(x, y, (200, 200, 230))
# suelo
rect(2, 26, 29, 29, (10, 8, 16))
# espada clavada
rect(15, 8, 16, 22, (205, 212, 224)); rect(16, 8, 16, 22, (160, 168, 184))
put(15, 7, (230, 235, 245)); put(16, 7, (230, 235, 245))
rect(12, 22, 19, 23, (200, 155, 60)); rect(14, 24, 17, 25, (120, 80, 36))
rect(15, 26, 16, 27, (60, 40, 20))
# fuego
def flame(cx, base, h, c):
    for i in range(h):
        w = max(0, (h - i) * 2 // 3)
        rect(cx - w // 2, base - i, cx + w // 2, base - i, c)
flame(8, 28, 9, (255, 106, 0)); flame(8, 28, 6, (255, 170, 34)); flame(8, 28, 3, (255, 238, 136))
flame(24, 28, 9, (255, 106, 0)); flame(24, 28, 6, (255, 170, 34)); flame(24, 28, 3, (255, 238, 136))
rect(5, 28, 11, 29, (85, 51, 17)); rect(21, 28, 27, 29, (85, 51, 17))

raw = b''.join(b'\x00' + b''.join(bytes(p) for p in row) for row in px)
def chunk(t, d):
    c = struct.pack('>I', len(d)) + t + d
    return c + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', S, S, 8, 6, 0, 0, 0)) \
      + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b'')
os.makedirs('res', exist_ok=True)
open('res/icon.png', 'wb').write(png)
print('res/icon.png', len(png), 'bytes')
