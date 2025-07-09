package io.github.labyrinthgenerator.pages.game3d.cubemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.mesh.NormalMapAttribute;
import io.github.labyrinthgenerator.pages.game3d.mesh.NormalMappedMesh;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;
import io.github.labyrinthgenerator.pages.game3d.shaders.MyShaderProvider;
import io.github.labyrinthgenerator.pages.game3d.shaders.SkyBoxShaderProgram;
import lombok.extern.slf4j.Slf4j;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;

@Slf4j
public class ReflectiveCubemap {

    private final float radius;
    private final Vector3 position;

    private final PerspectiveCamera camFb;
    private final FrameBufferCubemap fb;
    private final Cubemap cubemap;
    private final ModelInstanceBB reflectiveSphereMdlInst;

    private final CubeLab3D game;

    private final int fboWidth = 256, fboHeight = 256;

    private boolean renderedFirst = false;

    public ReflectiveCubemap(final Vector3 position, float radius, CubeLab3D game) {
        this.game = game;
        this.radius = radius;
        this.position = position;

        camFb = new PerspectiveCamera(90, 640, 480);
        camFb.position.set(0, HALF_UNIT, 0);
        camFb.lookAt(0, HALF_UNIT, 1);
        camFb.near = 0.01f;
        camFb.far = 100f;
        camFb.update();

        fb = new FrameBufferCubemap(Pixmap.Format.RGB888, fboWidth, fboHeight, true);
        fb.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        cubemap = fb.getColorBufferTexture();

        Model sphereModel = createSphereWithModelBuilder(radius);

        reflectiveSphereMdlInst = new ModelInstanceBB(sphereModel, position);
        log.info("sphere model loaded successfully, radius: " + reflectiveSphereMdlInst.radius);

        //reflectiveSphereMdlInst.transform.rotate(Vector3.X, 180);
        reflectiveSphereMdlInst.transform.setToTranslation(position);
        //reflectiveSphereMdlInst.calculateTransforms();
        //reflectiveSphereMdlInst.updateTransforms(); // bug!
        camFb.position.set(position.x, position.y/* + reflectiveSphereMdlInst.radius / 2f*/, position.z);
        camFb.update();

        reflectiveSphereMdlInst.materials.get(0).set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
        //updateCubemap(game.getScreen().getEnv(), 0);
    }

