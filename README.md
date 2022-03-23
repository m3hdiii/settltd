# **Introduction**

![Build Status](passing.svg)

Based on what is asked from the question this app:
- will extract the text content
- create a structure for the reader to read and understand
- it shows the result both in console and in an output file
- process both plain text file and pdf file
- calculates the number of words, number of dots and most used words

### Boot Phase
- This application create four folders in the root folder path (you define the path inside data.properties file)
- The system copy LINCOLN_CONTRACT.pdf inside input folder, fot the demo to work 
- You can copy any pdf file inside that folder afterwards. The system is intelligence enough to understand changes on input folder. It gets the newly copied file and then process it.
- The final result, store inside output folder and the processed file will move to processed folder
- The system stay alive and receive any new file copy to input folder and process them all 

### Running the application:
To run the application you should: 

- copy the LINCOLN_CONTRACT.pdf inside the root path of src/main/resources folder
- configure data.properties file to point to a path inside your system
- compile and build the app using maven. To do that follow the below instruction:
- navigate to the project root directory where [pom.xml] file exists.
- open a console/cmd prompt terminal and execute this command --> mvn clean install
  Take note you should have installed maven before doing the above.

The above commands compile and build the project using maven and create a new directory called *target.
Navigate to [target] directory and you will see 2 files:

- demo-0.0.1-SNAPSHOT.jar (to run the application in all platforms, Linux, Windows, Mac, etc)
- settld-solution-0.0.1-SNAPSHOT.exe (to run the application in windows applications only)

All of the above run the same application but in different environment. You can run:
```sh
java -jar demo-0.0.1-SNAPSHOT.jar
cmd -> settld-solution-0.0.1-SNAPSHOT.exe
```
After you run the application, the app will create input, processed, unsupported folders if they don't exist within the root folder..
In this phase:
- If there are files withint the input folder, they are processed automatically
- If you copy some file within the input folder, the application will process those files too
- The results will be available within the console
- The file will move from input folder to the processed folder
- Just in case if the file is not a plain text file, the application recognize that and move the file to unsupported folder 

---

I tries to implement all the requirements and I tested the solution on **Linux Ubuntu version 20.04 and Windows 10 Machine**. Moreover, for better explanation, I put some documents for the classes and interfaces..



> Sincerely Yours (Mehdi Afsarikashi - March 3 2022)