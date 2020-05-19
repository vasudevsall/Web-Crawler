/*
This class represents a single worker
that gets the current page from the queue and parses it
It adds the parsed page to the map
and adds all the found links to the Queue and then repeats the process
 */

import javax.swing.*;
import java.util.ArrayList;

public class Worker extends Thread {
    private boolean stopIt = false;
    private final JToggleButton runButton;
    private final ElapsedThread timer;
    private final JLabel currentActive;

    Worker(JToggleButton runButton, ElapsedThread timer, JLabel currentActive)
    {
        this.runButton = runButton;
        this.timer = timer;
        this.currentActive = currentActive;
    }

    @Override
    public void run() {
        int depth = 0;
        ArrayList<String> sites;
        int noSites = 0;
        int maxDepth = SiteActions.getMaxDepth();
        if(maxDepth == -1)  // If maxDepth is checkBox is not selected
            maxDepth = Integer.MAX_VALUE;
        while(depth <= maxDepth && !stopIt){
            if(!runButton.isSelected()) {   // If runButton (JToggleButton) is not selected (in the STOP mode)
                break;
            }
            sites = new ArrayList<>();
            String url = SiteActions.nextSite();
            depth = SiteActions.nextDepth();
            if(depth > maxDepth)    // If current depth is greater than maxDepth break out of work
                break;

            String siteData = SiteActions.getSiteData(url);

            /*
            If there is no site in the queue the queue nextSite will be null and getSiteData() will also return null
            Thus, this functions serves two problems:
            1 - If link is invalid do not add it to the map
            2 - If there is no link in the queue wait for nearly 10 seconds (sleep function) and try to find a link after each second
                If no link is found even after 10 seconds then probably there are no more links in the site.
                So the worker will go down or stop working after that
             */
            if(siteData == null){
                noSites++;
                if(noSites >= 10) {
                    stopIt = true;
                    break;
                }
                try {
                    sleep(1000);
                } catch (Exception e){
                    System.out.println("Worker:"+getName()+" is down");
                }
                continue;
            }

            String title = SiteActions.getTitle(siteData);

            SiteActions.putSite(url, title);

            SiteActions.getLinks(sites, siteData, url);

            SiteActions.addSitesToParse(sites, (depth+1));

            noSites = 0;
        }
        if(depth >= maxDepth) {
            timer.stopThread();
            runButton.setSelected(false);
        }
        SiteActions.workerDown();
        runButton.disable();
        System.out.println("Worker:"+getName()+" is going down");
        currentActive.setText(Integer.toString(SiteActions.getActiveWorkers()));
    }

    public void stopThread(){
        stopIt = true;
    }
}