package xyz.izaak.radon.primitive.geometry;

import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import org.joml.Vector3f;
import org.joml.Vector3i;
import xyz.izaak.radon.math.MarchingCubes;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.math.field.ScalarVolume;
import xyz.izaak.radon.primitive.Primitive;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static xyz.izaak.radon.math.MarchingCubes.EDGES;
import static xyz.izaak.radon.math.MarchingCubes.DELTA;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_NORMAL;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;

/**
 * Extracts a polygonal isosurface from a scalar field using the Marching Cubes algorithm, with edge and vertex
 * indices as described http://paulbourke.net/geometry/polygonise/. Relevant indexing and lookup tables are static
 * fields of {@link MarchingCubes}
 */
public class IsosurfaceGeometry extends Geometry {
    private static final float EPSILON = 0.00001f;

    private ScalarVolume scalarVolume;
    private Vector3f min;
    private Vector3i dimensions;
    private float isolevel;
    private float fidelity;
    private float[][][] samples;
    private List<Vector3f> vertices;

    /**
     * Constructs a new IsosurfaceGeometry from a specified volume of a scalar field
     *
     * @param scalarVolume the scalar field function to perform extraction
     * @param min the bottom-left-back corner of the specified volume
     * @param dimensions the number of cubes along each axis
     * @param isolevel the level at which to construct the isosurface
     * @param fidelity the edge length of a single cube
     */
    public IsosurfaceGeometry(
            ScalarVolume scalarVolume,
            Vector3f min,
            Vector3i dimensions,
            float isolevel,
            float fidelity) {

        this.scalarVolume = scalarVolume;
        this.min = min;
        this.dimensions = dimensions;
        this.isolevel = isolevel;
        this.fidelity = fidelity;
        this.samples = new float[dimensions.x + 1][dimensions.y + 1][dimensions.z + 1];
        this.runMarchingCubes();
    }

    private int computeCubeIndex(int x, int y, int z) {
        int cubeIndex = 0;
        int xIndex, yIndex, zIndex;
        float sample;
        for (int i = 0; i < 8; i++) {
            xIndex = x + DELTA[i][0];
            yIndex = y + DELTA[i][1];
            zIndex = z + DELTA[i][2];
            sample = samples[xIndex][yIndex][zIndex];
            if (sample < isolevel) cubeIndex |= 1 << i;
        }
        return cubeIndex;
    }

    private void setToInterpolatedPoint(
            Vector3f target,
            float px, float py, float pz,
            float qx, float qy, float qz,
            float pValue, float qValue) {

        Vector3f p = new Vector3f(px, py, pz);
        Vector3f q = new Vector3f(qx, qy, qz);

        if (Math.abs(isolevel - pValue) < EPSILON) target.set(px, py, pz);
        if (Math.abs(isolevel - qValue) < EPSILON) target.set(qx, qy, qz);
        if (Math.abs(pValue - qValue) < EPSILON) target.set(px, py, pz);

        float c = (isolevel - pValue) / (qValue - pValue);
        target.set(px + c * (qx - px), py + c * (qy - py), pz + c * (qz - pz));
    }

    private void findPointsOnEdges(Map<Integer, Vector3f> points, int crossingEdges, int x, int y, int z) {
        for (int i = 0; i < 12; i++) {

            // crossingEdges has a 1 at index i if edge i contains a point. That point is what we're looking for
            if ((crossingEdges & (1 << i)) != 0) {

                int p = EDGES[i][0];
                int q = EDGES[i][1];

                points.put(i, new Vector3f());
                setToInterpolatedPoint(
                        // Output point along edge i
                        points.get(i),

                        // coordinates of P in space
                        min.x + (fidelity * (x + DELTA[p][0])),
                        min.y + (fidelity * (y + DELTA[p][1])),
                        min.z + (fidelity * (z + DELTA[p][2])),

                        // coordinates of Q in space
                        min.x + (fidelity * (x + DELTA[q][0])),
                        min.y + (fidelity * (y + DELTA[q][1])),
                        min.z + (fidelity * (z + DELTA[q][2])),

                        // sampled value at P
                        samples[x + DELTA[p][0]][y + DELTA[p][1]][z + DELTA[p][2]],

                        // sampled value at Q
                        samples[x + DELTA[q][0]][y + DELTA[q][1]][z + DELTA[q][2]]
                );
            }
        }
    }

    @Override
    public void build(Primitive primitive) {
        Vector3f pointA, pointB, pointC;
        Vector3f bMinusA = new Vector3f();
        Vector3f cMinusA = new Vector3f();
        Vector3f surfaceNormal = new Vector3f();

        for (int i = 0; i < vertices.size(); i += 3) {
            pointA = vertices.get(i);
            pointB = vertices.get(i + 1);
            pointC = vertices.get(i + 2);

            primitive.next(VERTEX_POSITION, pointC);
            primitive.next(VERTEX_POSITION, pointB);
            primitive.next(VERTEX_POSITION, pointA);

            bMinusA.set(pointB).sub(pointA);
            cMinusA.set(pointC).sub(pointA);
            surfaceNormal.set(cMinusA).cross(bMinusA).normalize();

            primitive.next(VERTEX_NORMAL, surfaceNormal);
            primitive.next(VERTEX_NORMAL, surfaceNormal);
            primitive.next(VERTEX_NORMAL, surfaceNormal);
        }

        primitive.addInterval(GL_TRIANGLES, vertices.size());
    }

    public TriangleIndexVertexArray getAsTriangleIndexVertexArray() {
        ByteBuffer triangleIndexBase = ByteBuffer.allocateDirect(vertices.size() * 4).order(ByteOrder.nativeOrder());
        ByteBuffer vertexBase = ByteBuffer.allocateDirect(vertices.size() * 3 * 4).order(ByteOrder.nativeOrder());

        for (int i = 0; i < vertices.size(); i++) {
            triangleIndexBase.putInt(i);
            vertexBase.putFloat(vertices.get(i).x);
            vertexBase.putFloat(vertices.get(i).y);
            vertexBase.putFloat(vertices.get(i).z);
        }

        triangleIndexBase.rewind();
        vertexBase.rewind();

        return new TriangleIndexVertexArray(
                vertices.size() / 3,
                triangleIndexBase,
                3 * 4,
                vertices.size(),
                vertexBase,
                3 * 4);
    }

    private void runMarchingCubes() {
        Vector3f cubeStep = new Vector3f(fidelity, fidelity, fidelity);
        scalarVolume.sample(samples, min, Points.copyOf(dimensions).add(1, 1, 1), cubeStep);
        vertices = new LinkedList<>();

        int cubeIndex;
        Map<Integer, Vector3f> pointsOnEdges = new HashMap<>();

        for (int x = 0; x < dimensions.x; x++) {
            for (int y = 0; y < dimensions.y; y++) {
                for (int z = 0; z < dimensions.z; z++) {

                    cubeIndex = computeCubeIndex(x, y, z);
                    int crossingEdges = MarchingCubes.EDGE_TABLE[cubeIndex];
                    findPointsOnEdges(pointsOnEdges, crossingEdges, x, y, z);
                    int[] triangles = MarchingCubes.TRIANGLE_TABLE[cubeIndex];

                    for (int i = 0; triangles[i] != -1; i += 3) {
                        vertices.add(pointsOnEdges.get(triangles[i]));
                        vertices.add(pointsOnEdges.get(triangles[i + 1]));
                        vertices.add(pointsOnEdges.get(triangles[i + 2]));
                    }

                    pointsOnEdges.clear();
                }
            }
        }

    }
}
