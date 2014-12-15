FROM dockerfile/java:openjdk-7-jdk

# Install build dependencies
RUN apt-get update
RUN apt-get install -y ant

# Check out and install batfish
RUN git clone https://github.com/arifogel/batfish.git
RUN echo ". /data/batfish/tools/batfish_functions.sh" > /etc/profile.d/Z98-batfish.sh
RUN cd batfish && bash --login -c "batfish_build"

# Install LogicBlox.  Note: this must be downloaded manually because of licensing.  Please do not publish the docker container to a public repository.
RUN echo "Installing LogicBlox.  You MUST have downloaded this from https://download.logicblox.com/release-archive-lb4/ (authentication required)."
ADD logicblox-4.1.3.tar.gz /data/
ADD logicblox-env /etc/profile.d/Z99-logicblox.sh

RUN sed -i '/^tcp_server_max_idle_time =/s/=.*/= 0/' logicblox-4.1.3/lb-web/config/lb-web-server.config

# Open LogicBlox Port
EXPOSE 55179 55179

# Open LogicBlox Web Portal Port
EXPOSE 8080 8080

# Open LogicBlox Web Admin Port
EXPOSE 55183 55183

# TODO: Run this on a machine with > 2G in /dev/shm
# RUN bash --login slb && batfish_build lbcompile
