package io.github.labyrinthgenerator.pages.game3d.cubemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.mesh.NormalMapAttribute;
import io.github.labyrinthgenerator.pages.game3d.mesh.NormalMappedMesh;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;
import lombok.extern.slf4j.Slf4j;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;

@Slf4j
public class ReflectiveCubemap {

    private final PerspectiveCamera camFb;
    private final FrameBufferCubemap fb;
    private final Cubemap cubemap;
    private final ModelInstanceBB reflectiveSphereMdlInst;

    private final CubeLab3D game;

    public ReflectiveCubemap(final Vector3 position, CubeLab3D game) {
        this.game = game;

        camFb = new PerspectiveCamera(90, 640, 480);
        camFb.position.set(0, HALF_UNIT, 0);
        camFb.lookAt(0, HALF_UNIT, 1);
        camFb.near = 0.01f;
        camFb.far = 10f;
        camFb.update();

        fb = new FrameBufferCubemap(Pixmap.Format.RGB888, 256, 256, true);
        fb.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        cubemap = fb.getColorBufferTexture();

        float radius = 1f;

        // MODEL BUILDER
        /*ModelBuilder modelBuilder = new ModelBuilder();
        Model sphereModel = modelBuilder.createSphere(
            radius / 2f, radius / 2f, radius / 2f, 32, 32,
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        // Добавляем нормальную карту в материал
        sphereModel.materials.get(0).set(normalMapAttribute);
        //sphereModel.meshes.add(normalMapMesh);*/

        // CUSTOM MESH WITH NORMAL MAP
        NormalMappedMesh normalMapMesh = createSphereNormalMapMesh(radius, 32, 32);

        Texture normalMapTexture = normalMapMesh.getNormalMapTexture();
        NormalMapAttribute normalMapAttribute = NormalMapAttribute.createDiffuse(normalMapTexture);
        normalMapAttribute.textureDescription.minFilter = Texture.TextureFilter.Nearest;
        normalMapAttribute.textureDescription.magFilter = Texture.TextureFilter.Nearest;

        Material material = new Material();
        material.set(normalMapAttribute);

        Model sphereModel = new Model();
        sphereModel.meshes.add(normalMapMesh);
        sphereModel.materials.add(material);

        // OBJECT LOADER
        /*ObjLoader objLoader = new ObjLoader();
        Model sphereModel = objLoader.loadModel(Gdx.files.internal("models/sphere.obj"));
        assert sphereModel != null;*/

        reflectiveSphereMdlInst = new ModelInstanceBB(sphereModel, position);
        log.info("sphere model loaded successfully, radius: " + reflectiveSphereMdlInst.radius);

        reflectiveSphereMdlInst.transform.setToTranslation(position);
        //reflectiveSphereMdlInst.updateTransforms(); // bug!
        camFb.position.set(position.x, position.y + reflectiveSphereMdlInst.radius / 2f, position.z);
        camFb.update();

        reflectiveSphereMdlInst.materials.get(0).set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
        //updateCubemap(game.getScreen().getEnv(), 0);
    }

    public Vector3 getPosition() {
        return camFb.position;
    }

    public void setPosition(Vector3 position) {
        camFb.position.set(position);
        camFb.update();
        reflectiveSphereMdlInst.transform.setToTranslation(position);
    }

    public void updateCubemap(final ModelBatch modelBatch, final Environment env, float delta) {
        Camera currentCam = game.getScreen().getCurrentCam();
        if (!game.getScreen().frustumCull(currentCam, reflectiveSphereMdlInst)) return;

        game.getScreen().setCurrentCam(camFb);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        fb.begin();
        while (fb.nextSide()) {
            fb.getSide().getUp(camFb.up);
            camFb.up.scl(-1);
            fb.getSide().getDirection(camFb.direction);
            camFb.update();

            ScreenUtils.clear(1, 1, 1, 1, true);

            modelBatch.begin(camFb);

            game.getEntMan().render3DAllEntities(modelBatch, env, delta, camFb.position.x, camFb.position.z);

            modelBatch.end();
        }
        fb.end();

        game.getScreen().setCurrentCam(currentCam);
    }

    public void render(ModelBatch modelBatch, Environment env, Camera cam) {
        if (game.getScreen().frustumCull(cam, reflectiveSphereMdlInst)) {
            modelBatch.render(reflectiveSphereMdlInst, env, game.getShaderProvider().getShader());
        }
    }

