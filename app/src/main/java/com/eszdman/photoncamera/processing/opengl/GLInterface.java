package com.eszdman.photoncamera.processing.opengl;

import androidx.annotation.RawRes;

import com.eszdman.photoncamera.R;
import com.eszdman.photoncamera.app.PhotonCamera;
import com.eszdman.photoncamera.processing.render.Parameters;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.InputStreamReader;
import java.io.StringReader;

public class GLInterface {
    public final GLProg glProgram;
    public Parameters parameters;
    public GLCoreBlockProcessing glProcessing;
    public GLContext glContext;
    public GLUtils glUtils;

    public GLInterface(GLCoreBlockProcessing processing) {
        glProcessing = processing;
        glProgram = glProcessing.mProgram;
        glUtils = new GLUtils(glProcessing);
    }

    public GLInterface(GLContext context) {
        glContext = context;
        glProgram = glContext.mProgram;
    }
    public static String loadShader(String code){
        BufferedReader reader = new BufferedReader(new StringReader(code));
        return readprog(reader);
    }
    public static String loadShader(int fragment){
        BufferedReader reader = new BufferedReader(new InputStreamReader(PhotonCamera.getCameraActivity().getResources().openRawResource(fragment)));
        return readprog(reader);
    }
    static public String readprog(BufferedReader reader) {
        StringBuilder source = new StringBuilder();
        StringBuilder imports = new StringBuilder();
        for (Object line : reader.lines().toArray()) {
            String val = String.valueOf(line);
            if(val.contains("#import")){
                val = val.replace("\n","").replace(" ","").toLowerCase();
                @RawRes
                int id = 0;
                String headers = "";
                switch (val){
                    case "#importxyytoxyz":
                        id = R.raw.import_xyy2xyz;
                        headers+="vec3 xyYtoXYZ(vec3);";
                        break;
                    case "#importxyztoxyy":
                        id = R.raw.import_xyz2xyy;
                        headers+="vec3 XYZtoxyY(vec3);";
                        break;
                    case "#importsigmoid":
                        id = R.raw.import_sigmoid;
                        headers+="float sigmoid(float, float);";
                        break;
                    case "#importgaussian":
                        id = R.raw.import_gaussian;
                        headers+="float unscaledGaussian(float, float);";
                        headers+="vec3 unscaledGaussian(vec3, float);";
                        headers+="vec3 unscaledGaussian(vec3, vec3);";
                        break;
                    case "#importcubic":
                        id = R.raw.import_cubic;
                        headers+="vec4 cubic(float);";
                        break;
                    case "#importinterpolation":
                        id = R.raw.import_interpolation;
                        headers+="vec4 cubic(float);";
                        headers+="vec4 textureLinear(sampler2D, vec2);";
                        headers+="vec4 textureBicubic(sampler2D, vec2);";
                        headers+="vec4 textureBicubicHardware(sampler2D, vec2);";
                        break;
                }
                headers+="\n";
                if(id!= 0) {
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(PhotonCamera.getCameraActivity().getResources().openRawResource(id)));
                    for (Object line2 : reader2.lines().toArray()) {
                        imports.append(line2);
                        imports.append("\n");
                    }
                }
                source.append(headers);
                continue;
            }
            source.append(line).append("\n");
        }
        source.append(imports);
        return source.toString();
    }
}
