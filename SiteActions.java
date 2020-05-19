/*
This class is mainly used to control all the actions
THis site has all the content static
So as to allow different threads to communicate with each other
This controls everything like adding sites to parse to the queue
and keeping all the parsed Sites
This class also acts as a monitor, so that no two workers access Queues at the same time, so as to avoid data loss.
Telling timer thread and worker threads the constraints of maxDepth and maxTIme
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiteActions {
    private static final Map<String, String> parsedSites = new LinkedHashMap<>();   // Map stores all the parsed sites url->Title
    private static final Queue<String> siteQueue = new ConcurrentLinkedQueue<>();   // Queue stores all the sites to parse
    private static final Queue<Integer> siteDepth = new ConcurrentLinkedQueue<>();  // Queue stores the depth of the pages
    private static int maxDepth;
    private static int maxTime;
    private static int activeWorkers;

    public static void setMaxDepth(int maxDepth1){
        maxDepth = maxDepth1;
    }

    public static int getMaxDepth(){
        return maxDepth;
    }

    public static synchronized void addSitesToParse(ArrayList<String> urls, int depth){
        /*
        Checking that no site is repeated, so as to save extra work
         */
        for (String url : urls) {
            if (!siteQueue.contains(url) && !parsedSites.containsKey(url))
                siteQueue.add(url);
            siteDepth.add(depth);
        }
    }

    public static synchronized String nextSite(){
        return siteQueue.poll();
    }

    public static synchronized int nextDepth(){
        Integer depth = siteDepth.poll();
        if(depth == null)
            return -1;
        return depth;
    }

    public static Map<String, String> getParsedSites(){
        return parsedSites;
    }

    public static synchronized void putSite(String siteUrl, String title){
        parsedSites.put(siteUrl, title);
    }

    public static int numberOfSites(){
        return parsedSites.size();
    }

    public static void clearSites(){
        siteDepth.clear();
        siteQueue.clear();
        parsedSites.clear();
    }

    public static void setMaxTime(int maxTime1){
        maxTime = maxTime1;
    }

    public static long getMaxTime(){
        return maxTime;
    }

    public static void setActiveWorkers(int currentActive) {
        activeWorkers = currentActive;
    }

    public synchronized static int getActiveWorkers() {
        return activeWorkers;
    }

    public synchronized static void workerDown() {
        activeWorkers = activeWorkers - 1;
    }

    public static String getSiteData(String url){
        /*
        This method returns the complete HTML of the site
         */
        try {
            final InputStream inputStream = new URL(url).openStream();
            final BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            final StringBuilder stringBuilder = new StringBuilder();
            final String LINE_SEPARATOR = System.getProperty("line.separator");
            String nextLine;
            while((nextLine = reader.readLine()) != null){
                stringBuilder.append(nextLine);
                stringBuilder.append(LINE_SEPARATOR);
            }

            return stringBuilder.toString();
        } catch (Exception E){
            /*
            If the URL is wrong return null
             */
            return null;
        }
    }

    public static String getTitle(String siteData) {
        /*
        This function uses regex
        To find the <title></title> tag on the HTML page
        And returns the title of the current page
        If no title if found it returns empty String
         */
        Scanner scan = new Scanner(siteData);
        String title="";
        while(scan.hasNextLine()){
            String temp = scan.nextLine();
            if(temp.matches(".*<title>.*</title>.*")){
                int start = temp.indexOf("<title>");
                int end = temp.indexOf("</title>");
                String temp1 = temp.substring(start+7, end);
                if(temp1.trim().length() == 0)
                    title = "Window title is empty";
                else
                    title = temp1;
                break;
            }
        }
        return title;
    }

    public static void getLinks(ArrayList<String> sites, String siteData, String currentUrl){
        /*
        THis method uses Pattern and Matcher classes
        to find all the links on the HTML page
        It takes an ArrayList<String> as an argument, to which all the links are added
         */
        Scanner scan = new Scanner(siteData);
        while(scan.hasNext()){
            String temp = scan.nextLine();
            Pattern pattern = Pattern.compile("(href\\s?=\\s?\"(.*?)\"|href\\s?=\\s?'(.*?)')");
            Matcher matcher = pattern.matcher(temp);
            while(matcher.find()){
                String url = matcher.group();
                Scanner findUrl = new Scanner(url);
                findUrl.useDelimiter("(\")");
                findUrl.next();
                String foundUrl;
                try {
                    foundUrl = findUrl.next();
                } catch(NoSuchElementException e){
                    Scanner newFind = new Scanner(url);
                    newFind.useDelimiter("'");
                    foundUrl = newFind.next();
                }
                if(foundUrl.matches("https?://.*")){
                    foundUrl = "" + foundUrl;
                } else if (foundUrl.matches("//.*")){
                    if(currentUrl.contains("https://"))
                        foundUrl = "https:" + foundUrl;
                    else
                        foundUrl = "http:" + foundUrl;
                } else {
                    int lastSlash = currentUrl.lastIndexOf('/');
                    if(lastSlash<=7)
                        foundUrl = currentUrl + "/" + foundUrl;
                    else
                        foundUrl = currentUrl.substring(0,lastSlash) + "/" + foundUrl;
                }

                sites.add(foundUrl);
            }
        }
    }
}
