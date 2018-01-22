rm -rf server && rm -rf client && mkdir server && mkdir client

# install the fastsig library from the repo
mvn install:install-file -Dfile=fastsig.jar -DgroupId=fastsig -DartifactId=fastsig -Dpackaging=jar -Dversion=1

