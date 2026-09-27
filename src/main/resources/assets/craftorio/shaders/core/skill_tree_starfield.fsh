#version 150

uniform sampler2D Sampler0;
uniform vec2 iResolution;
uniform float iTime;
uniform vec2 Pan;
uniform float Zoom;
uniform float Intensity;

out vec4 fragColor;

const float FLIGHT_SPEED = 8.0;

const float DRAW_DISTANCE = 60.0;
const float FADEOUT_DISTANCE = 10.0;
const float FIELD_OF_VIEW = 1.05;

const float STAR_SIZE = 0.6;
const float STAR_CORE_SIZE = 0.14;

const float CLUSTER_SCALE = 0.02;
const float STAR_THRESHOLD = 0.775;

const float BLACK_HOLE_CORE_RADIUS = 0.2;
const float BLACK_HOLE_THRESHOLD = 0.9995;
const float BLACK_HOLE_DISTORTION = 0.03;

const float ORIGIN_OFFSET = 512.0;
const float PAN_DISTANCE = 6.0;
const float ZOOM_EXPONENT = 0.35;
const float NEBULA_OPACITY = 0.6;

// http://lolengine.net/blog/2013/07/27/rgb-to-hsv-in-glsl
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

// https://stackoverflow.com/questions/4200224/random-noise-functions-for-glsl
float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
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

float getDistance(ivec3 chunkPath, vec3 localStart, vec3 localPosition) {
    return length(vec3(chunkPath) + localPosition - localStart);
}

void move(inout vec3 localPosition, vec3 rayDirection, vec3 directionBound) {
    vec3 directionSign = sign(rayDirection);
    vec3 amountVector = (directionBound - directionSign * localPosition) / abs(rayDirection);

    float amount = min(amountVector.x, min(amountVector.y, amountVector.z));

    localPosition += amount * rayDirection;
}

void moveInsideBox(inout vec3 localPosition, inout ivec3 chunk, vec3 directionSign, vec3 directionBound) {
    const float eps = 0.0000001;
    if (localPosition.x * directionSign.x >= directionBound.x - eps) {
        localPosition.x -= directionSign.x;
        chunk.x += int(directionSign.x);
    } else if (localPosition.y * directionSign.y >= directionBound.y - eps) {
        localPosition.y -= directionSign.y;
        chunk.y += int(directionSign.y);
    } else if (localPosition.z * directionSign.z >= directionBound.z - eps) {
        localPosition.z -= directionSign.z;
        chunk.z += int(directionSign.z);
    }
}

bool hasStar(ivec3 chunk) {
    return textureLod(Sampler0, mod(CLUSTER_SCALE * (vec2(chunk.xy) + vec2(chunk.zx)) + vec2(0.724, 0.111), 1.0), 0.0).r > STAR_THRESHOLD
        && textureLod(Sampler0, mod(CLUSTER_SCALE * (vec2(chunk.xz) + vec2(chunk.zy)) + vec2(0.333, 0.777), 1.0), 0.0).r > STAR_THRESHOLD;
}

bool hasBlackHole(ivec3 chunk) {
    return rand(0.0001 * vec2(chunk.xy) + 0.002 * vec2(chunk.yz)) > BLACK_HOLE_THRESHOLD;
}

vec3 getStarToRayVector(vec3 rayBase, vec3 rayDirection, vec3 starPosition) {
    float r = (dot(rayDirection, starPosition) - dot(rayDirection, rayBase)) / dot(rayDirection, rayDirection);
    vec3 pointOnRay = rayBase + r * rayDirection;
    return pointOnRay - starPosition;
}

