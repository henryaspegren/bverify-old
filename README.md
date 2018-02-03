# BVERIFY - Efficient, Searchable Tamper-Evident Logs Using Bitcoin 


# Builds are managed using Maven. 

__To configure and install local dependencies__
$ sh setup-bverify.sh

__To install remote dependencies__
$ mvn install

__To run tests__
$ mvn test

# Proofs
The various proofs used by BVERIFY are in their own package and can be ported to any language 

# Tests
There are unit tests written in JUnit for the proofs and for the general system. Integration tests are coming soon.

# Benchmarks 
Benchmarking code for determining the sizes of different objects (e.g. proofs) are in the Benchmarking source folder. 
There is also benchmarking code for testing throughput. Because of the nature of this code it is in a separate repo called bverify_benchmarking. Throughput benchmarks should be run as stand alone code. All benchmarking results are stored in the __analysis__ folder. The Java benchmarking source code generates CSV output files which are stored in the __anaylsis/benchmarking__ folder. Additionally there are python scripts to analyze the benchmarking results and visualize the data in the __analysis/datavisscripts__ folder. 



