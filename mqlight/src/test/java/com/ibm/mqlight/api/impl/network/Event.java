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

package com.ibm.mqlight.api.impl.network;

public class Event {

    enum Type {
        CONNECT_SUCCESS,
        CONNECT_FAILURE,
        CHANNEL_READ,
        CHANNEL_CLOSE,
        CHANNEL_ERROR
    }
    
    public final Type type;
    public final Object context;
    
    public Event (Type type, Object context) {
        this.type = type;
        this.context = context;
    }
}