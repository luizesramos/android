package com.eac.machines;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {
	// constants
	private final int STEPS = 7; // largest memory sequence
	private final int ROBOTS = 4; // number of robots
	private final int ITEMS = ROBOTS + 1; // number of game objects
	private final int SOUNDS = ROBOTS + 2; // number of game sounds
	private final int LIVES = 5; // initial number of lives
	private final int HI_TIME = 700; // time (ms) in which robot says hi

	// sounds
	private final int EXPLOSION = ROBOTS; // explosion sound index
	private final int CHA_CHING = ROBOTS + 1; // cash register sound

	// states
	private final int STOPPED = 0;
	private final int RUNNING = 1;
	private final int PLAYING = 2;

	// actions
	private final int ACTION_NONE = 0;
	private final int ACTION_HIT = 1;
	private final int ACTION_MISS = 2;
	private final int ACTION_SHOW = 3;
	private final int ACTION_HIDE = 4;

	// view variables
	private ImageButton[] b;
	private TextView tvScore, tvLives, tvStepsLeft;
	MediaPlayer[] sounds;
	Handler sched;

	// state variables
	// currentStep = how advanced in from beginning to end the user is
	// autoStep = plays sounds from 0..currentStep
	// userStep = player must move from 0..currentStep to increment currentStep
	private int[] seq;
	private int score, lives, currentStep, autoStep, userStep;
	private int state;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// locate components on the screen
		b = new ImageButton[ITEMS];
		b[0] = ((ImageButton) findViewById(R.id.robot0));
		b[1] = ((ImageButton) findViewById(R.id.robot1));
		b[2] = ((ImageButton) findViewById(R.id.robot2));
		b[3] = ((ImageButton) findViewById(R.id.robot3));
		b[4] = ((ImageButton) findViewById(R.id.chest));
		tvScore = (TextView) findViewById(R.id.score);
		tvLives = (TextView) findViewById(R.id.lives);
		tvStepsLeft = (TextView) findViewById(R.id.stepsLeft);

		// pre-load sounds
		sounds = new MediaPlayer[SOUNDS];
		for (int i = 0; i < ROBOTS; ++i) {
			sounds[i] = MediaPlayer.create(this, R.raw.robot0 + i);
			sounds[i].setOnCompletionListener(soundCompleted);
		}
		sounds[EXPLOSION] = MediaPlayer.create(this, R.raw.explosion);
		sounds[EXPLOSION].setOnCompletionListener(soundCompleted);
		sounds[CHA_CHING] = MediaPlayer.create(this, R.raw.money);
		sounds[CHA_CHING].setOnCompletionListener(soundCompleted);

		// initialize the timing event handler
		sched = new Handler();

		// initialize state variables
		seq = new int[STEPS];
		initScreen(ACTION_HIDE);
		changeState(STOPPED);
		startNewRound(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// ---------------------------------------------------------------
	// Screen state

	public void initScreen(int action) {
		if (action == ACTION_SHOW) {
			tvLives.setVisibility(View.VISIBLE);
			tvStepsLeft.setVisibility(View.VISIBLE);
			updateScore(ACTION_NONE);
		} else {
			tvScore.setText(getString(R.string.hint));
			tvLives.setVisibility(View.INVISIBLE);
			tvStepsLeft.setVisibility(View.INVISIBLE);
			resetScene();
		}
	}

	public void resetScene() {
		b[0].setImageResource(R.drawable.robot00);
		b[1].setImageResource(R.drawable.robot10);
		b[2].setImageResource(R.drawable.robot20);
		b[3].setImageResource(R.drawable.robot30);
		b[4].setImageResource(R.drawable.chest1);
	}

	public void updateScore(int action) {
		if (action == ACTION_NONE) {
			tvLives.setTextColor(getResources().getColor(R.color.none_color));
		} else if (action == ACTION_HIT) {
			tvLives.setTextColor(getResources().getColor(R.color.hit_color));
		} else if (action == ACTION_MISS) {
			tvLives.setTextColor(getResources().getColor(R.color.miss_color));
		}

		tvScore.setText(getString(R.string.score) + " " + score);
		tvLives.setText(getString(R.string.lives) + " " + lives);
		tvStepsLeft.setText(getString(R.string.steps) + " "
				+ getRemainingSteps());
	}

	public void pingRobot(int id) {
		if (id < 0 || id >= ROBOTS) {
			Log.e("Machines", "Robot out of bounds!");
			return;
		}

		// changes the robot's picture
		b[id].setImageResource(R.drawable.robot01 + (2 * id));
	}

	public void clickChest(View v) {
		// automatically playing the first steps of the sequence
		if (state != PLAYING) {

			// running the game for the first time since launching
			if (state == STOPPED) {
				startNewRound(true);
				initScreen(ACTION_SHOW);
			}

			changeState(PLAYING);
			autoStep = 0;
			pingRobot(seq[autoStep]);
			playNextSound();
		}
	}

	OnCompletionListener soundCompleted = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			// we successfully executed one more sound
			autoStep++;

			// if we're playing, play next sound in HI_TIME milliseconds
			// if (state == PLAYING && autoStep < STEPS) { // for debugging
			if (state == PLAYING && autoStep <= currentStep) {
				sched.postDelayed(new Runnable() {
					@Override
					public void run() {
						Log.v("PLAY", "user " + userStep + "/" + currentStep);
						resetScene();
						pingRobot(seq[autoStep]);
						playNextSound();
					}
				}, HI_TIME);

				// otherwise, we're done with playing, and we're back at the
				// user
			} else {
				sched.postDelayed(new Runnable() {
					@Override
					public void run() {
						Log.v("PLAY_END", "user " + userStep + "/"
								+ currentStep);
						if (state == PLAYING)
							resetUserStep();

						// reset the state of the robots and chest
						resetScene();

						// set the state back to running
						changeState(RUNNING);
					}
				}, HI_TIME);

			}
		}
	};

	public void playNextSound() {
		sounds[seq[autoStep]].start();
	}

	public void updateTreasure(boolean win) {
		// changes the robot's picture
		if (win) {
			sched.postDelayed(new Runnable() {
				@Override
				public void run() {
					b[ROBOTS].setImageResource(R.drawable.chest0); // open chest
					sounds[CHA_CHING].start();
				}
			}, HI_TIME * 2);

		} else {
			sched.postDelayed(new Runnable() {
				@Override
				public void run() {
					b[ROBOTS].setImageResource(R.drawable.chest2); // explosion
					sounds[EXPLOSION].start();
				}
			}, HI_TIME * 2);
		}
	}

	// ---------------------------------------------------------------
	// Handle game state

	public void changeState(int newState) {
		// Log.v("ChangeState", "" + state + " => " + newState);
		state = newState;
	}

	public int getRemainingSteps() {
		return (STEPS - currentStep);
	}

	public boolean isDefeat() {
		return (lives == 0);
	}

	public void gameOver() {
		// create dialogue to inform end of game and ask if user wants to
		// continue
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		String title, message;

		if (!isDefeat()) { // player won
			changeState(RUNNING);
			title = new String("Awesome! Your current score is " + score);
			message = new String("You still have " + lives
					+ " lives. Click 'Continue' to keep on playing!");

		} else { // player lost all lives
			changeState(STOPPED);
			title = new String("Thanks for playing! Your final score was "
					+ score);
			message = new String("Click 'Continue' to start a new game.");
		}

		// set up the dialog's properties (using a factory class)
		alertDialogBuilder
				.setMessage(message)
				.setTitle(title)
				.setCancelable(false)
				.setPositiveButton("Continue",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if we lost, reset everything
								startNewRound(isDefeat());
								updateScore(ACTION_NONE);
								resetScene();
							}
						})
				.setNegativeButton("Quit",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								finish(); // dialog.cancel();
							}
						});

		// create and show the dialog box
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	public void resetCurrentStep() {
		currentStep = 0;
		// Log.e("Machines", "Current step: " + currentStep);
	}

	public void advanceCurrentStep() {
		currentStep++;
		// Log.e("Machines", "Current step: " + currentStep);
	}

	public void resetUserStep() {
		userStep = 0;
		// Log.e("Machines", "User step: " + currentStep);
	}

	public void advanceUserStep() {
		userStep++;
		// Log.e("Machines", "User step: " + currentStep);
	}

	public void startNewRound(boolean resetState) {
		if (resetState) {
			score = 0;
			lives = LIVES;
			resetCurrentStep();
		} else {
			changeState(RUNNING);
		}

		// forces the user to start her sequence over
		resetUserStep();

		for (int i = 0; i < STEPS; i++) {
			seq[i] = ((int) (Math.random() * 10)) % ROBOTS;
			// android.util.Log.e("Machines", "" + seq[i]);
		}
	}

	// ---------------------------------------------------------------
	// Game state transitions

	public void evaluate_guess(int id) {
		// move robot on screen
		pingRobot(id);

		// play the robot's sound
		sounds[id].start();

		// user guessed right
		if (seq[userStep] == id) {
			Log.v("BEGIN", "HIT user " + userStep + "/" + currentStep);

			score += 5;
			updateScore(ACTION_HIT);

			// advance sequence only if we got it right
			advanceUserStep();

			if (userStep > currentStep) {
				advanceCurrentStep();
				updateScore(ACTION_HIT);

				resetUserStep();
				// user just completed a game step: is it the last one?
				if (currentStep == STEPS) {
					updateTreasure(true);
					resetCurrentStep(); // we reached the end: start over

					// waits a little before showing the dialogue
					sched.postDelayed(new Runnable() {
						@Override
						public void run() {
							gameOver();
						}
					}, HI_TIME * 4);

				} else {

					// when the user reaches the end of the known sequence, we
					// wait a little
					// then automatically trigger the expanded sequence.
					sched.postDelayed(new Runnable() {
						@Override
						public void run() {
							clickChest(findViewById(R.id.chest));
						}
					}, HI_TIME * 3);
				}
			}

			// user guessed wrong
		} else {
			Log.v("BEGIN", "MISS user " + userStep + "/" + currentStep);

			lives--;
			resetUserStep();
			resetCurrentStep();
			updateScore(ACTION_MISS);
			updateTreasure(false);
			if (isDefeat())
				sched.postDelayed(new Runnable() {
					@Override
					public void run() {
						gameOver();
					}
				}, HI_TIME * 4);
		}

		// android.util.Log.e("Machines","CLICK " + id);
	}

	public void clickRobot(View v) {
		// prevent user from playing during automatic sequence
		if (state != RUNNING)
			return;

		if (currentStep >= STEPS || userStep > currentStep) {
			Log.e("Machines", "Out of bounds! Current step: " + currentStep
					+ " userStep: " + userStep);
			return;
		}

		if (v.getId() == R.id.robot0) {
			evaluate_guess(0);
		} else if (v.getId() == R.id.robot1) {
			evaluate_guess(1);
		} else if (v.getId() == R.id.robot2) {
			evaluate_guess(2);
		} else if (v.getId() == R.id.robot3) {
			evaluate_guess(3);
		}
	}

	// ---------------------------------------------------------------
	// Handle secondary screens
	public void help(View v) {
		startActivity(new Intent(this, Help.class));
	}

	public void wikipedia(View v) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("http://en.wikipedia.org/wiki/Robot"));
		startActivity(i);
	}
}