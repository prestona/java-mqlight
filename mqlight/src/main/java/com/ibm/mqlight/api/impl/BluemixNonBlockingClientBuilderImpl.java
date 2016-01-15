package com.ibm.mqlight.api.impl;

import java.util.regex.Pattern;

import com.ibm.mqlight.api.BluemixNonBlockingClientBuilder;
import com.ibm.mqlight.api.NonBlockingClient;
import com.ibm.mqlight.api.NonBlockingClientListener;
import com.ibm.mqlight.api.impl.callback.ThreadPoolCallbackService;
import com.ibm.mqlight.api.impl.endpoint.BluemixEndpointService;
import com.ibm.mqlight.api.impl.network.NettyNetworkService;
import com.ibm.mqlight.api.impl.timer.TimerServiceImpl;

public class BluemixNonBlockingClientBuilderImpl implements BluemixNonBlockingClientBuilder {

    private String id;
    private Pattern serviceNamePattern;
    private Pattern serviceLabelPattern;

    @Override
    public BluemixNonBlockingClientBuilder id(String id) throws IllegalArgumentException {
        NonBlockingClientBuilderImpl.validateId(id);
        this.id = id;
        return this;
    }

    @Override
    public BluemixNonBlockingClientBuilder serviceName(String name)  {
        this.serviceNamePattern = Pattern.compile(Pattern.quote(name));
        return this;
    }

    @Override
    public BluemixNonBlockingClientBuilder serviceName(Pattern pattern) {
        this.serviceNamePattern = pattern;
        return this;
    }

    @Override
    public BluemixNonBlockingClientBuilder serviceLabel(Pattern pattern) {
        this.serviceLabelPattern = pattern;
        return this;
    }

    @Override
    public BluemixNonBlockingClientBuilder serviceLabel(String name) {
        this.serviceLabelPattern = Pattern.compile(Pattern.quote(name));
        return this;
    }

    @Override
    public <C> NonBlockingClient build(NonBlockingClientListener<C> listener,
            C context) {
        BluemixEndpointService endpointService = new BluemixEndpointService(serviceLabelPattern, serviceNamePattern);
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
        // TODO Auto-generated method stub
        return null;
    }

}
