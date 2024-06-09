# ec2 instance setup for the docker image pull from dockerhub and run

# install dependencies if amazon linux instance give .NET 6.0 error
sudo ./bin/installdependencies.sh

# install .NET
sudo yum install libicu -y

# install docker
sudo yum install -y docker