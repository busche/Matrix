= Matrix build instructions

== Ant

type 

	ant
	
and follow the instructions (error messages) to get the project up and running.

== Maven

type

	mvn deploy

to run a complete cycle.

Note:

* You need to define the repository '''bcs-repository''' along with username/password information in your ~/.m2/settings.xml file in order to actually deploy the artifact.
* Note that building using Maven will *not yet work* as the repository information points to a private network. You will not be able to build the project. This is a pending TODO.
