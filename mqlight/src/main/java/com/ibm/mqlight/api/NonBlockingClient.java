/*
 *   <copyright 
 *   notice="oco-source" 
 *   pids="5725-P60" 
 *   years="2015" 
 *   crc="1438874957" > 
 *   IBM Confidential 
 *    
 *   OCO Source Materials 
 *    
 *   5724-H72
 *    
 *   (C) Copyright IBM Corp. 2015
 *    
 *   The source code for the program is not published 
 *   or otherwise divested of its trade secrets, 
 *   irrespective of what has been deposited with the 
 *   U.S. Copyright Office. 
 *   </copyright> 
 */

package com.ibm.mqlight.api;

import java.nio.ByteBuffer;
import java.util.Map;

import com.ibm.mqlight.api.callback.CallbackService;
import com.ibm.mqlight.api.endpoint.EndpointService;
import com.ibm.mqlight.api.impl.Component;
import com.ibm.mqlight.api.impl.NonBlockingClientImpl;
import com.ibm.mqlight.api.network.Network;

/**
 * A Java MQ Light client implementation that never blocks the calling thread when
 * carrying out messaging operations.  Notification of completion for any
 * (potentially blocking) operation is via a callback.
 * <p>
 * Example code for using the non-blocking client to send a message
 * <pre>
 * NonBlockingClient.create("amqps://localhost", new NonBlockingClientAdapter() {
 *     public void onStarted(NonBlockingClient client, Object context) {
 *         client.send("/kittens", "Hello kitty!");
 *         client.stop(null, null);
 *     }
 * }, null);
 * </pre>
 * <p>
 *
 * Example code for receiving messages published to the '/kittens' topic.
 * <pre>
 * NonBlockingClient client = NonBlockingClient.create("amqps://localhost", null, null);
 *     client.subscribe("/kittens", new DestinationAdapter() {
 *     public void onMessage(NonBlockingClient client, Object context, Delivery delivery) {
 *         switch (delivery.getType()) {
 *         case BYTES:
 *             BytesDelivery bd = (BytesDelivery)delivery;
 *             System.out.println(bd.getData());
 *             break;
 *         case STRING:
 *             StringDelivery sd = (StringDelivery)delivery;
 *             System.out.println(sd.getData());
 *             break;
 *         }
 *     }
 * }, null, null);
 * </pre>
 */
public abstract class NonBlockingClient extends Component { // TODO: not particularly sure I like this hierarchy...

    private static final ClientOptions defaultClientOptions = ClientOptions.builder().build();
    private static final SendOptions defaultSendOptions = SendOptions.builder().build();
    private static final SubscribeOptions defaultSubscribeOptions = SubscribeOptions.builder().build();
    
    /**
     * Creates a new instance of the <code>NonBlockingClient</code> in starting state.
     * @param service a URI for the service to connect to, for example: <code>amqp://example.org:5672</code>.
     *        This URI can start with either <code>amqp://</code> or <code>amqps://</code> (for SSL/TLS based
     *        connections).  User names and passwords may be embedded into the URL - for example:
     *        <code>amqp://user:pass@example.com</code>.
     * @param options a set of options that determine the behaviour of the client.
     * @param listener a listener that is notified of major life-cycle events for the client.
     * @param context a context object that is passed into the listener.  This can be used within the listener code to
     *                identify the specific instance of the create method relating to the listener invocation.
     * @return a new instance of <code>NonBlockingClient</code>
     * @throws IllegalArgumentException thrown if one or more of the <code>options</code> is not valid.
     */
    public static <T> NonBlockingClient create(String service, ClientOptions options, 
            NonBlockingClientListener<T> listener, T context) 
    throws IllegalArgumentException {
        return new NonBlockingClientImpl(service, options, listener, context);
    }

    /**
     * TODO
     * @param endpointService
     * @param callbackService
     * @param network
     * @param options
     * @param listener
     * @param context
     * @return a new instance of <code>NonBlockingClient</code>
     */
    public static <T> NonBlockingClient create(EndpointService endpointService,
                                               CallbackService callbackService,
                                               Network network,
                                               ClientOptions options,
                                               NonBlockingClientListener<T>listener,
                                               T context) {
        return new NonBlockingClientImpl(endpointService, callbackService, network, options, listener, context);
    }

