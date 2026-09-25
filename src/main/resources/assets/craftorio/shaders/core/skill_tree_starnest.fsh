#version 150

uniform vec2 iResolution;
uniform vec2 Pan;
uniform float Zoom;
uniform float Intensity;

out vec4 fragColor;

const int iterations = 17;
const float formuparam = 0.53;
const int volsteps = 20;
const float stepsize = 0.1;
const float zoom = 0.800;
const float tile = 0.850;
const float speed = 0.010;
const float brightness = 0.0015;
const float darkmatter = 0.300;
const float distfading = 0.730;
const float saturation = 0.850;
const float panDistance = 0.25;
const float zoomExponent = 0.35;

void main() {
    vec2 uv = gl_FragCoord.xy / iResolution.xy - 0.5;
    uv.y *= iResolution.y / iResolution.x;
    float magnification = pow(Zoom, zoomExponent);
    vec3 dir = vec3(uv * zoom / magnification, 1.0);
    float time = 0.25;

    float a1 = 0.5;
    float a2 = 0.8;
    mat2 rot1 = mat2(cos(a1), sin(a1), -sin(a1), cos(a1));
    mat2 rot2 = mat2(cos(a2), sin(a2), -sin(a2), cos(a2));
    dir.xz *= rot1;
    dir.xy *= rot2;
    vec3 from = vec3(1.0, 0.5, 0.5);
    from += vec3(time * 2.0, time, -2.0);
    from.xz *= rot1;
    from.xy *= rot2;

    vec3 right = vec3(1.0, 0.0, 0.0);
    right.xz *= rot1;
    right.xy *= rot2;
    vec3 up = vec3(0.0, 1.0, 0.0);
    up.xz *= rot1;
    up.xy *= rot2;
    from += (-Pan.x * right + Pan.y * up) * panDistance / magnification;

    float s = 0.1;
    float fade = 1.0;
    vec3 v = vec3(0.0);
    for (int r = 0; r < volsteps; r++) {
        vec3 p = from + s * dir * 0.5;
        p = abs(vec3(tile) - mod(p, vec3(tile * 2.0)));
        float pa = 0.0;
        float a = 0.0;
        for (int i = 0; i < iterations; i++) {
            p = abs(p) / dot(p, p) - formuparam;
            a += abs(length(p) - pa);
            pa = length(p);
        }
        float dm = max(0.0, darkmatter - a * a * 0.001);
        a *= a * a;
        if (r > 6) fade *= 1.0 - dm;
        v += fade;
        v += vec3(s, s * s, s * s * s * s) * a * brightness * fade;
        fade *= distfading;
        s += stepsize;
    }
    v = mix(vec3(length(v)), v, saturation);
    fragColor = vec4(clamp(v * 0.01 * Intensity, 0.0, 1.0), 1.0);
}
