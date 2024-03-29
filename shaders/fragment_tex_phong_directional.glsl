
out vec4 outputColor;

uniform vec4 input_color;

uniform mat4 view_matrix;

// Light properties
uniform int lightType;
uniform vec3 lightVec;
uniform vec3 torchPos;
uniform vec3 torchDir;
uniform vec3 torchIntensity;
uniform vec3 lightIntensity;
uniform vec3 ambientIntensity;

// Material properties
uniform vec3 ambientCoeff;
uniform vec3 diffuseCoeff;
uniform vec3 specularCoeff;
uniform float phongExp;

uniform sampler2D tex;

in vec4 viewPosition;
in vec3 m;

in vec2 texCoordFrag;

void main()
{
    // Compute the s, v and r vectors
    vec3 s;
    vec3 torch;
    vec3 torchAmbient;
    float attenuation = 0.8;
    s = normalize((view_matrix*vec4(lightVec, 0)).xyz);
    vec3 v = normalize(-viewPosition.xyz);
    vec3 r = normalize(reflect(-s,m));

    if (lightType==1) {
        torch = normalize((view_matrix*vec4(torchPos, 1) - viewPosition).xyz);

        float distanceToLight = length(vec4(torchPos, 1) - viewPosition);
        attenuation = 1.0 / (1.0 + 0.8 * pow(distanceToLight, 2));
        float lightToSurfaceAngle = degrees(acos(dot(-torch, normalize(torchDir))));
        if(lightToSurfaceAngle > 60){
            attenuation = 0.0;
        }
        torchAmbient = torchIntensity*ambientCoeff*attenuation;
    }

    vec3 ambient = ambientIntensity*ambientCoeff;
    vec3 diffuse = max(lightIntensity*diffuseCoeff*dot(m,s), 0.0);

    vec3 specular;

    // Only show specular reflections for the front face
    if ((dot(m,s) > 0) && lightType==0)
        specular = max(lightIntensity*specularCoeff*pow(dot(r,v),phongExp), 0.0);
    else
        specular = vec3(0);

    vec4 ambientAndDiffuse = vec4(ambient + diffuse, 1);
    outputColor = ambientAndDiffuse*input_color*texture(tex, texCoordFrag) ;//+ vec4(specular, 1);
    if (lightType==1) {
        outputColor += vec4(torchAmbient, 1);
    }
}
