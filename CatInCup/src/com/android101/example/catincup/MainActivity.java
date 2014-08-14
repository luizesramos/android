package com.android101.example.catincup;

/* CatInCup is a game where the user tries to guess in which of the cups a cat is hiding.
 * The user starts with 9 lives and his/her goal is to remain alive for as many rounds as possible.
 * */

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.TextView;
//import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import java.util.Random;

public class MainActivity extends Activity {
	private final int MAX_CUPS = 3;
	private final int ACTION_HIT = 0;
	private final int ACTION_MISS = 1;
	private final int ACTION_NONE = 3;
    private Random generator;
    private int lives, rounds, lastAction;
    private TextView livesView;
    private TextView roundsView;
    private ImageButton []b;
    
    // when the user changes the position of the device between land and port,
    // android creates a new Activity, so we must make sure to preserve 
    // the game score. 
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("CurrentLives", lives);
        savedInstanceState.putInt("CurrentRounds", rounds);
        savedInstanceState.putInt("LastAction", lastAction);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // force the orientation to be landscape from the start
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // set up the random number generator
        generator = new Random(System.currentTimeMillis());
        
        // save pointers to the frequently-used widgets
        livesView = ((TextView) findViewById(R.id.TextViewLives));
        roundsView = ((TextView) findViewById(R.id.TextViewRounds));
        
        b = new ImageButton[3];
        b[0] = ((ImageButton) findViewById(R.id.ImageButtonCup1));
        b[1] = ((ImageButton) findViewById(R.id.ImageButtonCup2));
        b[2] = ((ImageButton) findViewById(R.id.ImageButtonCup3));
        
        // set up the event handler for the buttons
        b[0].setOnClickListener(click);
        b[1].setOnClickListener(click);
        b[2].setOnClickListener(click);
        
        // setting game-play variables and interfaces for the first time
        if(savedInstanceState == null) {
        	start();
        	
        // propagating an existing state
        } else {
	        super.onRestoreInstanceState(savedInstanceState);
	        lives = savedInstanceState.getInt("CurrentLives");
	        rounds = savedInstanceState.getInt("CurrentRounds");
	        lastAction = savedInstanceState.getInt("LastAction");
	        refreshScore(lastAction);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void refreshScore(int action) {
    	if(action == ACTION_NONE) {
    		livesView.setTextColor(getResources().getColor(R.color.none_color));
    	} else if(action == ACTION_HIT) {
    		livesView.setTextColor(getResources().getColor(R.color.hit_color));
    	} else if(action == ACTION_MISS) {
    		livesView.setTextColor(getResources().getColor(R.color.miss_color));
    	}
    	lastAction = action;
    	livesView.setText(getString(R.string.lives) + " " + lives);
    	roundsView.setText(getString(R.string.rounds) + " " + rounds);
    }
    
    public void start() {
    	lives = 9;
    	rounds = 1;
    	refreshScore(ACTION_NONE);
    	clear_cups();
    }

    public void clear_cups() {
    	b[0].setImageResource(R.drawable.cup_empty);
    	b[1].setImageResource(R.drawable.cup_empty);
    	b[2].setImageResource(R.drawable.cup_empty);
    }
    
    public void show_cat(boolean win, ImageButton b) {
    	if(win) // player wins round
    	  b.setImageResource(R.drawable.cup_win1);
    	else // player loses round
      	  b.setImageResource(R.drawable.cup_lose1);
    	
    	new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            	clear_cups();
            }
        }, 700);
    	
    	//Log.v("CATINCUP","User guessed " + (win ? "right" : "wrong"));
    }
    
    public void evaluate_guess(int guess) {
    	 int cup = (generator.nextInt() % MAX_CUPS);
    	 if(cup < 0) cup = -cup;
    	 //Log.v("CATINCUP","CUP: " + cup);

    	 if(cup == guess-1) {
    		// user guesses right
    		 lives++;
    		 show_cat(true, b[cup]);
    		 refreshScore(ACTION_HIT);
    		 
    	 } else {
    		// user guesses wrong
    		 lives--;
    		 show_cat(false, b[cup]);
    		 refreshScore(ACTION_MISS);
    	 }
    	 
    	 // did we run out of lives?
    	 if(lives == 0) {
    		 game_over();
    	 } else {
        	 rounds++;
    	 }
    }
    
    public void game_over() {
    	// create dialogue to inform end of game and ask if user wants to continue
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set up the dialog's properties (using a factory class)
		alertDialogBuilder //.setMessage("Click New Game to continue playing!")
			.setTitle("You survived for " + rounds + " rounds! Play again?")
			.setCancelable(false)
			.setPositiveButton("New Game",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					start();
				}
			})
			.setNegativeButton("Quit",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					finish(); //dialog.cancel();
				}
			});
 
		// create and show the dialog box
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();    	
    }
    
    OnClickListener click = new OnClickListener() {
        @Override
        public void onClick(View v) {
        		if(v.getId() == R.id.ImageButtonCup1) {
                	evaluate_guess(1);
                } else if(v.getId() == R.id.ImageButtonCup2) {
                	evaluate_guess(2);
                } else if(v.getId() == R.id.ImageButtonCup3) {
                	evaluate_guess(3);
                }
        }
    };
}
