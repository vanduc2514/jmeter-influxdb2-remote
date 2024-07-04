package com.github.vanduc2514;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.RemoteJMeterEngineImpl;
import org.apache.jmeter.rmi.RMIServerSocketFactoryImpl;
import org.apache.jmeter.rmi.RmiUtils;
import org.apache.jmeter.rmi.SSLRMIServerSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.RMIServerSocketFactory;

public class IntegrationTestBootstrapper {

    public static void main(String[] args) throws IOException {
        // Server start
        RemoteJMeterEngineImpl.startServer(RmiUtils.getRmiRegistryPort());
        // Client start
        JMeter jMeter = new JMeter();
        jMeter.start(new String[]{"command line"});

    }

}
