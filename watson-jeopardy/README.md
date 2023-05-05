# Watson Jeopardy

## Description of Code
The bulk of the project exists in the Jeopardy.java and Index.java. The configuration that gave us the highest score was...
`mvn package`
`mvn exec:java -Dexec.mainClass=arizona.Jeopardy -Dexec.args="-e -n"`

### Jeopardy.java
This file has the main function. This class scans the question docs and instantiates the Index. It also compares the correct answer to the produced answer in order to evaluate effectiveness.

### Index.java
Builds the Index from the Wikipedia dataset. Includes Analyzer options, lemmatization, query/title processing, etc. in order to also provide the "best" document for a given query.

### Jeopardy.py
This file was just used to easily run the different analyzer options to compare scores. It utilizes the subprocess library in Python in order to run bash commands to package the maven project and execute the Java code with different commandline arguments that control the analyzer and scoring functions.

## Autorunner Instructions
`python3 Jeopardy.py`
This instruction will run multiple analyzer options in order to make quick comparisons.