package xyz.izaak.radon;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import xyz.izaak.radon.gamesystem.GameSystem;

import org.joml.Vector2f;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;

import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import static org.lwjgl.glfw.GLFW.nglfwGetCursorPos;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_INVALID_ENUM;
import static org.lwjgl.opengl.GL11.GL_INVALID_OPERATION;
import static org.lwjgl.opengl.GL11.GL_INVALID_VALUE;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_OUT_OF_MEMORY;
import static org.lwjgl.opengl.GL11.GL_STACK_OVERFLOW;
import static org.lwjgl.opengl.GL11.GL_STACK_UNDERFLOW;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION;

import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glClearStencil;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glLineWidth;

/**
 * A {@link Game} is the fundamental Radon class, representing the entirety of a running game application. All of a
 * Game's functionality should be attached to a Game via implementations of {@link GameSystem} which are added to the
 * Game object using the {@link #addGameSystem(GameSystem)} method. Once all systems have been added, the Game can
 * be executed using {@link #run()}.
 */
public class Game {

    private float previousTime;
    private Vector2f previousMousePosition;
    private Vector2f mouseDelta;
    private Vector3f clearColor;
    private long window;
    private int width;
    private int height;
    private String name;
    private List<GameSystem> gameSystems;
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;

    /**
     * Constructs a new Game instance
     *
     * @param name the title of the Game, which will also be the title of the application window
     * @param width the initial width of the Game application window
     * @param height the initial height of the Game application window
     * @param clearColor the initial argument to glClearColor (the background color of the game world)
     */
    public Game(String name, int width, int height, Vector3f clearColor) {
        this.name = name;
        this.gameSystems = new ArrayList<>();
        this.width = width;
        this.height = height;
        this.clearColor = clearColor;
        this.mouseDelta = new Vector2f();
    }

    public long getWindow() {
        return window;
    }

    /**
     * Adds a {@link GameSystem} to this Game instance. GameSystems will be initialized and receive callbacks
     * in the order that they are passed to this function.
     *
     * @param gameSystem the GameSystem to be added
     */
    public void addGameSystem(GameSystem gameSystem) {
        gameSystems.add(gameSystem);
    }

    /**
     * Begin the execution of this Game instance.
     */
    public void run() {
        initializeGlfw();
        createWindow();
        registerCallbacks();
        initializeGl();
        Channel.stopAll();
        initializeGameSystems();
        Channel.flowAll();
        loop();
        System.out.printf("Game %s has run!%n", name);
    }

    /**
     * Poll OpenGL for error flags, exiting the application with that exit code should the value be anything other
     * than GL_NO_ERROR
     *
     * @param message a message to be printed in addition to the error description
     */
    public static void exitOnGlErrorWithMessage(String message) {
        exitOnGlErrorWithMessage(message, false);
    }

    /**
     * Poll OpenGL for error flags, exiting the application with that exit code should the value be anything other
     * than GL_NO_ERROR. Also optionally print on success.
     *
     * @param message a message to be printed in addition to the error description
     * @param printSuccessOutput whether to also print in the case of GL_NO_ERROR
     */
    public static void exitOnGlErrorWithMessage(String message, boolean printSuccessOutput) {
        int errorValue = glGetError();
        switch(errorValue) {
            case GL_INVALID_ENUM:
                System.err.println(message + " (Invalid Enum, an unacceptable value is specified for an enumerated argument)"); break;
            case GL_INVALID_VALUE:
                System.err.println(message + " (Invalid Value, a numeric argument is out of range)"); break;
            case GL_INVALID_OPERATION:
                System.err.println(message + " (Invalid Operation, the specified operation is not allowed in the current state)"); break;
            case GL_INVALID_FRAMEBUFFER_OPERATION:
                System.err.println(message + " (Invalid Framebuffer Operation, the framebuffer object is not complete)"); break;
            case GL_OUT_OF_MEMORY:
                System.err.println(message + " (Out of Memory, there is not enough memory left to execute the command)"); break;
            case GL_STACK_OVERFLOW:
                System.err.println(message + " (An attempt has been made to perform an operation that would cause an internal stack to underflow)"); break;
            case GL_STACK_UNDERFLOW:
                System.err.println(message + " (An attempt has been made to perform an operation that would cause an internal stack to overflow)"); break;
            case GL_NO_ERROR:
                if (printSuccessOutput) {
                    System.out.println(message + " (No GL Error Detected!)");
                }
                return;
        }
        System.exit(errorValue);
    }

    private void initializeGlfw() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        previousTime = (float) glfwGetTime();
    }

    private void createWindow() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        window = glfwCreateWindow(width, height, name, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (videoMode.width() - width) / 2, (videoMode.height() - height) / 2);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    private void registerCallbacks() {
        GLFWErrorCallback.createPrint(System.err).set();
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                switch (action) {
                    case GLFW_PRESS:
                        for (int i = 0; i < gameSystems.size(); i++) {
                            gameSystems.get(i).onKeyDown(key);
                            gameSystems.get(i).onKeyHeld(key);
                        }
                        break;
                    case GLFW_REPEAT:
                        for (int i = 0; i < gameSystems.size(); i++) {
                            gameSystems.get(i).onKeyRepeat(key);
                            gameSystems.get(i).onKeyHeld(key);
                        }
                        break;
                    case GLFW_RELEASE:
                        for (int i = 0; i < gameSystems.size(); i++) {
                            gameSystems.get(i).onKeyUp(key);
                        }
                        break;
                }
            }
        });
        glfwSetCursorPosCallback(window, cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xPos, double yPos) {
                if (previousMousePosition == null) previousMousePosition = new Vector2f((float) xPos, (float) yPos);
                mouseDelta.set((float) xPos, (float) yPos).sub(previousMousePosition);
                previousMousePosition.set((float) xPos, (float) yPos);
                for (int i = 0; i < gameSystems.size(); i++) {
                    gameSystems.get(i).onMouseMove(mouseDelta);
                }
            }
        });
        glfwSetMouseButtonCallback(window, mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                switch (action) {
                    case GLFW_PRESS:
                        for (int i = 0; i < gameSystems.size(); i++) {
                            gameSystems.get(i).onMouseDown(previousMousePosition, button);
                        }
                        break;
                    case GLFW_RELEASE:
                        for (int i = 0; i < gameSystems.size(); i++) {
                            gameSystems.get(i).onMouseUp(previousMousePosition, button);
                        }
                        break;
                }
            }
        });
    }

    private void initializeGl() {
        GL.createCapabilities();

        glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
        glClearStencil(0);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glLineWidth(1.0f);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glfwSwapBuffers(window);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    private void initializeGameSystems() {
        gameSystems.forEach(GameSystem::initialize);
    }

    private void loop() {
        float rightNow, elapsedTime;
        while (!glfwWindowShouldClose(window)) {

            rightNow = (float) glfwGetTime();
            elapsedTime = rightNow - previousTime;
            previousTime = rightNow;

            for (int i = 0; i < gameSystems.size(); i++) {
                gameSystems.get(i).update(elapsedTime);
            }

            glfwSwapBuffers(window);
            if (!glfwWindowShouldClose(window)) glfwPollEvents();

            exitOnGlErrorWithMessage("Error!");
        }
    }
}
