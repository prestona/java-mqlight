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

import java.util.regex.Pattern;

/**
 * A builder for instances of {@link NonBlockingClient}. This builder is
 * intended for use inside the IBM Bluemix environment. It uses the values from
 * the <code>VCAP_SERVICES</code> environment variable to configure the client
 * that it builds.
 * <p>
 * The {@link NonBlockingClientBuilder} can be used to build clients for use
 * outside of the IBM Bluemix environment.
 */
public interface BluemixNonBlockingClientBuilder {

    /**
     * Specifies the user supplied name of a service to locate in the
     * <code>VCAP_SERVICES</code> configuration. If this is not set then
     * any name will match (which is probably the behaviour you want unless
     * you have bound your application to multiple instances of the
     * MQ Light service).
     * @param pattern
     *            a pattern to match against the <code>name</code> property of
     *            the service entry in <code>VCAP_SERVICES</code>.
     * @return the same instance of {@code BluemixNonBlockingClientBuilder} that
     *         this method was invoked on.
     */
    public BluemixNonBlockingClientBuilder serviceName(Pattern pattern);

    /**
     * Specifies an exact user supplied name for a service to locate in the
     * <code>VCAP_SERVICES</code> configuration. This is identical to calling:
     * <code>serviceName(Pattern.quote(name));</code>.
     * @param name
     *            an exact value to match against the <code>name</code> property
     *            of the service entry in VCAP_SERVICES.
     * @return the same instance of {@code BluemixNonBlockingClientBuilder} that
     *         this method was invoked on.
     */
    public BluemixNonBlockingClientBuilder serviceName(String name);

    /**
     * Specifies the label of a service to locate in the
     * <code>VCAP_SERVICES</code> configuration. If this is not set then
     * labels that contain any of the following strings will match (this is
     * probably the behaviour that you want):
     * <ul>
     * <li>mqlight
     * <li>messagehub
     * <li>user-provided
     * </ul>
     * @param pattern
     *            a pattern to match against the <code>label</code> property of
     *            the service entry in <code>VCAP_SERVICES</code>
     * @return the same instance of {@code BluemixNonBlockingClientBuilder} that
     *         this method was invoked on.
     */
    public BluemixNonBlockingClientBuilder serviceLabel(Pattern pattern);

    /**
     * Specifies the exact label for a service to locate in the
     * <code>VCAP_SERVICES</code> configuration. This is identical to calling:
     * <code>serviceLabel(Pattern.quote(name));</code>.
     * @param name
     *            an exact value to match against the <code>label</code>
     *            property of the service entry in VCAP_SERVICES.
     * @return the same instance of {@code BluemixNonBlockingClientBuilder} that
     *         this method was invoked on.
     */
    public BluemixNonBlockingClientBuilder serviceLabel(String name);

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
     * @return the same instance of {@code BluemixNonBlockingClientBuilder} that
     *         this method was invoked on.
     * @throws IllegalArgumentException if an invalid <code>id</code> value is
     *         specified.
     */
    public BluemixNonBlockingClientBuilder id(String id)
            throws IllegalArgumentException;

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
