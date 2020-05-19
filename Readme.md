# Simple Web Crawler Using JAVA

### Main features include:
* Seed site can be proivide by the user
* User can specify the maximum depth to be parsed by the crawler
* User can specify maximum time for which the crawler should run
* Number of workers (threads) can be specified by the user
* Process can be interrupted by the user
* Export the results to file

#### Some tips:
* Export to HTML sites for tablar view, which uses HTMl and [bulma](https://bulma.io/)
* Wait fot the active workers to get to zero, because workers complete the parsing of current page before shutting down
* Do not use very large number of threads (workers) as that might compromise the speed (because of synchronization)

## Sample image of the project is given below:
![sample example](https://github.com/vasudevsall/Web-Crawler/blob/master/images/sample.png?raw=true)

#### General information of the project:
* Project is created only using JAVA and some HTML and open-source CSS framework [bulma](https://bulma.io/)
* The complete GUI is created using swing (a light-weight JAVA widget library)
* The application is multi-threaded, which increases the speed of the parsing

## Running the application (using command prompt):
* Make sure JDK is installed in your system
* Go to the project folder
* Compile all the files using `javac className.java`
	- You have to compile all the `.java` files one by one
* Now run the `ApplicationRunner.class` file using `java ApplicationRunner`
* Now input all the required fields and press the Run button
* After the completion of the process save and view the result by entering a fileName and pressing the Export button

## Runnning the application (using IDE):
* Run the main function of the ApplicationRunner.java using the IDE
* Now input all the required fields and press the Run button
* After the completion of the process save and view the result by entering a fileName and pressing the Export button
