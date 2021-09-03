package org.ifisolution.exeptions;

public class PluginException extends Exception {

    public PluginException(String message) {
        super(message);
    }

    public PluginException(Throwable cause) {
        super(cause);
    }

}
