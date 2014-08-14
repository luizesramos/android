package com.eac.knights;

public class MoveTranslator {
	public static final int CLEAR = 0x00;
	public static final int GO_UP = 0x01;
	public static final int GO_RG = 0x02;
	public static final int GO_DN = 0x04;
	public static final int GO_LF = 0x08;

	// direction -> index translation
	private static final int DIR_UP = 0;
	private static final int DIR_UP_RGLF = 1;
	private static final int DIR_RG_LF = 2;
	private static final int DIR_DN_RGLF = 3;
	private static final int DIR_DN = 4;

	private static final int DIRECTIONS = 5;
	private static final int INDICES_PER_DIRECTION = 5;

	private int dirIndex, spriteIndex;
	private int[][] moves;
	private int status;

	public MoveTranslator() {
		dirIndex = DIR_DN;
		spriteIndex = 0;

		// movement matrix
		moves = new int[DIRECTIONS][INDICES_PER_DIRECTION];

		for (int i = 0; i < DIRECTIONS; i++) {
			moves[i][0] = i;
			for (int j = 1; j < INDICES_PER_DIRECTION; j++) {
				moves[i][j] = moves[i][j - 1] + INDICES_PER_DIRECTION;
			}

			// DEBUG
			// String line = "MOVE["+i+"][]:";
			// for (int j = 0; j < INDICES_PER_DIRECTION; j++)
			// line += " " + moves[i][j];
			// android.util.Log.v("MoveTranslator", line);
		}

		clearStatus();
	}

	// flag methods
	public void clearStatus() {
		status = CLEAR;
	}

	public void set(int flag) {
		status |= flag;
	}

	public boolean is(int flag) {
		return ((status & flag) != CLEAR);
	}

	public boolean isClear() {
		return status == CLEAR;
	}

	// translates a move intent into a frame index
	public int getNextFrameIndex() {
		if (isClear()) {
			dirIndex = DIR_DN;
			spriteIndex = 0;
		} else {
			// advance sprite index
			spriteIndex = ((spriteIndex + 1) % INDICES_PER_DIRECTION);
			if(spriteIndex == 0) spriteIndex = 1;
			
			// spriteIndex = ((spriteIndex + 1) % (INDICES_PER_DIRECTION));
			//android.util.Log.v("MoveTranslator", "INDEX:" + spriteIndex);

			if (is(GO_UP)) {
				dirIndex = (is(GO_RG) || is(GO_LF)) ? DIR_UP_RGLF : DIR_UP;
			} else if (is(GO_DN)) {
				dirIndex = (is(GO_RG) || is(GO_LF)) ? DIR_DN_RGLF : DIR_DN;
			} else {
				dirIndex = DIR_RG_LF;
			}
		}

		// android.util.Log.v("MoveTranslator","DI:"+dirIndex+
		// " SI:"+spriteIndex);

		return moves[dirIndex][spriteIndex];
	}
}