    public static NormalMappedMesh createSphereNormalMapMesh(float radius, int longitudeBands, int latitudeBands) {
        int totalVertices = (longitudeBands + 1) * (latitudeBands + 1);
        float[] vertices = new float[totalVertices * 3]; // x, y, z
        float[] normals = new float[totalVertices * 3]; // nx, ny, nz
        float[] texCoords = new float[totalVertices * 2]; // u, v
        short[] indices = new short[longitudeBands * latitudeBands * 6];

        int vertexIndex = 0;
        int texCoordIndex = 0;

        // Создание вершин, нормалей и текстурных координат
        for (int lat = 0; lat <= latitudeBands; lat++) {
            float theta = (float) lat * (float) Math.PI / latitudeBands;
            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);

            for (int lon = 0; lon <= longitudeBands; lon++) {
                float phi = (float) lon * 2.0f * (float) Math.PI / longitudeBands;
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);

                // Вершина
                float x = cosPhi * sinTheta;
                float y = cosTheta;
                float z = sinPhi * sinTheta;

                vertices[vertexIndex++] = x * radius;
                vertices[vertexIndex++] = y * radius;
                vertices[vertexIndex++] = z * radius;

                // Нормали
                normals[vertexIndex - 3] = x; // nx
                normals[vertexIndex - 2] = y; // ny
                normals[vertexIndex - 1] = z; // nz

                // Текстурные координаты
                texCoords[texCoordIndex++] = (float) lon / longitudeBands;
                texCoords[texCoordIndex++] = (float) lat / latitudeBands;
            }
        }

        // Создание индексов для треугольников
        int index = 0;
        for (int lat = 0; lat < latitudeBands; lat++) {
            for (int lon = 0; lon < longitudeBands; lon++) {
                int first = (lat * (longitudeBands + 1)) + lon;
                int second = first + longitudeBands + 1;

                indices[index++] = (short) first;
                indices[index++] = (short) second;
                indices[index++] = (short) (first + 1);

                indices[index++] = (short) second;
                indices[index++] = (short) (second + 1);
                indices[index++] = (short) (first + 1);
            }
        }

        if (vertices.length == 0) {
            throw new GdxRuntimeException("Vertices array is empty.");
        }


        NormalMappedMesh mesh = new NormalMappedMesh(true, totalVertices, indices.length,
            new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(),
                VertexAttribute.TexCoords(0), VertexAttribute.TexCoords(2))); // FIXME
        mesh.setVertices(vertices);
        Texture normalTex = createMeshNormalMapTexture(longitudeBands + 1, latitudeBands + 1, normals);
        mesh.setNormalMap(normalTex, texCoords);
        mesh.setIndices(indices);

        return mesh;
    }

    private static Texture createMeshNormalMapTexture(int width, int height, float[] normals) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int index = (j * width + i) * 3; // Индекс нормали
                int r = (int) ((normals[index] * 0.5f + 0.5f) * 255); // Преобразование в диапазон [0, 255]
                int g = (int) ((normals[index + 1] * 0.5f + 0.5f) * 255);
                int b = (int) ((normals[index + 2] * 0.5f + 0.5f) * 255);
                int rgba = (((r & 255) << 24) | ((g & 255) << 16) | ((b & 255) << 8) | 255);
                pixmap.drawPixel(i, j, rgba);
            }
        }
        Texture normalTexture = new Texture(pixmap);

        // Используем Gdx.files.local для записи
        FileHandle fileHandle = Gdx.files.local("textures/normal_map.png");
        fileHandle.parent().mkdirs(); // Создаем директорию, если она не существует.
        PixmapIO.writePNG(fileHandle, pixmap);

        pixmap.dispose();
        return normalTexture;
    }


    public void dispose() {
        fb.dispose();
        cubemap.dispose();
    }
}

/*float scale = 2f / reflectiveSphereMdlInst.radius;
        reflectiveSphereMdlInst.transform.setToScaling(scale, scale, scale);
        Vector3 scaleV = new Vector3();
        reflectiveSphereMdlInst.transform.getScale(scaleV);
        log.info("sphere model new scale: " + scaleV);
        reflectiveSphereMdlInst.transform.setToTranslation(position.x - reflectiveSphereMdlInst.radius, position.y, position.z);
        reflectiveSphereMdlInst.updateTransforms();
        Vector3 translatedPosition = new Vector3();
        reflectiveSphereMdlInst.transform.getTranslation(translatedPosition);
        log.info("sphere model updated successfully, radius: " + reflectiveSphereMdlInst.radius + ", position:" + translatedPosition);*/
