# b\_verify - Efficient, Searchable Tamper-Evident Logs Using Bitcoin 

Currently _b\_verify_ is in development and _is not ready for production use_.

# Development Setup

## Prerequisites 
* Java Version 9 and a Java IDE. Download and install [Java Version 9 from Oracle](http://www.oracle.com/technetwork/java/javase/overview/index.html). One good Java IDE is [Eclipse](https://www.eclipse.org/ide/)

* Maven. Maven is used to manage builds and dependencies. Download it using apt-get or via the [Apache Maven website](https://maven.apache.org/).

* Bitcoin. Download and install the official Bitcoin command line client [by following these instructions](https://bitcoin.org/en/full-node#what-is-a-full-node) from the Core Developers.  The Bitcoin client comes in two pieces - `bitcoind` which is a daemon that syncs with the network and verifies the chain and `bitcoin-cli` which you can use to interact with the blockchain. Take a loot at the btc-scripts/README.md for more notes on this. There is also a Bitcoin GUI `bitcoin-qt` that can be downloaded. Note that currently _b\_verify_ development uses Bitcoin only in regtest mode, so there is no need to download and verify the entire blockchain. To use _b\_verify_ trustlessly you will need to download and verify the entire chain 150+gb chain, but once that is done it completely fine to throw old blocks away and run a _pruned node_ that only takes up a few gb of space.

* Google Protobuf. Download and install [google protobuf](https://github.com/google/protobuf). This is used in _b\_verify_ to handle serialization of messages that is efficient and language agnostic. b\_verify only defines a message format and Google Protobuf creates code to serialize and deserialize the data. 

* Fastsig. This is an implementation of the history tree datastructure that has been adapted for _b\_verify_. The source code is located [here](https://github.com/henryaspegren/fastsig). Clone the repo to a separate folder. 

## Installation
The building and testing of _b\_verify_ is managed by Maven. 

1. First you need to install fastsig - the _b\_verify_ data structure dependency library. Go into the folder where you have copied the source code and execute the build script
`$ sh build.sh`
This script will generate the serialization code using google protobuf and install the fastsig library as a dependency using Maven. This script also runs the unit tests for the library and fails the build if any of the tests fail. 

2. Next in the main project folder run
`$ sh setup-bverify.sh`
This should configure the environment and install _b\_verify_ using Maven. It also runs all unit tests and fails the build if any of the tests fail. You can also re-run any tests by typing 
`$ mvn test`

## Notes on Maven 

__To clean, recompile, test and install run__
`$ mvn clean install`

# Structure 

## Proofs
The various proofs used by _b\_verify_ are in their own package and can be ported to any language 

## Tests
There are unit tests written in JUnit for the proofs and for the general system. Integration tests are coming soon.

# Benchmarks 
Benchmarking code for determining the sizes of different objects (e.g. proofs) are in the Benchmarking source folder. 
There is also benchmarking code for testing throughput. Because of the nature of this code it is in a separate repo called bverify_benchmarking. Throughput benchmarks should be run as stand alone code. All benchmarking results are stored in the __analysis__ folder. The Java benchmarking source code generates CSV output files which are stored in the __anaylsis/benchmarking__ folder. Additionally there are python scripts to analyze the benchmarking results and visualize the data in the __analysis/datavisscripts__ folder. 