    /**
     * Send a message to the MQ Light server.  This is equivalent to calling:
     * <code>create(service, ClientOptions.create, listener, context);</code>
     * @see NonBlockingClient#create(String, ClientOptions, NonBlockingClientListener, Object)
     */
    public static <T> NonBlockingClient create(String service, NonBlockingClientListener<T> listener, T context) {
        return create(service, defaultClientOptions, listener, context);
    }
    
    /**
     * @return the client ID, as assigned to this instance of the client either explicitly via the
     *         {@link ClientOptions} object passed in to the {@link NonBlockingClient#create(String, NonBlockingClientListener, Object)}
     *         method, or if this value is omitted, then the client ID will be set to a probabilistically
     *         unique string by the client implementation.
     */
    public abstract String getId();
    
    /**
     * @return the service to which the client is connected (as specified on the 
     *         {@link NonBlockingClient#create(String, NonBlockingClientListener, Object)} call or
     *         <code>null</code> if the client is not, currently, connected to the MQ Light server.
     */
    public abstract String getService();
    
    /**
     * @return the current state of this client.
     */
    public abstract ClientState getState();   

    /**
     * Sends a string message to a topic.
     * @param topic the topic to send the message to.
     * @param data the string data to send to the topic.
     * @param sendOptions a set of options that determine how the send operation works.
     * @param listener a listener object that is notified when the send operation completes.  For 'at most once' quality of
     *                 service messages, this is notified (of success) when the message has been flushed to the network.
     *                 For 'at least once' quality of service messages, this is notified (of success) when receipt of the
     *                 message has been confirmed by the service.
     * @param context a context object that is passed into the listener.  This can be used within the listener code to
     *                identify the specific instance of the send method relating to the listener invocation.
     * @throws StateException if the client is in stopped or stopping state when this method is invoked.
     * @return the instance of <code>NonBlockingClient</code> that the send method was invoked upon.  This is to allow
     *         invocations of the send method to be chained.
     */
    public abstract <T> boolean send(String topic, String data, Map<String, Object> properties, SendOptions sendOptions, CompletionListener<T> listener, T context)
    throws StateException;

    /**
     * Sends a <code>ByteBuffer</code> to a topic.
     * @param topic the topic to send the message to.
     * @param data the byte buffer to send to the topic.
     * @param sendOptions a set of options that determine exactly how the send operation works.
     * @param listener a listener object that is notified when the send operation completes.  For 'at most once' quality of
     *                 service messages, this is notified (of success) when the message has been flushed to the network.
     *                 For 'at least once' quality of service messages, this is notified (of success) when receipt of the
     *                 message has been confirmed by the service.
     * @param context a context object that is passed into the listener.  This can be used within the listener code to
     *                identify the specific instance of the send method relating to the listener invocation.
     * @throws StateException if the client is in stopped or stopping state when this method is invoked.
     * @return the instance of <code>NonBlockingClient</code> that the send method was invoked upon.  This is to allow
     *         invocations of the send method to be chained.
     */
    public abstract <T> boolean send(String topic, ByteBuffer data, Map<String, Object> properties, SendOptions sendOptions, CompletionListener<T> listener, T context)
    throws StateException;
    
    /**
     * Send a message to the MQ Light server.  This is equivalent to calling:
     * <code>send(topic, data, new SendOptions(), listener, context)</code>
     * @see NonBlockingClient#send(String, String, Map, SendOptions, CompletionListener, Object)
     */
    public <T> boolean send(String topic, String data, Map<String, Object> properties, CompletionListener<T> listener, T context)
    throws StateException {
        return send(topic, data, properties, defaultSendOptions, listener, context);
    }
    
    /**
     * Send a message to the MQ Light server.  This is equivalent to calling:
     * <code>send(topic, data, new SendOptions(), listener, context)</code>
     * @see NonBlockingClient#send(String, ByteBuffer, Map, SendOptions, CompletionListener, Object)
     */
    public <T> boolean send(String topic, ByteBuffer data, Map<String, Object> properties, CompletionListener<T> listener, T context)
    throws StateException {
        return send(topic, data, properties, defaultSendOptions, listener, context);
    }
    
    /**
     * Send a message to the MQ Light server.  This is equivalent to calling:
     * <code>send(topic, data, new SendOptions(), null, null)</code>
     * @see NonBlockingClient#send(String, String, Map, SendOptions, CompletionListener, Object)
     */
    public boolean send(String topic, String data, Map<String, Object> properties) throws StateException {
        return send(topic, data, properties, defaultSendOptions, null, null);
    }
    
