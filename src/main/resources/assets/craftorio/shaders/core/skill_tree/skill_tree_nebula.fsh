#version 150

uniform sampler2D Sampler0;
uniform vec2 iResolution;
uniform float iTime;
uniform vec2 Pan;
uniform float Zoom;
uniform float Intensity;
uniform float Opacity;

out vec4 fragColor;

const float FLIGHT_SPEED = 0.8;
const float FIELD_OF_VIEW = 1.05;

const float ORIGIN_OFFSET = 512.0;
const float PAN_DISTANCE = 6.0;
const float ZOOM_EXPONENT = 0.35;

// http://lolengine.net/blog/2013/07/27/rgb-to-hsv-in-glsl
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 getRayDirection(vec2 fragCoord, vec3 cameraDirection, float magnification) {
    vec2 uv = fragCoord.xy / iResolution.xy;

    const float screenWidth = 1.0;
    float originToScreen = magnification * screenWidth / 2.0 / tan(FIELD_OF_VIEW / 2.0);

    vec3 screenCenter = originToScreen * cameraDirection;
    vec3 baseX = normalize(cross(screenCenter, vec3(0.0, -1.0, 0.0)));
    vec3 baseY = normalize(cross(screenCenter, baseX));

    return normalize(screenCenter + (uv.x - 0.5) * baseX + (uv.y - 0.5) * iResolution.y / iResolution.x * baseY);
}

vec4 getNebulaColor(vec3 globalPosition, vec3 rayDirection) {
    vec3 color = vec3(0.0);
    float spaceLeft = 1.0;

    const float layerDistance = 10.0;

    const int steps = 4;
    for (int i = 0; i <= steps; i++) {
        vec3 noiseeval = globalPosition + rayDirection * ((1.0 - fract(globalPosition.z / layerDistance) + float(i)) * layerDistance / rayDirection.z);
        noiseeval.xy += noiseeval.z;

        float value = 0.06 * texture(Sampler0, fract(noiseeval.xy / 60.0)).r;

        if (i == 0) {
            value *= 1.0 - fract(globalPosition.z / layerDistance);
        } else if (i == steps) {
            value *= fract(globalPosition.z / layerDistance);
        }

        float hue = mod(noiseeval.z / layerDistance / 34.444, 1.0);

        color += spaceLeft * hsv2rgb(vec3(hue, 1.0, value));
        spaceLeft = max(0.0, spaceLeft - value * 2.0);
    }
    return vec4(color, 1.0);
}

void main() {
    float magnification = pow(Zoom, ZOOM_EXPONENT);
    vec3 movementDirection = normalize(vec3(0.01, 0.0, 1.0));

    vec3 rayDirection = getRayDirection(gl_FragCoord.xy, movementDirection, magnification);

    vec2 panOffset = vec2(-Pan.x, Pan.y) * PAN_DISTANCE / magnification;
    vec3 globalPosition = vec3(3.14159 + ORIGIN_OFFSET + panOffset.x, 3.14159 + ORIGIN_OFFSET + panOffset.y, 0.0)
                        + (iTime + 1000.0) * FLIGHT_SPEED * movementDirection;

    vec3 nebula = getNebulaColor(globalPosition, rayDirection).rgb * Opacity;
    fragColor = vec4(clamp(nebula * Intensity, 0.0, 1.0), 1.0);
}
