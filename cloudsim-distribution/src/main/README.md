# CloudSim: A Framework For Modeling And Simulation Of Cloud Computing Infrastructures And Services #

Cloud computing is the leading approach for delivering reliable, secure, fault-tolerant, sustainable, and scalable computational services. Hence timely, repeatable, and controllable methodologies for performance evaluation of new cloud applications and policies before their actual development are reqruied. Because utilization of real testbeds limits the experiments to the scale of the testbed and makes the reproduction of results an extremely difficult undertaking, simulation may be used.

CloudSim goal is to provide a generalized and extensible simulation framework that enables modeling, simulation, and experimentation of emerging Cloud computing infrastructures and application services, allowing its users to focus on specific system design issues that they want to investigate, without getting concerned about the low level details related to Cloud-based infrastructures and services.

CloudSim is developed in [the Cloud Computing and Distributed Systems (CLOUDS) Laboratory](http://cloudbus.org/), at [the Computer Science and Software Engineering Department](http://www.csse.unimelb.edu.au/) of [the University of Melbourne](http://www.unimelb.edu.au/).

More information can be found on the [CloudSim's web site](http://cloudbus.org/cloudsim/).

This version contains various fixes and improvements over the CloudSim framework.

## Main features

  * support for modeling and simulation of large scale Cloud computing data centers
  * support for modeling and simulation of virtualized server hosts, with customizable policies for provisioning host resources to virtual machines
  * support for modeling and simulation of application containers
  * support for modeling and simulation of energy-aware computational resources
  * support for modeling and simulation of data center network topologies and message-passing applications
  * support for modeling and simulation of federated clouds
  * support for dynamic insertion of simulation elements, stop and resume of simulation
  * support for user-defined policies for allocation of hosts to virtual machines and policies for allocation of host resources to virtual machines

## Added and changed features

 * replaced custom logging system with [SLF4J](https://www.slf4j.org/): an end-user just needs to add to his classpath an
SLF4J binding to an actual logging framework such as [Log4J](https://logging.apache.org/log4j/) or [LogBack](https://logback.qos.ch/),
giving him full control over the logging behavior
 * various fixes and improvements to code logic

## Requirements

CloudSim is tested and runs on **Sun's Java and OpenJDK version 8.** Older versions of Java are not compatible. If you
have non-Sun Java version, such as gcj or J++, they may or may not be compatible.

## Package content

 * docs: Javadoc under HTML form;
 * library: JAR archives containing compiled CloudSim simulation library and corresponding sources;
 * examples: JAR archives containing compiled CloudSim examples and corresponding sources;

## Quick start

You can run some examples from the JAR "examples/cloudsim-examples.jar", under the package `org.cloudbus.cloudsim.examples`.
You can check the sources to know the list of all available examples.

**CloudSim library can be used from the JAR "lib/cloudsim.jar".**

If using Maven, you may want to clone the repository this
package comes from, and run `mvn install` to make the dependency `org.cloudbus.cloudsim:cloudsim-library` available from
your local repository. Also, the original CloudSim framework is available on Maven repositories, but **this version is not.**

## Documentation

There is not much documentation. Examples should be informative enough to get you started, and the Javadoc is available
throughout the library's code and in the "docs" folder as HTML.

## Publications

  * Anton Beloglazov, and Rajkumar Buyya, [Optimal Online Deterministic Algorithms and Adaptive Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in Cloud Data Centers](http://beloglazov.info/papers/2012-optimal-algorithms-ccpe.pdf), Concurrency and Computation: Practice and Experience, Volume 24, Number 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012.
  * Saurabh Kumar Garg and Rajkumar Buyya, [NetworkCloudSim: Modelling Parallel Applications in Cloud Simulations](http://www.cloudbus.org/papers/NetworkCloudSim2011.pdf), Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.
  * **Rodrigo N. Calheiros, Rajiv Ranjan, Anton Beloglazov, Cesar A. F. De Rose, and Rajkumar Buyya, [CloudSim: A Toolkit for Modeling and Simulation of Cloud Computing Environments and Evaluation of Resource Provisioning Algorithms](http://www.buyya.com/papers/CloudSim2010.pdf), Software: Practice and Experience (SPE), Volume 41, Number 1, Pages: 23-50, ISSN: 0038-0644, Wiley Press, New York, USA, January, 2011. (Preferred reference for CloudSim)**
  * Bhathiya Wickremasinghe, Rodrigo N. Calheiros, Rajkumar Buyya, [CloudAnalyst: A CloudSim-based Visual Modeller for Analysing Cloud Computing Environments and Applications](http://www.cloudbus.org/papers/CloudAnalyst-AINA2010.pdf), Proceedings of the 24th International Conference on Advanced Information Networking and Applications (AINA 2010), Perth, Australia, April 20-23, 2010.
  * Rajkumar Buyya, Rajiv Ranjan and Rodrigo N. Calheiros, [Modeling and Simulation of Scalable Cloud Computing Environments and the CloudSim Toolkit: Challenges and Opportunities](http://www.cloudbus.org/papers/CloudSim-HPCS2009.pdf), Proceedings of the 7th High Performance Computing and Simulation Conference (HPCS 2009, ISBN: 978-1-4244-4907-1, IEEE Press, New York, USA), Leipzig, Germany, June 21-24, 2009.

[![](http://www.cloudbus.org/logo/cloudbuslogo-v5a.png)](http://cloudbus.org/)
