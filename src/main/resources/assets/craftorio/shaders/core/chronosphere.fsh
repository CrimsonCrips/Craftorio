#version 150

uniform vec4 ChronosphereParams;

in vec3 localDir;
in vec3 viewNormal;
in vec3 viewPos;
in float extrusion;

out vec4 fragColor;

float hash(vec3 p) {
    p = fract(p * 0.3183099 + vec3(0.71, 0.113, 0.419));
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float noise(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash(i + vec3(0, 0, 0)), hash(i + vec3(1, 0, 0)), f.x),
                   mix(hash(i + vec3(0, 1, 0)), hash(i + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(hash(i + vec3(0, 0, 1)), hash(i + vec3(1, 0, 1)), f.x),
                   mix(hash(i + vec3(0, 1, 1)), hash(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}

float fbm(vec3 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(p);
        p = p * 2.03 + vec3(11.7, 3.1, 5.3);
        amplitude *= 0.5;
    }
    return value;
}

vec3 spin(vec3 p, float angle) {
    float c = cos(angle);
    float s = sin(angle);
    return vec3(c * p.x - s * p.z, p.y, s * p.x + c * p.z);
}

float streaks(vec3 dir, float time, float scale, float speed, float seed, float width) {
    vec3 p = spin(dir, time * speed) * scale + vec3(seed, time * speed * 0.9, seed * 0.5);
    vec3 warp = vec3(fbm(p + 3.7), fbm(p + 8.1), fbm(p + 1.3)) - 0.5;
    float n = fbm(p + warp * 1.6);
    float line = 1.0 - smoothstep(0.0, width, abs(n - 0.5));
    return line;
}

void main() {
    float time = ChronosphereParams.x;
    float appear = ChronosphereParams.y;
    float dissipate = ChronosphereParams.z;
    float intensity = ChronosphereParams.w;

    if (dissipate >= 1.0 || appear <= 0.0) discard;

    vec3 dir = normalize(localDir);
    vec3 normal = normalize(viewNormal);
    vec3 toEye = normalize(-viewPos);
    float facing = abs(dot(normal, toEye));
    float rim = pow(1.0 - facing, 2.2);

    float height = dir.y * 0.5 + 0.5;
    float reach = appear * 1.3;
    float reveal = 1.0 - smoothstep(reach - 0.3, reach, height);
    if (reveal <= 0.0) discard;

    float band = 0.5 + 0.5 * sin(height * 9.0 - time * 1.4 + fbm(dir * 3.0) * 4.0);
    float flow = streaks(dir, time, 3.2, 0.35, 0.0, 0.07) * 0.8 + streaks(dir, time, 5.5, -0.5, 4.0, 0.07) * 0.55;
    flow = max(flow, extrusion * 1.2);
    float shimmer = fbm(spin(dir, time * 0.2) * 6.0 + vec3(0.0, time * 0.6, 0.0));

    vec3 deep = vec3(0.05, 0.22, 0.85);
    vec3 mid = vec3(0.16, 0.5, 1.0);
    vec3 light = vec3(0.72, 0.93, 1.0);

    vec3 color = mix(deep, mid, shimmer * 0.8 + rim * 0.5);
    color = mix(color, light, clamp(flow, 0.0, 1.0) * 0.9 + rim * 0.25 + band * 0.06);

    float alpha = 0.95;

    float dissolveValue = fbm(dir * 5.0 + 7.0) * 0.86 + 0.07;
    float threshold = mix(-0.15, 1.1, dissipate);
    float dissolve = smoothstep(threshold, threshold + 0.14, dissolveValue);
    float glowEdge = (1.0 - smoothstep(0.0, 0.09, abs(dissolveValue - threshold - 0.05))) * step(0.001, dissipate);
    color = mix(color, light, glowEdge * 0.9);
    alpha += glowEdge * 0.5;
    alpha *= dissolve;
    alpha *= 1.0 - dissipate * 0.35;

    float revealEdge = 1.0 - smoothstep(0.0, 0.1, reach - height);
    alpha += revealEdge * 0.25 * (1.0 - appear);
    alpha *= reveal * intensity;

    fragColor = vec4(color, clamp(alpha, 0.0, 0.95));
}
