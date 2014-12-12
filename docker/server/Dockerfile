FROM dockerfile/java:openjdk-7-jdk

# Install build dependencies
RUN apt-get update
RUN apt-get install -y ant

# Check out and install batfish
RUN git clone https://github.com/arifogel/batfish.git
RUN echo ". /data/batfish/tools/batfish_functions.sh" > /etc/profile.d/Z98-batfish.sh
RUN cd batfish && bash --login -c "batfish_build"

# Install LogicBlox.  Note: this must be downloaded manually because of licensing.  Please do not publish the docker container to a public repository.
RUN echo "TODO:  Install Real LogicBlox.  The next step will fail."
ADD logicblox logicblox