    private Model createSphereWithModelBuilder(float radius) {
        ModelBuilder modelBuilder = new ModelBuilder();
        return modelBuilder.createSphere(
            radius * 2f, radius * 2f, radius * 2f, 32, 32,
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
    }

    private Model createCustomSphere(float radius, boolean flipN) {
        NormalMappedMesh normalMapMesh = createSphereNormalMapMesh(radius, 32, 32, flipN);

        Texture normalMapTexture = normalMapMesh.getNormalMapTexture();
        NormalMapAttribute normalMapAttribute = NormalMapAttribute.createDiffuse(normalMapTexture);
        normalMapAttribute.textureDescription.minFilter = Texture.TextureFilter.Nearest;
        normalMapAttribute.textureDescription.magFilter = Texture.TextureFilter.Nearest;

        Material material = new Material(normalMapAttribute);

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        // Создаем модель с текстурированным кубом
        modelBuilder.part("sphere", normalMapMesh, GL20.GL_TRIANGLES, material);

        return modelBuilder.end();
    }

    private Model loadModel(String path) {
        ObjLoader objLoader = new ObjLoader();
        Model sphereModel = objLoader.loadModel(Gdx.files.internal(path));
        assert sphereModel != null;
        return sphereModel;
    }

    public Vector3 getPosition() {
        return camFb.position;
    }

    public void setPosition(Vector3 position) {
        this.position.set(position);
        camFb.position.set(position);
        camFb.update();
        reflectiveSphereMdlInst.transform.setToTranslation(position);
    }

    public void updateCubemap(final ModelBatch modelBatch, final Environment env, final SkyBoxShaderProgram envCubeMap, float delta) {
        if (renderedFirst) return; // comment this

        Camera currentCam = game.getScreen().getCurrentCam();
        //if (!game.getScreen().frustumCull(currentCam, reflectiveSphereMdlInst)) return;
        if (!game.getScreen().frustumCull(currentCam, position, radius * 2)) return;

        MyShaderProvider myShaderProvider = game.getShaderProvider();
        Shader currentShader = myShaderProvider.currentShader;
        myShaderProvider.currentShader = myShaderProvider.fogFreeShader;

        game.getScreen().setCurrentCam(camFb);

        Gdx.gl.glViewport(0, 0, fboWidth, fboHeight);
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        fb.begin();
        while (fb.nextSide()) {
            fb.getSide().getUp(camFb.up);
            fb.getSide().getDirection(camFb.direction);
            camFb.update();
            //if (renderedFirst && !game.getScreen().frustumCull(camFb, currentCam.position, radius)) continue;

            ScreenUtils.clear(1, 1, 1, 1, true);

            envCubeMap.render(camFb);

            modelBatch.begin(camFb);
            game.getEntMan().render3DAllEntities(modelBatch, env, delta, camFb.position.cpy(), false); // true
            modelBatch.end();
        }
        fb.end();
        renderedFirst = true;

        myShaderProvider.currentShader = currentShader;
        game.getScreen().setCurrentCam(currentCam);
    }

    public void render(ModelBatch modelBatch, Environment env, Camera cam) {
        // game.getScreen().frustumCull(cam, reflectiveSphereMdlInst)
        if (game.getScreen().frustumCull(cam, position, radius * 2)) {
            modelBatch.render(reflectiveSphereMdlInst, env, game.getShaderProvider().getShader());
        }
    }

    public static NormalMappedMesh createSphereNormalMapMesh(float radius, int longitudeBands, int latitudeBands, boolean flipN) {
        int totalVertices = (longitudeBands + 1) * (latitudeBands + 1);
        // @formatter:off
        float[] vertices  = new float[totalVertices * 8]; // x, y, z, nx, ny, nz, u, v
        float[] normals   = new float[totalVertices * 3]; // nx, ny, nz
        float[] texCoords = new float[totalVertices * 2]; // u, v
        short[] indices   = new short[longitudeBands * latitudeBands * 6];
        // @formatter:on

        // Создание вершин, нормалей и текстурных координат
        int vertexIndex = 0;
        int normalIndex = 0;
        int texCoordIndex = 0;

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
                float nx = MathUtils.clamp(x, -1, 1) * (flipN ? -1 : 1);
                float ny = MathUtils.clamp(y, -1, 1) * (flipN ? -1 : 1);
                float nz = MathUtils.clamp(z, -1, 1) * (flipN ? -1 : 1);

                // all in vertices
                vertices[vertexIndex++] = x * radius;
                vertices[vertexIndex++] = y * radius;
                vertices[vertexIndex++] = z * radius;
                vertices[vertexIndex++] = nx;
                vertices[vertexIndex++] = ny;
                vertices[vertexIndex++] = nz;
                vertices[vertexIndex++] = (float) lon / longitudeBands;
                vertices[vertexIndex++] = (float) lat / latitudeBands;

                // Нормали
                normals[normalIndex++] = nx;
                normals[normalIndex++] = ny;
                normals[normalIndex++] = nz;

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

        for (float v : vertices)
            if (Float.isNaN(v))
                throw new GdxRuntimeException("Vertex contains NaN value.");

        for (float n : normals)
            if (Float.isNaN(n))
                throw new GdxRuntimeException("Normal contains NaN value.");

        Texture normalTex = createMeshNormalMapTexture(longitudeBands + 1, latitudeBands + 1, normals, texCoords);

        NormalMappedMesh mesh = new NormalMappedMesh(true, totalVertices, indices.length,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0")); // a_texCoord2
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        mesh.setNormalMap(normalTex);

        return mesh;
    }

    private static Texture createMeshNormalMapTexture(int width, int height, float[] normals, float[] texCoords) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int index = (j * width + i) * 3; // Индекс нормали
                int r = (int) ((normals[index] * 0.5f + 0.5f) * 255); // Преобразование в диапазон [0, 255]
                int g = (int) ((normals[index + 1] * 0.5f + 0.5f) * 255);
                int b = (int) ((normals[index + 2] * 0.5f + 0.5f) * 255);
                int rgba = (((r & 255) << 24) | ((g & 255) << 16) | ((b & 255) << 8) | 255);

                // Используем текстурные координаты для правильного размещения пикселей
                int texCoordIndex = (j * width + i) * 2; // Индекс текстурной координаты
                int texCoordU = (int) (texCoords[texCoordIndex] * width);
                int texCoordV = (int) (texCoords[texCoordIndex + 1] * height);

                // Убедимся, что координаты в пределах границ
                texCoordU = MathUtils.clamp(texCoordU, 0, width - 1);
                texCoordV = MathUtils.clamp(texCoordV, 0, height - 1);

                pixmap.drawPixel(texCoordU, texCoordV, rgba);
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
