
== Things that need to be done ==
* adapt IO to use novel file names.


== Download & Installation ==

get ISMLL-Matrices project from 

svn+ssh://svn.ismll.de/srv/svn/repositories/software/buza/Matrix

and follow the instructions in the read.me. In the run.sh-Script, you may want to remove everything below the execution of Java-Executable (line 75; the error handling is not working on the cluster)


== Sample: reading data ==

C:\workspace\ISMLLMatrices\output>java -cp .;kdd2011.jar;bootstrap-0.4.1.jar;log4j-1.2.15.jar;weka-3.7.3.jar de.ismll.kdd2011.IO

=== Sample for bootstrap: ===

	C:\workspace\ISMLLMatrices\output>java -cp .;kdd2011.jar;bootstrap-0.4.1.jar;log4j-1.2.15.jar;weka-3.7.3.jar de.ismll.console.Generic de.ismll.kdd2011.BootstrapIO track1=c:\work\kdd\2011\sampledata\track1

or on linux (in output folder):

	./run.sh de.ismll.kdd2011.BootstrapIO track1=/home/busche/kdd/2011/sampledata/track1

== Adjusting Console output ==

change entries in $ISMLLMatrices$/src/java/log4j.properties:

e.g.:
log4j.logger.de=ERROR # for nearly no output, except for errors, and those classes explicitly enabled:
log4j.logger.de.ismll.kdd2011=DEBUG

See log4j description on the web for more information...

