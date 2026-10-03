#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 ChronosphereParams;

out vec3 localDir;
out vec3 viewNormal;
out vec3 viewPos;
out float extrusion;

const float EXTRUDE_HEIGHT = 0.035;

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

    vec3 dir = normalize(Position);
    float height = dir.y * 0.5 + 0.5;
    float reach = appear * 1.3;
    float reveal = 1.0 - smoothstep(reach - 0.3, reach, height);

    float ridge = clamp(streaks(dir, time, 3.2, 0.35, 0.0, 0.1) * 0.8 + streaks(dir, time, 5.5, -0.5, 4.0, 0.1) * 0.55, 0.0, 1.0);
    extrusion = ridge * reveal * (1.0 - dissipate);

    vec3 displaced = dir * (1.0 + extrusion * EXTRUDE_HEIGHT);
    vec4 view = ModelViewMat * vec4(displaced, 1.0);
    localDir = dir;
    viewNormal = normalize(mat3(ModelViewMat) * dir);
    viewPos = view.xyz;
    gl_Position = ProjMat * view;
}
