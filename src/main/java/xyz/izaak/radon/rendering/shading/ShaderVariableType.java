package xyz.izaak.radon.rendering.shading;

/**
 * Created by ibaker on 17/08/2016.
 */
public enum ShaderVariableType {
    // scalars
    BOOL, INT, UINT, FLOAT, DOUBLE,

    // vectors
    VEC2, VEC3, VEC4,

    // matrices
    MAT2X2, MAT2X3, MAT2X4,
    MAT3X2, MAT3X3, MAT3X4,
    MAT4X2, MAT4X3, MAT4X4,
    MAT2, MAT3, MAT4,

    // samplers
    SAMPLER_1D("sampler1D"),
    SAMPLER_2D("sampler2D"),
    SAMPLER_3D("sampler3D"),
    SAMPLER_CUBE("samplerCube"),
    SAMPLER_2D_RECT("sampler2DRect"),
    SAMPLER_1D_ARRAY("sampler1DArray"),
    SAMPLER_2D_ARRAY("sampler2DArray"),
    SAMPLER_CUBE_ARRAY("samplerCubeArray"),
    SAMPLER_BUFFER("samplerBuffer"),
    SAMPLER_2D_MS("sampler2DMS"),
    SAMPLER_2D_MS_ARRAY("sampler2DMSArray"),

    // shadow samplers
    SAMPLER_1D_SHADOW("sampler1DShadow"),
    SAMPLER_2D_SHADOW("sampler2DShadow"),
    SAMPLER_CUBE_SHADOW("samplerCubeShadow"),
    SAMPLER_2D_RECT_SHADOW("sampler2DRectShadow"),
    SAMPLER_1D_ARRAY_SHADOW("sampler1DArrayShadow"),
    SAMPLER_2D_ARRAY_SHADOW("sampler2DArrayShadow"),
    SAMPLER_CUBE_ARRAY_SHADOW("samplerCubeArrayShadow");

    private String type;
    private int[] length;

    public String getTypeString() {
        return type;
    }

    public int[] getLength() {
        return length;
    }

    public ShaderVariableType array(int... length) {
        this.length = length;
        return this;
    }

    public ShaderVariableType g(char modifier) {
        type = modifier + type;
        return this;
    }

    ShaderVariableType(String type) {
        this.type = type;
    }

    ShaderVariableType() {
        this.type = this.toString().toLowerCase();
    }

}
