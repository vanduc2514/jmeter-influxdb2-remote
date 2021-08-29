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
    wget -O "lib/ext/jmeter-junit-xml-converter-0.0.1-SNAPSHOT-jar-with-dependencies.jar" https://docs.getxray.app/download/attachments/62262689/jmeter-junit-xml-converter-0.0.1-SNAPSHOT-jar-with-dependencies.jar && \
    java -cp lib/ext/jmeter-plugins-manager-1.6.jar org.jmeterplugins.repository.PluginManagerCMDInstaller

# Install required plugins
RUN wget -O "lib/ext/jmeter-plugin-influxdb2-listener-1.1.jar" https://github.com/mderevyankoaqa/jmeter-influxdb2-listener-plugin/releases/download/v1.1/jmeter-plugin-influxdb2-listener-1.1.jar
COPY plugins.txt .
RUN bin/PluginsManagerCMD.sh install $(cat plugins.txt | grep ^[^#])

COPY jmeter-influxdb2-remote-0.9.jar ./lib/ext

# Copy Jmeter entrypoint
COPY entrypoint.sh /
RUN chmod +x /entrypoint.sh

COPY user.properties ./bin

EXPOSE 1099 50000 60000 8000

ENTRYPOINT ["/entrypoint.sh"]

