# start docker daemon
sudo service docker start;
sudo chmod 666 /var/run/docker.sock

# run the docker container
docker rm -f investment-tracker-container;
docker run -d -p 8080:8080 --name investment-tracker-container tperamasani/investment-tracker

# start github self hosted runner
./config.sh --url https://github.com/this-is-thiru/investment-tracker --token AQPGMTD56CBDC5ZV4CLLTVLGMSNNK;
./run.sh