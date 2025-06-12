package io.github.labyrinthgenerator.pages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import io.github.labyrinthgenerator.MyApplication;
import io.github.labyrinthgenerator.interfaces.ApplicationFacade;

public class Game3dPage implements Page {

    private ApplicationFacade application;
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Model model;
    private ModelInstance instance;
    private Environment environment;
    private CameraInputController camController;;

    @Override
    public void create() {
        application = MyApplication.getApplicationInstance();
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 10f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();
        modelBatch = new ModelBatch();

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(5f, 5f, 5f,
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        instance = new ModelInstance(model);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(camController);
    }

    @Override
    public void input() {

    }

    @Override
    public void logic() {

    }

    @Override
    public void draw() {
        prepareDraw();
        modelBatch.render(instance, environment);
        endDraw();
    }

    public void prepareDraw() {
        camController.update();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        modelBatch.begin(camera);
    }

    public void endDraw() {
        modelBatch.end();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public Page getNextPage() {
        return null;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        model.dispose();
    }

    @Override
    public Camera getCamera() {
        return camera;
    }
}
