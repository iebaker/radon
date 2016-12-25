package xyz.izaak.radon.external;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.json.JSONArray;
import org.json.JSONObject;
import xyz.izaak.radon.Resource;
import xyz.izaak.radon.math.OrthonormalBasis;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.mesh.geometry.QuadGeometry;
import xyz.izaak.radon.mesh.material.PhongMaterial;
import xyz.izaak.radon.world.Entity;
import xyz.izaak.radon.world.Portal;
import xyz.izaak.radon.world.Scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ibaker on 24/12/2016.
 */
public class SceneLoaderV0 {
    private static Vector3f scratch = new Vector3f();

    public static Scene load(String filename) {
        PhongMaterial phongMaterial = new PhongMaterial(Points.WHITE, Points.WHITE, Points.WHITE, Points.BLACK, 50.0f);

        JSONObject sceneJson = new JSONObject(Resource.stringFromFile(filename));
        String sceneName = sceneJson.getString("SceneName");
        Scene scene = Scene.builder().name(sceneName).build();
        Entity roomEntity = Entity.builder().build();

        float height = (float) sceneJson.getDouble("Height");

        Map<Integer, List<Float>> portalMap = new HashMap<>();
        JSONArray portalJson = sceneJson.getJSONArray("Portals");
        int portalCount = portalJson.length();
        for (int i = 0; i < portalCount; i++) {
            JSONArray singlePortalJson = portalJson.getJSONArray(i);
            int wallIndex = singlePortalJson.getInt(0);
            if (!portalMap.containsKey(wallIndex)) {
                portalMap.put(wallIndex, new ArrayList<>());
            }
            portalMap.get(wallIndex).add((float) singlePortalJson.getDouble(1));
        }

        JSONArray footprint = sceneJson.getJSONArray("Footprint");
        int corners = footprint.length();
        Vector2f corner = new Vector2f();
        Vector2f nextCorner = new Vector2f();
        Matrix4f rotation;

        Vector2f maxCorner = new Vector2f(Float.MIN_VALUE, Float.MIN_VALUE);
        for (int i = 0; i < corners; i++) {
            Points.from2f(footprint.getJSONArray(i), corner);
            maxCorner.x = Math.max(maxCorner.x, corner.x);
            maxCorner.y = Math.max(maxCorner.y, corner.y);
        }

        QuadGeometry floorGeometry = new QuadGeometry();
        Mesh floorMesh = new Mesh(floorGeometry, phongMaterial);
        floorMesh.translate(0.5f, 0.5f, 0.0f);
        floorMesh.scale(maxCorner.x, maxCorner.y, 1.0f);
        roomEntity.addMeshes(floorMesh);

        QuadGeometry ceilingGeometry = new QuadGeometry();
        Mesh ceilingMesh = new Mesh(ceilingGeometry, phongMaterial);
        ceilingMesh.rotate(Points.piOver(1), Points._Y_);
        ceilingMesh.translate(0.5f, 0.5f, 0.0f);
        ceilingMesh.scale(maxCorner.x, maxCorner.y, 1.0f);
        ceilingMesh.translate(0, 0, height);
        roomEntity.addMeshes(ceilingMesh);

        for (int i = 0; i <= corners; i++) {
            if (i == 0) {
                corner.set(0, 0);
                Points.from2f(footprint.getJSONArray(i), nextCorner);
            } else if (i == corners) {
                Points.from2f(footprint.getJSONArray(i - 1), corner);
                nextCorner.set(0, 0);
            } else {
                Points.from2f(footprint.getJSONArray(i - 1), corner);
                Points.from2f(footprint.getJSONArray(i), nextCorner);
            }

            System.out.printf("%d: (%.1f, %.1f) -> (%.1f, %.1f)%n", i, corner.x, corner.y, nextCorner.x, nextCorner.y);

            scratch.set(corner.x, corner.y, 0.0f).sub(nextCorner.x, nextCorner.y, 0.0f);
            float wallWidth = scratch.length();
            rotation = OrthonormalBasis.rotationTo(new OrthonormalBasis(scratch.normalize(), Points.__Z));
            scratch.negate();

            if (portalMap.containsKey(i)) {
                Collections.sort(portalMap.get(i));

                float currentPosition = 0.0f;
                for (Float portalPosition : portalMap.get(i)) {
                    float width = (portalPosition - Portal.PORTAL_DIMENSIONS.x / 2) - currentPosition;
                    float distanceAlongWall = currentPosition + width / 2;

                    QuadGeometry wallSegmentGeometry = new QuadGeometry();
                    Mesh wallSegmentMesh = new Mesh(wallSegmentGeometry, phongMaterial);
                    wallSegmentMesh.scale(width, Portal.PORTAL_DIMENSIONS.y, 1.0f);
                    wallSegmentMesh.transform(rotation);

                    scratch.mul(distanceAlongWall).add(corner.x, corner.y, Portal.PORTAL_DIMENSIONS.y / 2);
                    wallSegmentMesh.translate(scratch);
                    scratch.sub(corner.x, corner.y, Portal.PORTAL_DIMENSIONS.y / 2).normalize();
                    roomEntity.addMeshes(wallSegmentMesh);

                    currentPosition = portalPosition + Portal.PORTAL_DIMENSIONS.x / 2;
                }

                float finalWidth = wallWidth - currentPosition;
                float finalDistanceAlongWall = currentPosition + finalWidth / 2;

                QuadGeometry finalWallSegmentGeometry = new QuadGeometry();
                Mesh finalWallSegmentMesh = new Mesh(finalWallSegmentGeometry, phongMaterial);
                finalWallSegmentMesh.scale(finalWidth, Portal.PORTAL_DIMENSIONS.y, 1.0f);
                finalWallSegmentMesh.transform(rotation);

                scratch.mul(finalDistanceAlongWall).add(corner.x, corner.y, Portal.PORTAL_DIMENSIONS.y / 2);
                finalWallSegmentMesh.translate(scratch);
                scratch.sub(corner.x, corner.y, Portal.PORTAL_DIMENSIONS.y / 2).normalize();
                roomEntity.addMeshes(finalWallSegmentMesh);

                float remainingHeight = height - Portal.PORTAL_DIMENSIONS.y;

                QuadGeometry wallTopGeometry = new QuadGeometry();
                Mesh wallTopMesh = new Mesh(wallTopGeometry, phongMaterial);
                wallTopMesh.scale(wallWidth, remainingHeight, 1.0f);
                wallTopMesh.transform(rotation);

                scratch.mul(wallWidth / 2).add(corner.x, corner.y, Portal.PORTAL_DIMENSIONS.y + remainingHeight / 2);
                wallTopMesh.translate(scratch);
                roomEntity.addMeshes(wallTopMesh);

            } else {

                QuadGeometry wallGeometry = new QuadGeometry();
                Mesh wallMesh = new Mesh(wallGeometry, phongMaterial);
                wallMesh.scale(wallWidth, height, 1.0f);
                wallMesh.transform(rotation);

                scratch.mul(wallWidth / 2).add(corner.x, corner.y, height / 2);
                wallMesh.translate(scratch);
                roomEntity.addMeshes(wallMesh);
            }
        }

        scene.addEntity(roomEntity);
        return scene;
    }
}