    /**
     * Send a message to the MQ Light server.  This is equivalent to calling:
     * <code>send(topic, data, new SendOptions(), null, null)</code>
     * @see NonBlockingClient#send(String, ByteBuffer, Map, SendOptions, CompletionListener, Object)
     */
    public boolean send(String topic, ByteBuffer data, Map<String, Object> properties) throws StateException {
        return send(topic, data, properties, defaultSendOptions, null, null);
    }
    
    /**
     * Requests that the client transitions into started state.
     * This prepares the client to send and/or receive messages from the server. As new instances of
     * <code>NonBlockingClient</code> are created in <code>starting</code> state, this method need 
     * only be called if an instance of the client has been stopped using the
     * {@link NonBlockingClient#stop(CompletionListener, Object)} method.
     * 
     * @param listener this listener will be invoked when the start operation completes.  This can
     *                 either be when the client has attained started state, or when a subsequent
     *                 call to stop results in the client attaining stopped state before it ever
     *                 achieves started state.
     * @param context a context object that is passed into the listener.  This can be used within the listener code to
     *                identify the specific instance of the start method relating to the listener invocation.
     * @return the instance of <code>NonBlockingClient</code> that the start method was invoked upon.
     */
    public abstract <T> NonBlockingClient start(CompletionListener<T> listener, T context);

    /**
     * Requests that the client transitions into stopped state, automatically unsubscribing from
     * any destinations previously subscribed to using the <code>subscribe(...)</code> methods.
     * 
     * TODO: we've removed the drain parameter - merge the relevent parts of its description
     *       into the method description.
     *  drain determines whether the client should flush any buffered messages before
     *              completing its transition into stopped state, and calling the
     *              <code>CompletionListener</code>.  When this value is set to
     *              <code>true</code> any messages that are held pending transmission over the
     *              network, or pending delivery to the <code>DestinationListener</code>
     *              will be flushed before the client completes its transition into stopped
     *              state. When set to <code>false</code> the client will transition into stopped state
     *              without draining any messages which it is buffering.  Instead, these messages
     *              will be discarded, resulting in the potential for loss of 'at most once'
     *              messages and duplication of 'at least once' messages.  If this method is
     *              called with drain set to <code>true</code> it is possible to invoke the
     *              method again with drain set to <code>false</code> and interrupt the process
     *              of flushing buffered messages, causing both calls to the method to complete
     *              and any messages that are still buffered to be discarded.
     * @param listener a listener that is notified when the stop operation has completed and
     *                 the client has attained stopped state.
     * @param context a context object that is passed into the listener.  This can be used within the listener code to
     *                identify the specific instance of the stop method relating to the listener invocation.
     */
    public abstract <T> void stop(CompletionListener<T> listener, T context);

    /**
     * Subscribes the client to a destination, based on the supplied topic pattern and
     * share parameters.  The <code>topicPattern</code> parameter is matched against the 
     * topic that messages are sent to, allowing the messaging service to determine whether a
     * particular message will be delivered to a particular destination, and hence the subscribing client.
     * @param topicPattern the topic pattern to subscribe to.  This determines which messages will be
     *                     held at the subscribed to destination pending delivery to this client.
     * @param subOptions a set of options that control the behaviour of the destination subscribed to
     *                   and influence how this client receives messages from the destination.
     * @param destListener a listener that is notified when messages arrive at the client and also 
     *                      when the destination is unsubscribed from using the 
     *                      {@link NonBlockingClient#unsubscribe(String, String, int, CompletionListener, Object)} method.
     * @param compListener a listener that is notified when the subscribe operation completes.  If a
     *                     value of <code>null</code> is specified then no-one is notified.
     * @param context a context object that is passed into the listeners registered using this method.
     *                The object supplied can be used within the listener code to identify the specific
     *                instance of the subscribe method relating to the listener invocation.
     * @throws StateException if the client is in stopped or stopping state when this method is invoked, or if
     *                        the client is already subscribed to the destination identified by a combination
     *                        of the topic pattern and share options.
     * @throws IllegalArgumentException if one of the values supplied via the <code>subOptions</code> parameter
     *                                  is not valid.  Also thrown if a <code>null</code> <code>destListener</code>
     *                                  parameter is specified.
     * @return the instance of <code>NonBlockingClient</code> that the unsubscribe method was invoked upon.  This is to
     *         allow invocations of the unsubscribe method to be chained.
     */
    public abstract <T> NonBlockingClient subscribe(String topicPattern, SubscribeOptions subOptions, 
                                                    DestinationListener<T> destListener, CompletionListener<T> compListener, T context)
    throws StateException, IllegalArgumentException;
    
