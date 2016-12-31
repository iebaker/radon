package xyz.izaak.radon.external;

import com.google.gson.Gson;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.izaak.radon.Resource;
import xyz.izaak.radon.math.OrthonormalBasis;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.geometry.QuadGeometry;
import xyz.izaak.radon.material.PhongMaterial;
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
public class SceneLoaderV0 implements SceneLoader {
    private static Gson gson = new Gson();

    private SceneModelV0 sceneModel;
    private Vector3f scratch = new Vector3f();

    public SceneLoaderV0(String filename) {
        String fileContents = Resource.stringFromFile(filename);
        this.sceneModel = gson.fromJson(fileContents, SceneModelV0.class);
    }

    public Scene newInstance() {
        PhongMaterial phongMaterial = new PhongMaterial(Points.WHITE, Points.WHITE, Points.WHITE, Points.BLACK, 50.0f);

        Scene scene = Scene.builder().name(sceneModel.sceneName).build();
        Entity roomEntity = Entity.builder().build();

        Map<Integer, List<Float>> portalMap = new HashMap<>();
        int portalCount = sceneModel.portals.length;
        for (int i = 0; i < portalCount; i++) {
            int wallIndex = (int) sceneModel.portals[i][0];
            if (!portalMap.containsKey(wallIndex)) {
                portalMap.put(wallIndex, new ArrayList<>());
            }
            portalMap.get(wallIndex).add(sceneModel.portals[i][1]);
        }

        int corners = sceneModel.footprint.length;
        Vector2f corner = new Vector2f();
        Vector2f nextCorner = new Vector2f();
        Matrix4f rotation;

        Vector2f maxCorner = new Vector2f(Float.MIN_VALUE, Float.MIN_VALUE);
        Vector2f minCorner = new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
        for (int i = 0; i < corners; i++) {
            corner.set(sceneModel.footprint[i][0], sceneModel.footprint[i][1]);
            maxCorner.x = Math.max(maxCorner.x, corner.x);
            maxCorner.y = Math.max(maxCorner.y, corner.y);
            minCorner.x = Math.min(minCorner.x, corner.x);
            minCorner.y = Math.min(minCorner.y, corner.y);
        }

        QuadGeometry floorGeometry = new QuadGeometry();
        Mesh floorMesh = new Mesh(floorGeometry, phongMaterial);
        floorMesh.translate(0.5f, 0.5f, 0.0f);
        floorMesh.scale(maxCorner.x - minCorner.x, maxCorner.y - minCorner.y, 1.0f);
        floorMesh.translate(minCorner.x, minCorner.y, 0.0f);
        roomEntity.addMeshes(floorMesh);

        QuadGeometry ceilingGeometry = new QuadGeometry();
        Mesh ceilingMesh = new Mesh(ceilingGeometry, phongMaterial);
        ceilingMesh.rotate(Points.piOver(1), Points._Y_);
        ceilingMesh.translate(0.5f, 0.5f, 0.0f);
        ceilingMesh.scale(maxCorner.x - minCorner.x, maxCorner.y - minCorner.y, 1.0f);
        ceilingMesh.translate(minCorner.x, minCorner.y, sceneModel.height);
        roomEntity.addMeshes(ceilingMesh);

        for (int i = 0; i <= corners; i++) {
            if (i == 0) {
                corner.set(0, 0);
                nextCorner.set(sceneModel.footprint[i][0], sceneModel.footprint[i][1]);
            } else if (i == corners) {
                corner.set(sceneModel.footprint[i - 1][0], sceneModel.footprint[i - 1][1]);
                nextCorner.set(0, 0);
            } else {
                corner.set(sceneModel.footprint[i - 1][0], sceneModel.footprint[i - 1][1]);
                nextCorner.set(sceneModel.footprint[i][0], sceneModel.footprint[i][1]);
            }

            scratch.set(corner.x, corner.y, 0.0f).sub(nextCorner.x, nextCorner.y, 0.0f);
            float wallWidth = scratch.length();
            OrthonormalBasis wallBasis = new OrthonormalBasis(scratch.normalize(), Points.__Z);
            rotation = OrthonormalBasis.rotationTo(wallBasis);
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

                    scratch.mul(portalPosition).add(corner.x, corner.y, Portal.PORTAL_DIMENSIONS.y / 2);
                    Portal portal = new Portal(scratch, wallBasis);
                    scene.addPortal(portal);
                    scratch.sub(corner.x, corner.y, Portal.PORTAL_DIMENSIONS.y / 2).normalize();

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

                float remainingHeight = sceneModel.height - Portal.PORTAL_DIMENSIONS.y;

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
                wallMesh.scale(wallWidth, sceneModel.height, 1.0f);
                wallMesh.transform(rotation);

                scratch.mul(wallWidth / 2).add(corner.x, corner.y, sceneModel.height / 2);
                wallMesh.translate(scratch);
                roomEntity.addMeshes(wallMesh);
            }
        }

        scene.addEntity(roomEntity);
        return scene;
    }
}
