/*
This class in run first when main of ApplicationRunner.java is run
This class creates the front-end part in Swing
And starts all the processes
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;

public class WebCrawler extends JFrame {
    public WebCrawler(){
        super("Web Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(null);
        setLocationRelativeTo(null);

        //All these are font-end components - widgets of the swing library

        JLabel startLabel = new JLabel("Start URL:");
        startLabel.setBounds(10, 20, 100, 30);
        add(startLabel);

        JTextField urlField = new JTextField();
        urlField.setName("UrlTextField");
        urlField.setBounds(120, 20, 360, 30);
        add(urlField);

        JToggleButton runButton = new JToggleButton("Run");
        runButton.setName("RunButton");
        runButton.setBounds(490, 20, 80, 30);
        add(runButton);

        JLabel workersLabel = new JLabel("Workers:");
        workersLabel.setBounds(10, 70, 100, 30);
        add(workersLabel);

        JTextField workersField = new JTextField("5");
        workersField.setName("WorkersTextField");
        workersField.setBounds(120, 70, 450, 30);
        add(workersField);

        JLabel maxDepth = new JLabel("Maximum Depth:");
        maxDepth.setBounds(10, 120, 100, 30);
        add(maxDepth);

        JTextField depthNumber = new JTextField("10");
        depthNumber.setName("DepthTextField");
        depthNumber.setBounds(120, 120, 360, 30);
        add(depthNumber);

        JCheckBox depthCheck = new JCheckBox("Enabled");
        depthCheck.setName("DepthCheckBox");
        depthCheck.setBounds(490, 120, 80, 30);
        add(depthCheck);

        JLabel timeLimitLabel = new JLabel("Time Limit:");
        timeLimitLabel.setBounds(10, 170, 100, 30);
        add(timeLimitLabel);

        JTextField timeLimit = new JTextField();
        timeLimit.setBounds(120, 170, 240, 30);
        add(timeLimit);

        JLabel timeUnit = new JLabel("seconds");
        timeUnit.setBounds(370, 170, 90, 30);
        add(timeUnit);

        JCheckBox timeCheck = new JCheckBox("Enabled");
        timeCheck.setBounds(490, 170, 80, 30);
        add(timeCheck);

        JLabel timeLabel = new JLabel("Elapsed Time:");
        timeLabel.setBounds(10, 220, 100, 30);
        add(timeLabel);

        JLabel timeElapsed = new JLabel("0:00");
        timeElapsed.setBounds(120, 220, 450, 30);
        add(timeElapsed);


        JLabel pageLabel = new JLabel("Parsed Pages");
        pageLabel.setBounds(10, 270, 100, 30);
        add(pageLabel);

        JLabel parsedLabel = new JLabel("0");
        parsedLabel.setName("ParsedLabel");
        parsedLabel.setBounds(120, 270, 450, 30);
        add(parsedLabel);

        JLabel exportLabel = new JLabel("Export");
        exportLabel.setBounds(10, 320, 100, 30);
        add(exportLabel);

        JTextField exportField = new JTextField();
        exportField.setName("ExportUrlTextField");
        exportField.setBounds(120, 320, 360, 30);
        add(exportField);

        JLabel exportInfo = new JLabel("*export to .html files for tabular alignment of links");
        exportInfo.setBounds(120, 350, 450, 10);
        Font font = new Font("Courier", Font.ITALIC, 8);
        exportInfo.setFont(font);
        exportInfo.setForeground(Color.RED);
        add(exportInfo);

        JButton exportButton = new JButton("Save");
        exportButton.setName("ExportButton");
        exportButton.setBounds(490, 320, 80, 30);
        add(exportButton);

        JLabel workerNumberLabel = new JLabel("Active Workers:");
        workerNumberLabel.setBounds(10, 370, 100, 30);
        add(workerNumberLabel);

        JLabel workersActive = new JLabel("0");
        workersActive.setBounds(120, 370, 360, 30);
        add(workersActive);

        JLabel errorLabel = new JLabel("");
        errorLabel.setBounds(10, 420, 450, 30);
        errorLabel.setForeground(Color.RED);
        add(errorLabel);


        SiteActions.setActiveWorkers(1);
        runButton.addItemListener(new ItemListener() {

            // ItemListener added to the Run button

            @Override
            public void itemStateChanged(ItemEvent e) {
                int state = e.getStateChange();
                ElapsedThread timer = new ElapsedThread(timeElapsed, parsedLabel, runButton, workersActive);

                if(state == ItemEvent.SELECTED){    // Run button is pressed
                    runButton.setText("Stop");
                    SiteActions.clearSites();
                    timer.start();

                    try {
                        int numWorkers = Integer.parseInt(workersField.getText());
                        if(numWorkers<=0)
                            numWorkers = 1;
                        SiteActions.setMaxWorkers(numWorkers);

                        if(depthCheck.isSelected()) {
                            SiteActions.setMaxDepth(Integer.parseInt(depthNumber.getText()));
                        } else {
                            SiteActions.setMaxDepth(-1);
                        }

                        if(timeCheck.isSelected()) {
                            SiteActions.setMaxTime(Integer.parseInt(timeLimit.getText()));
                        } else {
                            SiteActions.setMaxTime(-1);
                        }

                        /*
                        Getting the links from the first site
                        So that the threads do not shut down
                        Since there will ony be one link in the queue
                         */

                        String url = urlField.getText();
                        String siteData = SiteActions.getSiteData(url);
                        String getTitle = SiteActions.getTitle(siteData);
                        ArrayList<String> links = new ArrayList<>();
                        SiteActions.getLinks(links, siteData, url);

                        SiteActions.putSite(url, getTitle);

                        SiteActions.addSitesToParse(links, 1);

                        /*
                        Creating threads/workers
                         */
                        Worker[] workers = startCrawling(numWorkers, runButton, timer, workersActive);
                        SiteActions.setActiveWorkers(numWorkers);
                    } catch (Exception E) {
                        runButton.setSelected(false);   // Do not allow running if any exception occurs
                        errorLabel.setText("Please check all the input fields!");
                        System.out.println(E.getMessage());
                    }
                } else {
                    // Stop button is pressed

                    timer.stopThread();
                    runButton.setText("Run");
                }
            }
        });

        exportButton.addActionListener(new ActionListener() {
            /*
            ActionListener added to Export button
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if(exportField.getText().trim().length() == 0)
                    errorLabel.setText("Invalid File Name");
                saveFile(errorLabel, exportField.getText(), parsedLabel);
            }
        });

        setVisible(true);
    }

    public Worker[] startCrawling(int workerNumber, JToggleButton runButton, ElapsedThread timer, JLabel activeWorkers){
        // This function is used to start the threads
        if(workerNumber <= 0)   // There must at-least be one worker to parse the pages
            workerNumber = 1;
        Worker[] workers = new Worker[workerNumber];
        for(int i=0; i<workers.length; i++){
            workers[i] = new Worker(runButton, timer, activeWorkers);
            workers[i].start();
        }
        return workers;
    }

    public void saveFile(JLabel errorLabel, String fileName, JLabel parsedPages){
        boolean matches = fileName.matches(".*\\.html");    // Checking if the input file is a HTML file
        try{
            File file = new File(fileName);
            PrintWriter printWriter = new PrintWriter(file);

            Map<String, String> parsedSites = SiteActions.getParsedSites();
            int num = 0;
            if(matches){
                /*
                To style the HTML table
                I use open source CSS framework bulma: https://bulma.io/
                 */
                printWriter.println("<!DOCTYPE html>");
                printWriter.println("<html>");
                printWriter.println("<head>");
	            printWriter.println("<title>Example Page</title>");
                printWriter.println("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/bulma/0.6.2/css/bulma.min.css\">");
                printWriter.println("</head>");
                printWriter.println("<body>");
                printWriter.println("<div class=\"table-container\" style=\"position: relative; margin: 50px auto; display: block;\">");
                printWriter.println("<table class=\"table\">");
                printWriter.println("<thead>");
                printWriter.println("<tr>");
                printWriter.println("<th>#</th>");
                printWriter.println("<th>Found Links</th>");
                printWriter.println("</tr>");
                printWriter.println("</thead>");
                printWriter.println("<tbody>");
            }
            for (String key: parsedSites.keySet()) {
                num++;
                if(matches){
                    printWriter.println("<tr>");
                    printWriter.println("<td>"+num+"</td>");
                    if(parsedSites.get(key).trim().length() == 0)
                        printWriter.println("<td><a class=\"is-link\" href=\""+key+"\">"+"Title Unavailable"+"</a></td>");
                    else
                        printWriter.println("<td><a class=\"is-link\" href=\""+key+"\">"+parsedSites.get(key)+"</a></td>");
                    printWriter.println("</tr>");
                } else {
                    printWriter.println(num + "->\tSiteUrl->\t" + key);
                    printWriter.println("  \tTitle->\t" + parsedSites.get(key));
                    printWriter.println();
                }
            }
            if(matches){
                printWriter.println("</tbody>");
                printWriter.println("</table>");
                printWriter.println("</div>");
                printWriter.println("</body>");
                printWriter.println("</html>");
            }
            parsedPages.setText(Integer.toString(num));
            printWriter.close();
        } catch (Exception e) {
            errorLabel.setText("Invalid File Name");
        }
    }
}