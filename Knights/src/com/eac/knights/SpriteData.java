package com.eac.knights;

public class SpriteData {
	private static final int SCREEN_BORDERS = 10;
	private int posX, posY, velX, velY;
	private int targetX, targetY;
	private int screenWidth, screenHeight;
	private MoveTranslator mt;

	public SpriteData(int screenW, int screenH) {
		screenWidth = screenW;
		screenHeight = screenH;
		posX = screenWidth / 2;
		posY = screenHeight / 2;
		velX = 16;
		velY = 16;
		mt = new MoveTranslator();
		setTarget(posX, posY); // start stopped
	}

	public void randomMove() {
		int x = SCREEN_BORDERS
				+ (int) ((Math.random() * (screenWidth - SCREEN_BORDERS) * 777) % (screenWidth - SCREEN_BORDERS));

		int y = SCREEN_BORDERS
				+ (int) ((Math.random() * (screenHeight - SCREEN_BORDERS) * 777) % (screenHeight - SCREEN_BORDERS));

		setTarget(x, y);
	}

	private void parseDirection() {
		mt.clearStatus();

		// find horizontal direction
		if (targetX > posX) {
			mt.set(MoveTranslator.GO_RG);
		} else if (targetX < posX) {
			mt.set(MoveTranslator.GO_LF);
		}

		// find vertical direction
		if (targetY > posY) {
			mt.set(MoveTranslator.GO_DN);
		} else if (targetY < posY) {
			mt.set(MoveTranslator.GO_UP);
		}

		//android.util.Log.v("SpriteData", "POS(" + posX + "," + posY + ") TGT("
		//		+ targetX + "," + targetY + ")");
	}

	public void setTarget(int x, int y) {
		// ignore target if either x or y are out-of-bounds
		if (x < 0 || x > screenWidth || y < 0 || y > screenHeight)
			return;

		targetX = x;
		targetY = y;
		parseDirection();
	}

	// positions the avatar in the correct direction
	// returns the index of the
	public int walk() {
		// stop condition: rounding up the arrival
		if (Math.abs(targetX - posX) < velX) {
			targetX = posX;
		}
		if (Math.abs(targetY - posY) < velY) {
			targetY = posY;
		}

		// verify if there is movement
		parseDirection();

		// mtlate
		if (mt.is(MoveTranslator.GO_UP)) {
			posY -= velY;
		} else if (mt.is(MoveTranslator.GO_DN)) {
			posY += velY;
		}

		if (mt.is(MoveTranslator.GO_RG)) {
			posX += velX;
		} else if (mt.is(MoveTranslator.GO_LF)) {
			posX -= velX;
		}

		// return the new frame index
		return mt.getNextFrameIndex();
	}

	public boolean getReverse() {
		return (mt.is(MoveTranslator.GO_LF));
	}

	public int getX() {
		return posX;
	}

	public int getY() {
		return posY;
	}

	public boolean stopped() {
		return mt.isClear();
	}
}