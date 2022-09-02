# JDBC Execution Tool
 
Provides JDBC connection test and simple SQL execution.
 
 
## Prerequisites
 
1. Language: Java (JDK v.7+)
2. Build tool: Apache Ant (v.1.9+)
 
 
## Build and execution steps
 
1. Download source code.
 
2. Open jdbcexec.properties from the project root directory in editor, and set proper JDK directory
 
3. Open command line at the project root directory, and run ANT.
	ant
               
4. Change directory to distribution directory
	cd dist/lib
               
5. Open run script (e.g. run.bat) in editor, and set JAVA_HOME to proper JDK directory.
 
6. Open jdbc.properties in editor, and set connection information and, optionally, query.
 
7. In command line, execute the run script.
	run.bat


## Build and execution steps for restrictive environment
If the environment has security policy where full download (especially jar files), you may download source code files selectively, build manually, update files manually, and execute it.

1. Download source code selectively.
```
src/main/java/JDBCExec.java
src/main/resources/jdbc.properties
src/main/resources/run.bat
```
2. Copy JDBC jar file in the same directory
3. Build code in command line.
> For example,
```
javac -classpath ./sqljdbc.jar JDBCExec.java
```
4. Update jdbc.properties to specify proper connection info - driver, credential, etc.
5. Update run.bat for JDK path, CLASSPATH
6. Execute run.bat (convert to run.sh for Unix/Linux)

