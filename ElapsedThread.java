/*
This class extends Thread
This is used to display the elapsed timer and the real-time number of pages parsed
This also checks if the maximum time is reached and breaks out of execution if necessary
 */
import javax.swing.*;

public class ElapsedThread extends Thread {
    private final JLabel elapsedLabel;
    private final JLabel parsedLabel;
    private final JToggleButton runButton;
    private final JLabel activeWorkers;
    private boolean exit;

    ElapsedThread(JLabel elapsedLabel, JLabel parsedLabel, JToggleButton runButton, JLabel activeWorkers){
        this.elapsedLabel = elapsedLabel;
        this.parsedLabel = parsedLabel;
        this.runButton = runButton;
        this.activeWorkers = activeWorkers;
        exit = false;
    }

    @Override
    public void run(){
        long startTime = System.currentTimeMillis();
        long pageNum = 0;
        int displayMinutes = 0;
        boolean oneSec = false;
        /*
        oneSec variable is used to mark if one second has passed after x minutes 0 seconds occured last time
        If this is not used since time is used in milliSeconds the minutes are incremented 1000 times in that one second.
         */
        long maxTime = SiteActions.getMaxTime();
        int currentActive = SiteActions.getActiveWorkers();
        if(maxTime == -1)
            maxTime = Long.MAX_VALUE;           // If user does not provide max time
        while(!exit && runButton.isSelected()) {
            if(SiteActions.numberOfSites() > pageNum){      // To display the total number of pages parsed
                pageNum = SiteActions.numberOfSites();
                parsedLabel.setText(Long.toString(pageNum));
            }

            long changeTime = System.currentTimeMillis() - startTime;
            long secondsPassed = changeTime/1000;

            if(secondsPassed == 1)
                oneSec = true;
            if(secondsPassed == 60){    // To reset the seconds to 0 after they reach 60
                secondsPassed = 0;
                startTime = System.currentTimeMillis();
            }
            if(secondsPassed%60 == 0 && oneSec) {
                oneSec = false;
                displayMinutes++;
            }

            if(maxTime <= (displayMinutes*60 + secondsPassed)){     // If max time has reached break out of the loop
                exit = true;
            }

            if(currentActive != SiteActions.getActiveWorkers()){    // Display the number of currently active workers
                currentActive = SiteActions.getActiveWorkers();
                if(currentActive == 0)
                    exit = true;
                activeWorkers.setText(Integer.toString(currentActive));
            }

            if(secondsPassed < 10)
                elapsedLabel.setText(String.format("%d:0%d",displayMinutes, secondsPassed));
            else
                elapsedLabel.setText(String.format("%d:%d",displayMinutes, secondsPassed));
        }
        runButton.setSelected(false);
    }

    public void stopThread(){
        exit = true;
    }
}