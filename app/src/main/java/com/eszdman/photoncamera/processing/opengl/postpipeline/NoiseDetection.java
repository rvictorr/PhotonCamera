package com.eszdman.photoncamera.processing.opengl.postpipeline;

import com.eszdman.photoncamera.processing.opengl.GLFormat;
import com.eszdman.photoncamera.processing.opengl.GLProg;
import com.eszdman.photoncamera.processing.opengl.GLTexture;
import com.eszdman.photoncamera.processing.opengl.nodes.Node;

public class NoiseDetection extends Node {
    public NoiseDetection(int rid, String name) {
        super(rid, name);
    }

    @Override
    public void Run() {
        PostPipeline postPipeline = (PostPipeline) super.basePipeline;
        GLProg glProg = basePipeline.glint.glProgram;
        glProg.setTexture("InputBuffer", previousNode.WorkingTexture);
        WorkingTexture = new GLTexture(previousNode.WorkingTexture.mSize, new GLFormat(GLFormat.DataType.FLOAT_16), null);
    }
}
