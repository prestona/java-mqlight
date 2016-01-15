package com.ibm.mqlight.api.impl;

import java.io.File;

import com.ibm.mqlight.api.NonBlockingClient;
import com.ibm.mqlight.api.NonBlockingClientBuilder;
import com.ibm.mqlight.api.NonBlockingClientListener;
import com.ibm.mqlight.api.impl.callback.ThreadPoolCallbackService;
import com.ibm.mqlight.api.impl.endpoint.SingleEndpointService;
import com.ibm.mqlight.api.impl.network.NettyNetworkService;
import com.ibm.mqlight.api.impl.timer.TimerServiceImpl;
import com.ibm.mqlight.api.logging.Logger;
import com.ibm.mqlight.api.logging.LoggerFactory;

public class NonBlockingClientBuilderImpl implements NonBlockingClientBuilder {

    private static final Logger logger = LoggerFactory.getLogger(NonBlockingClientBuilderImpl.class);

    private String id = null;
    private File certificateFile = null;
    private boolean verifyName = false;
    private String service;
    private String username;
    private String password;

    @Override
    public NonBlockingClientBuilder id(String id) throws IllegalArgumentException {
        NonBlockingClientBuilderImpl.validateId(id);
        this.id = id;
        return this;
    }

    protected static void validateId(String id) throws IllegalArgumentException {
        final String methodName = "setId";
        logger.entry(methodName, id);

        if (id != null) {
            if (id.length() > 256) {
              final IllegalArgumentException exception = new IllegalArgumentException("Client identifier '" + id + "' is longer than the maximum ID length of 256.");
              logger.throwing(methodName, exception);
              throw exception;
            } else if (id.length() < 1) {
              final IllegalArgumentException exception = new IllegalArgumentException("Client identifier must be a minimum ID length of 1.");
              logger.throwing(methodName, exception);
              throw exception;
            }
            for (int i = 0; i < id.length(); ++i) {
                if (!"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789%/._".contains(id.substring(i, i+1))) {
                  final IllegalArgumentException exception = new IllegalArgumentException("Client identifier '" + id + "' contains invalid character: '" + id.substring(i, i+1) + "'");
                  logger.throwing(methodName, exception);
                  throw exception;
                }
            }
        }

        logger.exit(methodName);
    }

    @Override
    public NonBlockingClientBuilder trustCertificate(File certificateFile) {
        this.certificateFile = certificateFile;
        return this;
    }

    @Override
    public NonBlockingClientBuilder verifyHostName(boolean verifyName) {
        this.verifyName = verifyName;
        return this;
    }

    @Override
    public NonBlockingClientBuilder service(String service) {
        // TODO: need to validate that service is not null
        this.service = service;
        return this;
    }

    @Override
    public NonBlockingClientBuilder credentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    @Override
    public <C> NonBlockingClient build(NonBlockingClientListener<C> listener, C context) {
        // TODO: need to validate that service is not null
        SingleEndpointService endpointService = new SingleEndpointService(service, username, password, certificateFile, verifyName);
        return new NonBlockingClientImpl(endpointService,
                new ThreadPoolCallbackService(5),
                new NettyNetworkService(),
                new TimerServiceImpl(),
                null,
                id,
                listener,
                context);
    }

    @Override
    public NonBlockingClient build(NonBlockingClientListener<?> listener) {
        return build(listener, null);
    }
}
