## Dockerfiles 

This folder contains the dockerfiles for batfish client and server.  See top level README for non-docker install instructions.

To build the docker files, go to either client or server directories and run:

    docker build .

#### Server

To start the server interactively (for now), run the following:

    docker run -i -t --privileged -p 8080:8080 -p 55179:55179 -p 55183:55183 [SERVER DOCKER IMAGE ID] /bin/bash --login
    mount -t tmpfs -o rw,nosuid,nodev,noexec,relatime,size=512M tmpfs /dev/shm
    slb

#### Client

To run the client against the server:

NOTE:  For the time being, find IP address of docker server and swap it into the docker-env file, then run docker build.  This is required until I figure out how to discover it.

    docker run -i -t [CLIENT DOCKER IMAGE ID] /bin/bash --login
    batfish_analyze batfish/test_rigs/example example
