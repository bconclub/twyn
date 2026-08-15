"""Generate the TWYN particle twin-mark SVG: two overlapping dot-spheres."""
import math
import random

random.seed(7)  # deterministic mark

W, H = 480, 300
R = 100
C1 = (W / 2 - 55, H / 2)
C2 = (W / 2 + 55, H / 2)

def sphere_dots(cx, cy, n, base_opacity):
    dots = []
    for _ in range(n):
        u = random.random()
        # bias toward the rim so it reads as a 3D globe
        r = R * (u ** 0.35)
        a = random.random() * 2 * math.pi
        x = cx + r * math.cos(a)
        y = cy + r * math.sin(a)
        rim = r / R
        size = random.uniform(0.6, 1.9) * (0.7 + 0.6 * rim)
        op = base_opacity * random.uniform(0.25, 1.0) * (0.45 + 0.55 * rim)
        dots.append((x, y, size, min(op, 1.0)))
    return dots

def in_overlap(x, y):
    d1 = math.hypot(x - C1[0], y - C1[1])
    d2 = math.hypot(x - C2[0], y - C2[1])
    return d1 < R and d2 < R

parts = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" role="img" aria-label="TWYN mark">']
for (cx, cy), n, op in [(C1, 1500, 0.85), (C2, 1500, 0.85)]:
    for x, y, s, o in sphere_dots(cx, cy, n, op):
        boost = 1.35 if in_overlap(x, y) else 1.0
        parts.append(
            f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{s:.2f}" fill="#fff" opacity="{min(o * boost, 1):.2f}"/>'
        )
parts.append("</svg>")

with open("twyn-mark.svg", "w") as f:
    f.write("".join(parts))
print("twyn-mark.svg written,", len(parts) - 2, "dots")
