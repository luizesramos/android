package com.eac.knights;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;

public class RawSprite {
	private Bitmap rawBitmap, mBitmap;
	private int w, h; // raw width and height of entire sprite file
	private int unitWidth, unitHeight; // dimensions of a unit inside the file
	private int frame; // current frame
	private int maxFrames;
	private Canvas mCanvas;
	private static final int scalingFactor = 2;
	private Paint paint;

	public RawSprite(InputStream stream, int unitsWidthwise, int unitsHeightwise) {
		// read the entire raw sprite file
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inScaled = true;
		rawBitmap = BitmapFactory.decodeStream(stream, null, opts);

		// get dimensions
		h = opts.outHeight;
		w = opts.outWidth;
		unitHeight = h / unitsHeightwise;
		unitWidth = w / unitsWidthwise;
		maxFrames = unitsHeightwise * unitsWidthwise;
		// parent.showMessage("ROWS:" + h / unitHeight + " W:" + w / unitWidth);

		// paint
		paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC));

		// parent.showMessage("UH:" + unitHeight + " UW:" + unitWidth);
		mBitmap = Bitmap.createBitmap(unitWidth * scalingFactor, unitHeight
				* scalingFactor, Config.ARGB_8888);
		mCanvas = new Canvas();
		setFrame(0);
	}

	public int getFrame() {
		return frame;
	}

	public void setFrame(int i) {
		if (i < 0 || i >= maxFrames)
			return;
		frame = i;
	}

	public Bitmap getCurrentBitmap(boolean reverse) {
		int row = ((frame * unitWidth) / w) * unitHeight;
		int col = ((frame * unitWidth) % w);
		// parent.quickMessage("ROW:" + row + " COL:" + col);

		Rect view = new Rect(0, 0, unitWidth, unitHeight);
		Rect clip = new Rect(col, row, col + unitWidth, row + unitHeight);

		mCanvas.setBitmap(mBitmap);
		mCanvas.save();
		if (reverse) {
			mCanvas.translate(mBitmap.getWidth(), 0);
			mCanvas.scale(-scalingFactor, scalingFactor);
			mCanvas.drawBitmap(rawBitmap, clip, view, paint);
		} else {
			mCanvas.scale(scalingFactor, scalingFactor);
			mCanvas.drawBitmap(rawBitmap, clip, view, paint);
		}
		mCanvas.restore();

		return mBitmap;
	}
	
	public int getHeight() {
		return unitHeight;
	}

	public int getWidth() {
		return unitWidth;
	}
}