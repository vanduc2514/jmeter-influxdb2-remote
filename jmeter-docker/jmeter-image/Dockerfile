FROM openjdk:11.0.4-jre-slim

ARG JMETER_VERSION

ENV JMETER_VERSION ${JMETER_VERSION:-5.4.1}
ENV JMETER_HOME /jmeter/apache-jmeter-$JMETER_VERSION/
ENV PATH $JMETER_HOME/bin:$PATH
# ENV JVM_ARGS -agentlib:jdwp=transport=dt_socket,server=y,address=8000

# Install Dependencies
RUN apt-get update -y && apt-get -y install wget

# Install Jmeter
RUN mkdir /jmeter
WORKDIR /jmeter
RUN wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-$JMETER_VERSION.tgz && \
    tar -xzf apache-jmeter-$JMETER_VERSION.tgz && \
    rm apache-jmeter-$JMETER_VERSION.tgz

# Install jmeter plugin manager
WORKDIR $JMETER_HOME
RUN wget -O "lib/cmdrunner-2.2.jar" https://repo1.maven.org/maven2/kg/apc/cmdrunner/2.2/cmdrunner-2.2.jar && \
    wget -O "lib/ext/jmeter-plugins-manager-1.6.jar" https://repo1.maven.org/maven2/kg/apc/jmeter-plugins-manager/1.6/jmeter-plugins-manager-1.6.jar && \
    java -cp lib/ext/jmeter-plugins-manager-1.6.jar org.jmeterplugins.repository.PluginManagerCMDInstaller

# Install required plugins
RUN wget -O "lib/ext/jmeter-influxdb2-remote-1.3.jar" https://github.com/vanduc2514/jmeter-influxdb2-remote/releases/download/v1.4/jmeter-influxdb2-remote-1.4.jar
COPY plugins.txt .
RUN bin/PluginsManagerCMD.sh install $(cat plugins.txt | grep ^[^#])

# Enable debug
ENV JVM_ARGS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000

# Enable the jmeter-slave for remote testing
COPY user.properties ./bin

EXPOSE 1099 50000 60000 8000

ENTRYPOINT jmeter-server

