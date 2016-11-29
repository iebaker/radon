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
import xyz.izaak.radon.world.Entity;
import xyz.izaak.radon.world.Scene;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ibaker on 28/11/2016.
 */
public class JBulletPhysicsSystem implements GameSystem {
    private Scene scene;
    private DynamicsWorld dynamicsWorld;
    private Map<Entity, RigidBody> rigidBodiesByEntity;

    public JBulletPhysicsSystem(Scene scene) {
        this.scene = scene;
        this.rigidBodiesByEntity = new HashMap<>();
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

            RigidBody entityRigidBody = new RigidBody(entityBodyConstructionInfo);
            dynamicsWorld.addRigidBody(entityRigidBody);
            rigidBodiesByEntity.put(entity, entityRigidBody);
        });
    }

    @Override
    public void update(float seconds) {
        dynamicsWorld.stepSimulation(seconds);
        rigidBodiesByEntity.entrySet().forEach(entry -> {
            Entity entity = entry.getKey();
            RigidBody rigidBody = entry.getValue();

            Transform entityTransform = new Transform();
            rigidBody.getMotionState().getWorldTransform(entityTransform);
            float[] matrixData = new float[16];
            entityTransform.getOpenGLMatrix(matrixData);

            Matrix4f entityMatrix = new Matrix4f(matrixData);
            entity.setTransform(Points.toJoml(entityMatrix));
        });
    }
}