    /**
     * Subscribes to a destination.  This is equivalent to calling:
     * <code>subscribe(topicPattern, new SubscribeOptions(), listener, context)</code>
     * @see NonBlockingClient#subscribe(String, SubscribeOptions, DestinationListener, CompletionListener, Object)
     */
    public <T> NonBlockingClient subscribe(String topicPattern, DestinationListener<T> dstListener, CompletionListener<T> compListener, T context)
    throws StateException {
        return subscribe(topicPattern, defaultSubscribeOptions, dstListener, compListener, context);
    }

    /**
     * Unsubscribes from a destination.  Once complete, this stops messages received by the destination
     * from being sent to this client.
     * @param topicPattern a topic pattern that identifies the destination to unsubscribe from.  This must match
     *                     one of the topic patterns previously subscribed to using the 
     *                     {@link NonBlockingClient#subscribe(String, SubscribeOptions, DestinationListener, CompletionListener, Object)}
     *                     method.
     * @param share a share name that identifies the destination to unsubscribe from.  This must match the
     *              share option specified when the corresponding topic pattern was subscribed to.  If a value
     *              of <code>null</code> is supplied for this parameter then the client will attempt to
     *              unsubscribe from a private destination.
     * @param ttl the new time-to-live value to assign to the destination.  Currently the only supported value for this
     *            parameter is zero.
     * @param listener a listener that is notified when the unsubscribe operation has completed.  Invocation of this
     *                 listener is deferred until any messages, buffered pending delivery to a <code>DestiantionListener</code>
     *                 registered with the client, have been delivered.  If a value of <code>null</code> is supplied for this
     *                 parameter then no notification will be generated.
     * @param context a context object that is passed into the listener.  This can be used within the listener code to
     *                identify the specific instance of the stop method relating to the listener invocation.
     * @throws StateException if the client is in stopped or stopping state when this method is invoked, or an attempt is made
     *                        to unsubscribe from a destination that the client is not currently subscribed to.
     * @throws IllegalArgumentException if a non-zero <code>ttl</code> parameter is specified.
     * @return the instance of <code>NonBlockingClient</code> that the unsubscribe method was invoked upon.  This is to
     *         allow invocations of the unsubscribe method to be chained.
     */
    public abstract <T> NonBlockingClient unsubscribe(String topicPattern, String share, int ttl, CompletionListener<T> listener, T context)
    throws StateException, IllegalArgumentException;
    
    /**
     * Unsubscribes from a destination.  This is equivalent to calling:
     * <code>unsubscribe(topicPattern, null, ttl)</code>
     * @see NonBlockingClient#unsubscribe(String, String, int, CompletionListener, Object)
     */
    public <T> NonBlockingClient unsubscribe(String topicPattern, int ttl, CompletionListener<T> listener, T context)
    throws StateException, IllegalArgumentException {
        return unsubscribe(topicPattern, null, ttl, listener, context);
    }
    
    /**
     * Unsubscribes from a destination.  This is equivalent to calling the 
     * {@link NonBlockingClient#unsubscribe(String, String, int, CompletionListener, Object)} method without
     * specifying a ttl (time-to-live) value.  This has the effect of unsubscribing from the destination
     * without changing the ttl value currently assigned to the destination.
     */
    public abstract <T> NonBlockingClient unsubscribe(String topicPattern, String share, CompletionListener<T> listener, T context)
    throws StateException;
    
    /**
     * Unsubscribes from a destination.  This is equivalent to calling:
     * <code>unsubscribe(topicPattern, null, listerner, context)</code>
     * @see NonBlockingClient#unsubscribe(String, String, CompletionListener, Object)
     */
    public <T> NonBlockingClient unsubscribe(String topicPattern, CompletionListener<T> listener, T context) 
    throws StateException {
        return unsubscribe(topicPattern, null, listener, context);
    }
}