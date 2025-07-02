package io.github.labyrinthgenerator.pages.game3d.mesh;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class NormalMappedMesh extends Mesh {
    private Texture normalMap;

    public NormalMappedMesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
        super(isStatic, maxVertices, maxIndices, attributes);
    }

    public void setNormalMap(Texture normalMap) {
        this.normalMap = normalMap;
    }

    public Texture getNormalMapTexture() {
        return normalMap;
    }

    @Override
    public void render(ShaderProgram shader, int primitiveType) {
        /*if (normalMap != null) {
            normalMap.bind(2); // Привязываем к текстурному слоту 1
            shader.setUniformi("u_normalMap", 2); // Устанавливаем uniform для карты нормалей
        }*/

        super.render(shader, primitiveType);
    }
}
