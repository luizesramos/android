package com.eac.knights;

import java.io.InputStream;

import com.eac.knights.MainActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class Scene {
	private int w, h; // dimensions of the view
	private Bitmap sceneBitmap;
	private Canvas mCanvas;
	private Paint clearPaint, drawPaint;

	public Scene(MainActivity parent) {
		DisplayMetrics metrics = parent.getBaseContext().getResources().getDisplayMetrics();         
		w = metrics.widthPixels;
		h = metrics.heightPixels;
		initScreen();
	}
	
	public Scene(InputStream stream) {
		// read the entire raw sprite file
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inScaled = true;
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(stream, null, opts);
		h = opts.outHeight;
		w = opts.outWidth;
		initScreen();
	}

	private void initScreen(){
		sceneBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		mCanvas = new Canvas();
		mCanvas.setBitmap(sceneBitmap);

		// create paints
		clearPaint = new Paint();
		clearPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		drawPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
		resetScene();
	}
	
	public void resetScene() {
		mCanvas.drawPaint(clearPaint);
		mCanvas.drawColor(0x200000a0); // DEBUG
	}

	public void placeAvatar(int x, int y, Bitmap bmp) {
		mCanvas.drawBitmap(bmp, x, y, drawPaint);
	}

	public void draw(ImageView scene) {
		scene.setImageBitmap(sceneBitmap);
	}
	
	public int getHeight() {
		return h;
	}

	public int getWidth() {
		return w;
	}
}