FROM openjdk:11.0.4-jre-slim

ARG JMETER_VERSION

ENV JMETER_VERSION ${JMETER_VERSION:-5.4.1}
ENV JMETER_HOME /jmeter/apache-jmeter-$JMETER_VERSION/
ENV PATH $JMETER_HOME/bin:$PATH

# Install Dependencies
RUN apt-get update -y && apt-get -y install wget

# Install Jmeter
RUN mkdir /jmeter
WORKDIR /jmeter
RUN wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-$JMETER_VERSION.tgz && \
    tar -xzf apache-jmeter-$JMETER_VERSION.tgz && \
    rm apache-jmeter-$JMETER_VERSION.tgz

# Enable debug
ENV JVM_ARGS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000

# Add slave configuration in distrubuted testing
COPY user.properties ./bin

EXPOSE 1099 50000 60000 8000

ENTRYPOINT jmeter-server

