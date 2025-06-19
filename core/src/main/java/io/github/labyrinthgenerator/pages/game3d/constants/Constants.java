package io.github.labyrinthgenerator.pages.game3d.constants;

import io.github.labyrinthgenerator.MyApplication;

public abstract class Constants {
	private static final int VERSION_MAJOR = 0;
	private static final int VERSION_MINOR = 0;
	private static final int VERSION_REVISION = 2;
	private static final long VERSION_BUILD = 140220221956L;
	public static final String VERSION = Integer.toString(VERSION_MAJOR) + "." + Integer.toString(VERSION_MINOR) + "."
			+ Integer.toString(VERSION_REVISION) + " " + Long.toString(VERSION_BUILD);

    public static final int TEXTURE_SIZE = 256;
    public static final float PPU = 1f / 16f;
    public static final int TILE_SIZE = 16;
	public static final float HALF_UNIT = 0.5f;

    // this should be a constant, but when resizing the window, the transition to a new page does not work correctly
	public static int WINDOW_WIDTH;
	public static int WINDOW_HEIGHT;

	public static final int FBO_WIDTH_ORIGINAL = MyApplication.windowW / 4; // 160
	public static final int FBO_HEIGHT_ORIGINAL = MyApplication.windowH / 4; // 120

	public static final int FBO_WIDTH_DECENT = MyApplication.windowW / 2;
	public static final int FBO_HEIGHT_DECENT = MyApplication.windowH / 2;

	public static final int FBO_WIDTH_DELUXE = MyApplication.windowW;
	public static final int FBO_HEIGHT_DELUXE = MyApplication.windowH;
}
