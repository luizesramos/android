package com.eac.knights;

import java.io.InputStream;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements Runnable, OnTouchListener {
	public static final String TAG = "Knight";
	private static final int WALK_DELAY = 500;
	private static final int DEFAULT_MAX_STEPS = 30;
	private ImageView sceneView;
	private RawSprite avatarSprite;
	private Scene scene;
	private SpriteData avatar;
	private Handler sched;
	private int stepsThreshold;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// setting up the image view
		if (sceneView == null) {
			sceneView = (ImageView) findViewById(R.id.imageView1);
			sceneView.setOnTouchListener(this);
		}

		// creating a scene bitmap with the dimensions of the screen
		if (scene == null) {
			scene = new Scene(this);
		}

		// creating the avatar's position and current frame data
		if (avatar == null) {
			avatar = new SpriteData(scene.getWidth(), scene.getHeight());
		}

		// creating avatar images from asset sprite file
		if (avatarSprite == null) {
			try {
				InputStream stream;
				stream = getAssets().open("knightwalk.png");
				avatarSprite = new RawSprite(stream, 5, 5);
				stream.close();
			} catch (Exception e) {
				android.util.Log.e(TAG, "Decoding Knight Bitmap", e);
				finish();
			}
		}

		// creating handler for automatic movements + safety threshold
		if (sched == null) {
			sched = new Handler();
			resetStepThreshold();
		}

		// draw everything
		redraw();
	}

	private void redraw() {
		scene.resetScene();
		avatarSprite.setFrame(avatar.walk());
		scene.placeAvatar(avatar.getX(), avatar.getY(),
				avatarSprite.getCurrentBitmap(avatar.getReverse()));
		scene.draw(sceneView);
		// android.util.Log.v(TAG, "FRAME: " + avatarSprite.getFrame());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void showMessage(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
	}

	public void quickMessage(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

	public void prevClick(View v) {
		avatar.randomMove();
	}

	// the avatar moves this many steps at a time
	private void resetStepThreshold() {
		stepsThreshold = DEFAULT_MAX_STEPS;
	}

	@Override
	public void run() {
		stepsThreshold--;
		if (!avatar.stopped() && stepsThreshold > 0) {
			redraw();
			sched.postDelayed(this, WALK_DELAY);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int x = (int) event.getX() - (avatarSprite.getWidth() / 2);
		int y = (int) event.getY() + (avatarSprite.getHeight() / 2);

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// DEBUG: put something on the screen
			// android.util.Log.v(TAG, "TOUCH X:" + x + " Y:" + y);
			// scene.placeAvatar(x, y,
			// avatarSprite.getCurrentBitmap(avatar.getReverse()));
			// scene.draw(sceneView);

			resetStepThreshold();
			avatar.setTarget(x, y);
			sched.postDelayed(this, WALK_DELAY);
		}
		return false;
	}
}