This submission contains:

* Signed apk file: CatInCup-v1.0.apk.

* Two screenshots of the app.
CatInCup1.png
CatInCup2.png

* What does your app do?

My app is a simple guessing game. The user sees three cups and must guess which one contains a cat. The user begins with 9 lives at round 1. The goal is to reach as many rounds as possible. The app shows both the current number of lives (in red when the user loses a life, by guessing wrong, and in green when the user gains a life, by guessing right) and the current round’s number. Besides providing feedback as the life/round counters, I briefly display (for 700ms) the location of the cat. 

* Why did you decided to build the app?

I had done this app a long time ago as a Java applet, and thought of turning it into an Android app using the valuable lessons of the first three weeks of this course.

* What do you remember most about your development experience? For example, what was the hardest part of this assignment or the part that required the most time?

The hardest part was learning to control the layouts. That is: (1) correctly controlling the relative positions and sizes of fonts and widgets; (2) figuring out how to correctly show the content in layout-normal, layout-xlarge devices; (3) for this game, I wanted to enable only a landscape orientation, so I had to modify the manifest file and insert some code to correctly start the activity if it is initially in portrait mode.

* What would you like to do next to your app?

A reverse polish notation calculator; some app with swiping capability, like a cards game;  some app with GPS capabilities and voice recognition. 


* Optionally, you may post some code to receive feedback from your peers on your code. No need to post an entire file. If you're particularly pleased with some code you have written, include it! The code snippet does not need to be large, just share the experience of what you managed to create. Note, your second app will only be graded on completion (was it done or not) and not on functionality or design. You will, however, get comments back from your peers, so this is a chance to say "Hello World" in your own creative way.

Hi, I just wanted to share some interesting pieces of code, for which I found guidelines online.

a) To show the cat in a cup for 700ms, I used a timed event.

new android.os.Handler().postDelayed(new Runnable() {
  @Override
  public void run() {
    clear_cups();
  }
}, 700);

b) since Android destroys the Activity when we switch between portrait and landscape mode, I used the bundle to save and restore those variables and maintain the game’s state.

// save state
public void onSaveInstanceState(Bundle savedInstanceState) {
  savedInstanceState.putInt("CurrentLives", lives);
  savedInstanceState.putInt("CurrentRounds", rounds);
  savedInstanceState.putInt("LastAction", lastAction);
  super.onSaveInstanceState(savedInstanceState);
}

// restore state (in onCreate)
super.onRestoreInstanceState(savedInstanceState);
lives = savedInstanceState.getInt("CurrentLives");
rounds = savedInstanceState.getInt("CurrentRounds");
lastAction = savedInstanceState.getInt("LastAction");
refreshScore(lastAction);

c) To keep the orientation fixed in landscape, I modified the manifest file and inserted some code in the Activity’s onCreate method (to initialize the orientation correctly).

// in the manifest file
<application
  android:allowBackup="true"
  android:icon="@drawable/ic_launcher"
  android:label="@string/app_name"
  android:screenOrientation="landscape"
  android:configChanges="orientation|screenSize"
  android:theme="@style/AppTheme" >

// in MainActivity.java
setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

d) Finally, when the game ends (zero lives), I use a  dialog box to inform end of game and ask if user wants to continue.

// build the dialog box
AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
alertDialogBuilder.setTitle("You survived for " + rounds + " rounds! Play again?");
alertDialogBuilder
  .setCancelable(false)
  .setPositiveButton("New Game",new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog,int id) {
      start();
    }
  })
  .setNegativeButton("Quit",new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog,int id) {
      finish();
    }
  });
 
// create and show the dialog box
AlertDialog alertDialog = alertDialogBuilder.create();
alertDialog.show();