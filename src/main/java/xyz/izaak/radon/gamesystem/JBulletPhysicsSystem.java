package xyz.izaak.radon.gamesystem;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.scene.Entity;
import xyz.izaak.radon.scene.Scene;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ibaker on 28/11/2016.
 */
public class JBulletPhysicsSystem implements GameSystem {
    private Scene scene;
    private DynamicsWorld dynamicsWorld;
    private Transform entityTransform;
    private float[] matrixData;
    private Map<Entity, RigidBody> rigidBodiesByEntity;

    public JBulletPhysicsSystem(Scene scene) {
        this.scene = scene;
        this.rigidBodiesByEntity = new HashMap<>();
        this.entityTransform = new Transform();
        this.matrixData = new float[16];
    }

    @Override
    public void initialize() {
        BroadphaseInterface broadphaseInterface = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher collisionDispatcher = new CollisionDispatcher(collisionConfiguration);
        ConstraintSolver constraintSolver = new SequentialImpulseConstraintSolver();

        dynamicsWorld = new DiscreteDynamicsWorld(
                collisionDispatcher,
                broadphaseInterface,
                constraintSolver,
                collisionConfiguration
        );

        dynamicsWorld.setGravity(Points.toJavax(scene.getGravity()));
        scene.getEntities().forEach(entity -> {
            Transform entityTransform = new Transform();

            float[] matrixData = new float[16];
            entity.getModel().get(matrixData);
            entityTransform.setFromOpenGLMatrix(matrixData);
            MotionState entityGroundMotionState = new DefaultMotionState(entityTransform);
            Vector3f entityInertia = new Vector3f(0, 0, 0);
            entity.getCollisionShape().calculateLocalInertia(entity.getMass(), entityInertia);

            RigidBodyConstructionInfo entityBodyConstructionInfo = new RigidBodyConstructionInfo(
                    entity.getMass(),
                    entityGroundMotionState,
                    entity.getCollisionShape(),
                    entityInertia
            );

            entityBodyConstructionInfo.restitution = entity.getRestitution();
            entityBodyConstructionInfo.angularDamping = entity.getFriction();
            entityBodyConstructionInfo.friction = entity.getFriction();

            RigidBody entityRigidBody = new RigidBody(entityBodyConstructionInfo);
            dynamicsWorld.addRigidBody(entityRigidBody);
            rigidBodiesByEntity.put(entity, entityRigidBody);
        });
    }

    @Override
    public void update(float seconds) {
        dynamicsWorld.stepSimulation(seconds);
        List<Entity> entities = scene.getEntities();
        int entityCount = entities.size();
        for (int i = 0; i < entityCount; i++) {
            RigidBody rigidBody = rigidBodiesByEntity.get(entities.get(i));
            if (rigidBody == null) continue;
            rigidBody.getMotionState().getWorldTransform(entityTransform);
            entityTransform.getOpenGLMatrix(matrixData);
            entities.get(i).setTransform(matrixData);
        }
    }
}
