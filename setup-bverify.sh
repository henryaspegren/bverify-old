echo '-----------setting up B_Verify-----------------'

rm -rf server && rm -rf client && mkdir server && mkdir client

# install the fastsig library from the repo
echo 'installing fastsig dependency' 
mvn install:install-file -Dfile=fastsig.jar -DgroupId=fastsig -DartifactId=fastsig -Dpackaging=jar -Dversion=1

# generate the google protobuf code
echo 'generating protobuf serialization code'
mkdir src/generated
protoc --java_out=src/generated serialization.proto 


echo '------------completed!-----------------------'

