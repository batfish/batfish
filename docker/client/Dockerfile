FROM dockerfile/java:openjdk-7-jdk

# Install build dependencies
RUN apt-get update
RUN apt-get install -y parallel git autoconf ant

# Check out and install Z3 constraint solver
RUN git clone https://git.codeplex.com/forks/nunoplopes/hassel
RUN cd hassel; autoconf; ./configure
RUN cd hassel; python scripts/mk_make.py
RUN cd hassel/build && make -j2
RUN cd hassel/build && make install

# Check out and install batfish
RUN git clone https://github.com/arifogel/batfish.git
RUN echo ". /data/batfish/tools/batfish_functions.sh" > /etc/profile.d/Z98-batfish.sh
RUN cd batfish && bash --login -c "batfish_build"

# Remaining environment setup (pointing to batfish server, etc) should be done via docker -e flags.
