package io.github.labyrinthgenerator.pages.game3d.constants;

import com.badlogic.gdx.math.MathUtils;
import io.github.labyrinthgenerator.MyApplication;

public abstract class Constants {
    private static final int VERSION_MAJOR = 0;
    private static final int VERSION_MINOR = 0;
    private static final int VERSION_REVISION = 2;
    private static final long VERSION_BUILD = 140220221956L;
    public static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR + "."
        + VERSION_REVISION + " " + VERSION_BUILD;

    public static final int TEXTURE_SIZE = 256;
    public static final float HALF_UNIT = 0.5f;

    public static final float CAMERA_FAR = 100f;

    public static final int CHUNK_SIZE = 10;
    public static final int CHUNKS_UPDATE_RANGE_AROUND_CAM = 1;
    public static final int CHUNKS_RANGE_AROUND_CAM = MathUtils.ceil(CAMERA_FAR / CHUNK_SIZE);

    // FIXME this should be a constant, but when resizing the window, the transition to a new page does not work correctly
    public static int WINDOW_WIDTH;
    public static int WINDOW_HEIGHT;

    public static final int FBO_WIDTH_ORIGINAL = MyApplication.windowW / 4; // 160
    public static final int FBO_HEIGHT_ORIGINAL = MyApplication.windowH / 4; // 120

    public static final int FBO_WIDTH_DECENT = MyApplication.windowW / 2;
    public static final int FBO_HEIGHT_DECENT = MyApplication.windowH / 2;

    public static final int FBO_WIDTH_DELUXE = MyApplication.windowW;
    public static final int FBO_HEIGHT_DELUXE = MyApplication.windowH;
}
