package xyz.izaak.radon.primitive.geometry;

import org.joml.Vector3f;
import org.joml.Vector3i;
import xyz.izaak.radon.math.MarchingCubes;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.math.field.ScalarVolume;
import xyz.izaak.radon.primitive.Primitive;

import java.util.HashMap;
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

        // Sample from the scalar volume at the edges of "dimensions" cubes spaced "fidelity" apart
        // starting from "min," storing the result in "samples"
        Vector3f cubeStep = new Vector3f(fidelity, fidelity, fidelity);
        scalarVolume.sample(samples, min, Points.copyOf(dimensions).add(1, 1, 1), cubeStep);

        int cubeIndex;
        int triangleCount = 0;
        Map<Integer, Vector3f> pointsOnEdges = new HashMap<>();

        // (x, y, z) is an index into the volume of cubes
        for (int x = 0; x < dimensions.x; x++) {
            for (int y = 0; y < dimensions.y; y++) {
                for (int z = 0; z < dimensions.z; z++) {

                    cubeIndex = computeCubeIndex(x, y, z);
                    int crossingEdges = MarchingCubes.EDGE_TABLE[cubeIndex];
                    findPointsOnEdges(pointsOnEdges, crossingEdges, x, y, z);
                    int[] triangles = MarchingCubes.TRIANGLE_TABLE[cubeIndex];

                    for (int i = 0; triangles[i] != -1; i += 3) {

                        primitive.next(VERTEX_POSITION, pointsOnEdges.get(triangles[i]));
                        primitive.next(VERTEX_POSITION, pointsOnEdges.get(triangles[i + 1]));
                        primitive.next(VERTEX_POSITION, pointsOnEdges.get(triangles[i + 2]));

                        // Set all normals on this face to the face normal. This doesn't lend itself nicely
                        // to smooth shading... but whatever.
                        primitive.next(VERTEX_NORMAL, pointsOnEdges.get(triangles[i + 1]));
                        primitive.next(VERTEX_NORMAL, pointsOnEdges.get(triangles[i + 1]));
                        primitive.next(VERTEX_NORMAL, pointsOnEdges.get(triangles[i + 1]));

                        triangleCount++;
                    }

                    pointsOnEdges.clear();
                }
            }
        }

        primitive.addInterval(GL_TRIANGLES, 3 * triangleCount);
    }
}
