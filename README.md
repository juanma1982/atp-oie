# atp-oie
An autonomous Open Information Extraction Method

## Steps to compile ATP-OIE application

    1. user must have Java 1.8 or greater installed and Maven (https://maven.apache.org/install.html). (tested with maven 3.5.2 and 3.3.9)
    
 		mvn clean compile assembly:single

	This step will download all needed libraries and then will build ATP-OIE from source code.
    2. When finish you should see a “BUILD SUCCESS” message similar to the following:
	[INFO] --------------
	[INFO] BUILD SUCCESS
	[INFO] --------------

    3. The application should be created into “target” directory. Please move the jar file to the parent directory and rename as “atp-oie.jar”, you can do that executing the following command:

	mv target/ATP-OIE-1.0.1-jar-with-dependencies.jar atp-oie.jar
	
## Steps to run ATP-OIE

    1. If you are in a Linux environment, you can execute the file “runATP-OIE.sh”, the available options are:

 	-f : mandatory param, indicates the input text file
 	-o : indicates the output file. If not present, the result will be printed in console
 	-reverb :  Use reverb for sentences without extractions 
 	-clausie : Use ClausIE for sentences without extractions (after using Reverb if it is also set) 
 	-trainOnline : if Reverb and/or ClausIE are set, this flag allow the creation of new extraction
			patterns using the relations extracted of this methods. 
 	-score : also prints the score of the exraction
 	-full : prints score, id, and if the relation is non factual its dependency
 	-help : prints this menu


	for example, you can execute:

	./runATP-OIE.sh -f testFile.txt -full -o out.txt


    2. If you are under Windows environment or another SO, you must run the application with the following command:

	java -jar -Xmx4056m  -Xms1024m -ea atp-oie.jar 

	the parameters are the same, for example:

	java -jar -Xmx4056m  -Xms1024m -ea atp-oie.jar -f testFile.txt -full -reverb
