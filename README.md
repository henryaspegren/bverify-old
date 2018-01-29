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
TBD

# Benchmarks 
All benchmarking code is in the benchmark source folder. Benchmarking results are stored in the __analysis__ folder. The Java benchmarking source code generates CSV output files which are stored in the __anaylsis/benchmarking__ folder. Additionally there are python scripts to analyze the benchmarking results and visualize the data in the __analysis/datavisscripts__ folder. 