vec3 getStarPosition(ivec3 chunk, float starSize) {
    vec3 position = abs(vec3(rand(vec2(float(chunk.x) / float(chunk.y) + 0.24, float(chunk.y) / float(chunk.z) + 0.66)),
                             rand(vec2(float(chunk.x) / float(chunk.z) + 0.73, float(chunk.z) / float(chunk.y) + 0.45)),
                             rand(vec2(float(chunk.y) / float(chunk.x) + 0.12, float(chunk.y) / float(chunk.z) + 0.76))));

    return starSize * vec3(1.0) + (1.0 - 2.0 * starSize) * position;
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

vec4 getStarGlowColor(float starDistance, float angle, float hue) {
    float progress = 1.0 - starDistance;
    return vec4(hsv2rgb(vec3(hue, 0.3, 1.0)), 0.4 * pow(progress, 2.0) * mix(pow(abs(sin(angle * 2.5)), 8.0), 1.0, progress));
}

float atan2(vec2 value) {
    if (value.x > 0.0) {
        return atan(value.y / value.x);
    } else if (value.x == 0.0) {
        return 3.14592 * 0.5 * sign(value.y);
    } else if (value.y >= 0.0) {
        return atan(value.y / value.x) + 3.141592;
    } else {
        return atan(value.y / value.x) - 3.141592;
    }
}

vec3 getStarColor(vec3 starSurfaceLocation, float seed, float viewDistance) {
    const float DISTANCE_FAR = 20.0;
    const float DISTANCE_NEAR = 15.0;

    if (viewDistance > DISTANCE_FAR) {
        return vec3(1.0);
    }

    float fadeToWhite = max(0.0, (viewDistance - DISTANCE_NEAR) / (DISTANCE_FAR - DISTANCE_NEAR));

    vec3 coordinate = vec3(acos(starSurfaceLocation.y), atan2(starSurfaceLocation.xz), seed);

    float progress = pow(textureLod(Sampler0, fract(0.3 * coordinate.xy + seed * vec2(1.1)), 0.0).r, 4.0);

    return mix(mix(vec3(1.0, 0.98, 0.9), vec3(1.0, 0.627, 0.01), progress), vec3(1.0), fadeToWhite);
}

vec4 blendColors(vec4 front, vec4 back) {
    return vec4(mix(back.rgb, front.rgb, front.a / max(front.a + back.a, 0.000001)), front.a + back.a - front.a * back.a);
}

void main() {
    float magnification = pow(Zoom, ZOOM_EXPONENT);
    vec3 movementDirection = normalize(vec3(0.01, 0.0, 1.0));

    vec3 rayDirection = getRayDirection(gl_FragCoord.xy, movementDirection, magnification);
    vec3 directionSign = sign(rayDirection);
    vec3 directionBound = vec3(0.5) + 0.5 * directionSign;

    vec2 panOffset = vec2(-Pan.x, Pan.y) * PAN_DISTANCE / magnification;
    vec3 globalPosition = vec3(3.14159 + ORIGIN_OFFSET + panOffset.x, 3.14159 + ORIGIN_OFFSET + panOffset.y, 0.0)
                        + (iTime + 1000.0) * FLIGHT_SPEED * movementDirection;
    ivec3 chunk = ivec3(floor(globalPosition));
    vec3 localPosition = globalPosition - floor(globalPosition);
    moveInsideBox(localPosition, chunk, directionSign, directionBound);

    ivec3 startChunk = chunk;
    vec3 localStart = localPosition;

    vec4 color = vec4(0.0);

    for (int i = 0; i < 200; i++) {
        move(localPosition, rayDirection, directionBound);
        moveInsideBox(localPosition, chunk, directionSign, directionBound);

        if (hasStar(chunk)) {
            vec3 starPosition = getStarPosition(chunk, 0.5 * STAR_SIZE);
            float currentDistance = getDistance(chunk - startChunk, localStart, starPosition);

            vec3 starToRayVector = getStarToRayVector(localPosition, rayDirection, starPosition);
            float distanceToStar = length(starToRayVector);
            distanceToStar *= 2.0;

            if (distanceToStar < STAR_SIZE) {
                float starMaxBrightness = clamp((DRAW_DISTANCE - currentDistance) / FADEOUT_DISTANCE, 0.001, 1.0);

                float starColorSeed = (float(chunk.x) + 13.0 * float(chunk.y) + 7.0 * float(chunk.z)) * 0.00453;
                if (distanceToStar < STAR_SIZE * STAR_CORE_SIZE) {
                    vec3 starSurfaceVector = normalize(starToRayVector + rayDirection * sqrt(pow(STAR_CORE_SIZE * STAR_SIZE, 2.0) - pow(distanceToStar, 2.0)));

                    color = blendColors(color, vec4(getStarColor(starSurfaceVector, starColorSeed, currentDistance), starMaxBrightness));
                    break;
                } else {
                    float localStarDistance = ((distanceToStar / STAR_SIZE) - STAR_CORE_SIZE) / (1.0 - STAR_CORE_SIZE);
                    vec4 glowColor = getStarGlowColor(localStarDistance, atan2(starToRayVector.xy), starColorSeed);
                    glowColor.a *= starMaxBrightness;
                    color = blendColors(color, glowColor);
                }
            }
        } else if (hasBlackHole(chunk)) {
            const vec3 blackHolePosition = vec3(0.5);
            float currentDistance = getDistance(chunk - startChunk, localStart, blackHolePosition);
            float fadeout = clamp((DRAW_DISTANCE - currentDistance) / FADEOUT_DISTANCE, 0.0, 1.0);

            vec3 coreToRayVector = getStarToRayVector(localPosition, rayDirection, blackHolePosition);
            float distanceToCore = length(coreToRayVector);
            if (distanceToCore < BLACK_HOLE_CORE_RADIUS * 0.5) {
                color = blendColors(color, vec4(vec3(0.0), fadeout));
                break;
            } else if (distanceToCore < 0.5) {
                rayDirection = normalize(rayDirection - fadeout * (BLACK_HOLE_DISTORTION / distanceToCore - BLACK_HOLE_DISTORTION / 0.5) * coreToRayVector / distanceToCore);
            }
        }

        if (length(vec3(chunk - startChunk)) > DRAW_DISTANCE) {
            break;
        }
    }

    if (color.a < 1.0) {
        vec4 nebula = getNebulaColor(globalPosition, rayDirection);
        nebula.rgb *= NEBULA_OPACITY;
        color = blendColors(color, nebula);
    }

    fragColor = vec4(clamp(color.rgb * Intensity, 0.0, 1.0), 1.0);
}
