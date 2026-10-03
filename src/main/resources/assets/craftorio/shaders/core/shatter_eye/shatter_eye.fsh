#version 150

uniform sampler2D Sampler0;
uniform vec2 iResolution;
uniform float iTime;
uniform float Strength;

out vec4 fragColor;

const mat2 m = mat2( 0.80,  0.60, -0.60,  0.80 );

float hash( float n )
{
    return fract(sin(n)*43758.5453);
}

float noise( in vec2 x )
{
    vec2 i = floor(x);
    vec2 f = fract(x);

    f = f*f*(3.0-2.0*f);

    float n = i.x + i.y*57.0;

    return mix(mix( hash(n+ 0.0), hash(n+ 1.0),f.x),
               mix( hash(n+57.0), hash(n+58.0),f.x),f.y);
}

float fbm( vec2 p )
{
    float f = 0.0;
    f += 0.50000*noise( p ); p = m*p*2.02;
    f += 0.25000*noise( p ); p = m*p*2.03;
    f += 0.12500*noise( p ); p = m*p*2.01;
    f += 0.06250*noise( p ); p = m*p*2.04;
    f += 0.03125*noise( p );
    return f/0.984375;
}

float length2( vec2 p )
{
    vec2 q = p*p*p*p;
    return pow( q.x + q.y, 1.0/4.0 );
}

void main() {
    vec2 uv = gl_FragCoord.xy / iResolution;
    vec3 noiseColor = texture(Sampler0, uv).rgb;

    float tear = (texture(Sampler0, vec2(0.5, uv.y)).r - 0.5) * 0.06 * Strength;
    vec2 fragCoord = gl_FragCoord.xy + vec2(tear * iResolution.x, 0.0);

    vec2 p = (2.0*fragCoord-iResolution.xy)/iResolution.y;

    float r = length( p );
    float a = atan( p.y, p.x );

    r *= 1.0 + 0.2*clamp(1.0-r,0.0,1.0)*sin(4.0*iTime);

    vec3 col = vec3( 0.0, 0.3, 0.4 );
    float f = fbm( 5.0*p );
    col = mix( col, vec3(0.2,0.5,0.4), f );

    col = mix( col, vec3(0.9,0.6,0.2), 1.0-smoothstep(0.2,0.6,r) );

    f = smoothstep( 0.4, 0.9, fbm( vec2(15.0*a,10.0*r) ) );
    col *= 1.0-0.5*f;

    a += 0.05*fbm( 20.0*p );

    f = smoothstep( 0.3, 1.0, fbm( vec2(20.0*a,6.0*r) ) );
    col = mix( col, vec3(1.0,1.0,1.0), f );

    col *= 1.0-0.25*smoothstep( 0.6,0.8,r );

    f = 1.0-smoothstep( 0.0, 0.6, length2( mat2(0.6,0.8,-0.8,0.6)*(p-vec2(0.3,0.5) )*vec2(1.0,2.0)) );
    col += vec3(1.0,0.9,0.9)*f*0.985;

    col *= vec3(0.8+0.2*cos(r*a));

    f = 1.0-smoothstep( 0.2, 0.25, r );
    col = mix( col, vec3(0.0), f );

    float mask = (1.0 - smoothstep( 0.79, 0.82, r )) * Strength;
    float gray = clamp(dot(col, vec3(0.299, 0.587, 0.114)), 0.0, 1.0);

    vec3 eye = vec3(gray) * (0.7 + 0.5 * noiseColor.r);
    vec3 result = mix(noiseColor, eye, mask * 0.4);

    fragColor = vec4(result, 1.0);
}
