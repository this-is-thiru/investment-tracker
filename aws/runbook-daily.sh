# start docker daemon
sudo service docker start;
sudo chmod 666 /var/run/docker.sock

# start github self hosted runner
./config.sh --url https://github.com/this-is-thiru/investment-tracker --token AQPGMTD56CBDC5ZV4CLLTVLGMSNNK;
./run.sh