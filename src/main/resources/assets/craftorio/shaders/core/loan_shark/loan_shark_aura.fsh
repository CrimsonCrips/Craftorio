#version 150

uniform vec2 AuraParams;

in vec2 texCoord;
out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i + vec2(0.0, 0.0)), hash(i + vec2(1.0, 0.0)), f.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(p);
        p = p * 2.05;
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    float time = AuraParams.x;
    float intensity = AuraParams.y;
    if (intensity <= 0.002) discard;

    vec2 uv = texCoord - 0.5;
    float dist = length(uv) * 2.0;

    float swirl = fbm(uv * 3.2 + vec2(time * 0.12, -time * 0.09));
    float radial = 1.0 - smoothstep(0.1, 1.05, dist + (swirl - 0.5) * 0.25);
    radial = clamp(radial, 0.0, 1.0);

    vec3 deep = vec3(0.04, 0.0, 0.06);
    vec3 glow = vec3(0.28, 0.02, 0.32);
    vec3 color = mix(deep, glow, swirl);

    float pulse = 0.85 + 0.15 * sin(time * 1.6);
    float alpha = radial * radial * intensity * pulse * 0.9;

    fragColor = vec4(color, clamp(alpha, 0.0, 0.95));
}
