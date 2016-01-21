/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.ibm.mqlight.api;

import java.io.File;

/**
 * A builder for instances of {@link NonBlockingClient}. This builder is
 * intended for use outside of the IBM Bluemix environment. The
 * {@link BluemixNonBlockingClientBuilder} can be used to build clients that
 * automatically discover their configuration from the Bluemix environment.
 */
public interface NonBlockingClientBuilder {

    /**
     * Specifies a X.509 certificate chain file for SSL/TLS certificates
     * that the client will trust. This can either be a file in PEM format
     * or a Java KeyStore (JKS) file.
     *
     * @param certificateFile
     *            a trust store that contains SSL/TLS certificates that the
     *            client is to trust. If this is not set (or is set to null)
     *            then the client will use the set of trusted certificates
     *            supplied with the JVM.
     * @return the same instance of {@link NonBlockingClientBuilder} that
     *         this method was invoked on.
     */
    public NonBlockingClientBuilder trustCertificate(File certificateFile);

    /**
     * Determines whether the client validates that the CN name of the
     * server's certificate matches its DNS name.
     * @param verifyName
     *            should the client validate the server's CN name? If this
     *            method is not called, the default is to behave as if this
     *            method was called with a value of <code>true</code>.
     * @return the same instance of {@link NonBlockingClientBuilder} that
     *         this method was invoked on.
     */
    public NonBlockingClientBuilder verifyHostName(boolean verifyName);

    public NonBlockingClientBuilder keyStore(File keyStoreFile);

    public NonBlockingClientBuilder keyStorePassphrase(String passphrase);

    public NonBlockingClientBuilder clientCertificate(File clientCertificateFile);

    public NonBlockingClientBuilder clientKey(File clientKeyFile);

    public NonBlockingClientBuilder clientKeyPassphrase(String passphrase);

    /**
     * A URI for the service to connect to. For example:
     * <code>amqp://example.org:5672</code>. This URI can start with either
     * <code>amqp://</code> or <code>amqps://</code> (for SSL/TLS based
     * connections).  User names and passwords may be embedded into the URL
     * - for example: <code>amqp://user:pass@example.com</code>.
     * @param service
     *            the URI of the service to connect to.
     * @return the same instance of {@link NonBlockingClientBuilder} that
     *         this method was invoked on.
     */
    public NonBlockingClientBuilder service(String service);

    /**
     * Sets the credentials, that will be associated with the
     * {@link NonBlockingClient} that will be built. If these values are
     * not set (or both values are set to <code>null</code> then the client
     * will attempt to use the SASL ANNONYMOUS mechanism when it connects to
     * the MQ Light server.
     * @param username
     *            the user name that the client will identify itself using.
     * @param password
     *            the password that the client will use to authenticate
     *            itself.
     * @return the same instance of {@link NonBlockingClientBuilder} that
     *         this method was invoked on.
     */
    public NonBlockingClientBuilder credentials(String username, String password);

    /**
     * Sets a client identifier, that will be associated with the
     * {@code NonBlockingClient}.
     * @param id
     *            a unique identifier for this client. If this is not set then
     *            the default is the string "AUTO_" followed by a randomly
     *            chosen 7 digit hex value (with hex characters lower case). A
     *            maximum of one instance of the client (as identified by the
     *            value of this parameter) can be connected the an MQ Light
     *            server at a given point in time.  If another instance of the
     *            same client connects, then the previously connected instance
     *            will be disconnected. This is reported, to the first client,
     *            as a ReplacedException, and the client transitioning into
     *            stopped state. When set, the id must be a minimum of 1
     *            character and a maximum of 256 characters in length. The id
     *            can only contain alphanumeric characters, and any of the
     *            following characters: percent sign (%), slash (/), period
     *            (.), underscore (_).
     * @return the same instance of {@link NonBlockingClientBuilder} that
     *         this method was invoked on.
     * @throws IllegalArgumentException if an invalid <code>id</code> value is
     *         specified.
     */
    public NonBlockingClientBuilder id(String id) throws IllegalArgumentException;

    /**
     * Builds an instance of the {@link NonBlockingClient}. This client is
     * configured using the values associated with this builder object.
     * @param listener
     *            a listener that is notified of major life-cycle events for the
     *            client.
     * @param context
     *            a context object that is passed into the listener.  This can
     *            be used within the listener code to identify the specific
     *            instance of the create method relating to the listener
     *            invocation.
     * @return a new instance of {@link NonBlockingClient}.
     * @throws IllegalArgumentException thrown if one or more of the values
     *         specified to this builder object is not valid.
     */
    public <C> NonBlockingClient build(NonBlockingClientListener<C>listener, C context)
            throws IllegalArgumentException;

    /**
     * Builds an instance of the {@link NonBlockingClient}. This is equivalent
     * to calling <code>build(listener, null);</code>.
     * @param listener
     *            a listener that is notified of major life-cycle events for the
     *            client.
     * @return a new instance of {@link NonBlockingClient}.
     * @throws IllegalArgumentException thrown if one or more of the values
     *         specified to this builder object is not valid.
     */
    public NonBlockingClient build(NonBlockingClientListener<?>listener)
            throws IllegalArgumentException;
}
