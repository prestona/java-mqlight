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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;

import junit.framework.AssertionFailedError;
import static org.junit.Assert.*;

import org.junit.Test;

import com.ibm.mqlight.api.network.NetworkWriteFuture;

public class TestNettyNetwork {

    private class BaseListener implements Runnable {
        
        private final int port;
        private final Thread thread;
        
        protected BaseListener(int port) throws IOException, InterruptedException {
            this.port = port;
            thread = new Thread(this);
            thread.setDaemon(true);
            synchronized(this) {
                thread.start();
                this.wait();
            }
        }
        
        protected boolean join(long timeout) throws InterruptedException {
            thread.join(timeout);
            return !thread.isAlive();
        }
        
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                synchronized(this) {
                    this.notifyAll();
                }
                Socket socket = serverSocket.accept();
                processSocket(socket);
                socket.close();
                serverSocket.close();
            } catch(IOException ioException) {
                ioException.printStackTrace();
                synchronized(this) {
                    this.notifyAll();
                }
            }
        }
        
        protected void processSocket(Socket socket) throws IOException {
        }
    }
    
    private class ReceiveListener extends BaseListener {

        private byte[] buffer = new byte[1024 * 1024];
        private int bytesRead;
        
        protected ReceiveListener(int port) throws IOException, InterruptedException {
            super(port);
        }
        
        @Override
        protected void processSocket(Socket socket) throws IOException {
            InputStream in = socket.getInputStream();
            int count = 0;
            while(true) {
                int n = in.read(buffer);
                if (n < 0) break;
                count += n;
            }
            synchronized(this) {
                bytesRead = count;
            }
        }
        
        protected synchronized int getBytesRead() {
            return bytesRead;
        }
    }
    
    private class SendListener extends BaseListener {

        private int bytesWritten;
        
        protected SendListener(int port) throws IOException, InterruptedException {
            super(port);
        }
        
        @Override
        protected void processSocket(Socket socket) throws IOException {
            OutputStream out = socket.getOutputStream();
            int count = 0;
            byte[] data = new byte[(1 << 24)];
            Arrays.fill(data, (byte)123);
            for (int i = 0; i < 25; ++i) {
                out.write(data, 0, 1 << i);
                count += 1 << i;
            }
            synchronized(this) {
                bytesWritten = count;
            }
        }
        
        protected synchronized int getBytesWritten() {
            return bytesWritten;
        }
    }
    
    @Test
    public void connectClose() throws Exception {
        NettyNetwork nn = new NettyNetwork();
        BaseListener testListener = new BaseListener(34567);
        
        LinkedList<Event> events = new LinkedList<>();
        MockNetworkListener listener = new MockNetworkListener(events);
        MockNetworkConnectFuture future = new MockNetworkConnectFuture(events);
        nn.connect("localhost", 34567, listener, future);
        
        while(!future.isDone()) {
            Thread.sleep(50);
        }
        assertTrue("Expected future to be marked done", future.isDone());
        assertTrue("Expected listener to end!", testListener.join(2500));
        assertEquals("Expected two events!", 2, events.size());
        assertEquals("Expected first event to be a connect success", Event.Type.CONNECT_SUCCESS, events.get(0).type);
        assertEquals("Expected second event to be a close", Event.Type.CHANNEL_CLOSE, events.get(1).type);
    }
    
    @Test
    public void writeData() throws Exception {
        NettyNetwork nn = new NettyNetwork();
        ReceiveListener testListener = new ReceiveListener(34567);
        
        LinkedList<Event> events = new LinkedList<>();
        MockNetworkListener listener = new MockNetworkListener(events);
        MockNetworkConnectFuture future = new MockNetworkConnectFuture(events);
        nn.connect("localhost", 34567, listener, future);
        
        for (int i = 0 ; i < 20; ++i) {
            if (future.isDone()) break;
            Thread.sleep(50);
        }
        assertTrue("Expected connect future to be marked done", future.isDone());
        assertNotNull("Expected connect future to contain a channel", future.getChannel());
        
        byte[] data = new byte[(1 << 24)];
        Arrays.fill(data, (byte)123);
        int expectedBytes = 0;
        NetworkWriteFuture[] futures = new NetworkWriteFuture[25];
        for (int i = 0; i < 25; ++i) {
            ByteBuffer buffer = ByteBuffer.wrap(data, 0, 1 << i);
            futures[i] = new MockNetworkWriteFuture();
            future.getChannel().write(buffer, futures[i]);
            expectedBytes += (1 << i);
        }
        
        boolean allWritesComplete = false;
        for (int j = 0; j < 100; ++j) {
            allWritesComplete = true;
            for (int i = 0; i < futures.length; ++i) {
                if (!futures[i].isDone()) {
                    allWritesComplete = false;
                    break;
                }
            }
            if (allWritesComplete) {
                break;
            }
            Thread.sleep(50);
        }
        
        if (!allWritesComplete) {
            StringBuilder sb = new StringBuilder("Expected all write futures to be completed:\n");
            for (int i = 0; i < futures.length; ++i) {
                sb.append("Future #" + i +" is " + (futures[i].isDone() ? "completed" : "not completed!\n"));
            }
            throw new AssertionFailedError(sb.toString());
        }
        
        MockNetworkCloseFuture closeFuture = new MockNetworkCloseFuture();
        future.getChannel().close(closeFuture);
        for (int i = 0 ; i < 20; ++i) {
            if (closeFuture.isDone()) break;
            Thread.sleep(50);
        }
        assertTrue("Expected close future to be marked done", closeFuture.isDone());
        assertTrue("Expected listener to end!", testListener.join(2500));
        
        assertEquals("Expected to have received same amount of data as was sent", expectedBytes, testListener.getBytesRead());
    }
    
    @Test
    public void readData() throws IOException, InterruptedException {
        NettyNetwork nn = new NettyNetwork();
        SendListener testListener = new SendListener(34567);
        
        LinkedList<Event> events = new LinkedList<>();
        MockNetworkListener listener = new MockNetworkListener(events);
        MockNetworkConnectFuture connectFuture = new MockNetworkConnectFuture(events);
        nn.connect("localhost", 34567, listener, connectFuture);
        
        for (int i = 0 ; i < 20; ++i) {
            if (connectFuture.isDone()) break;
            Thread.sleep(50);
        }
        assertTrue("Expected connect future to be marked done", connectFuture.isDone());
        assertNotNull("Expected connect future to contain a channel", connectFuture.getChannel());
        
        assertTrue("Expected listener to end!", testListener.join(2500));
        
        for (int i = 0; i < 20; ++i) {
            synchronized(events) {
                if ((events.size() > 1) && events.getLast().type == Event.Type.CHANNEL_CLOSE) {
                    break;
                }
            }
            Thread.sleep(50);
        }
        assertTrue("Expected at least three events...", events.size() > 3);
        assertEquals("Expected first event to be a connect success", Event.Type.CONNECT_SUCCESS, events.removeFirst().type);
        assertEquals("Expected last event to be a channel close", Event.Type.CHANNEL_CLOSE, events.removeLast().type);
        
        int amount = 0;
        while(!events.isEmpty()) {
            Event event = events.removeFirst();
            assertEquals("Expected channel read event", Event.Type.CHANNEL_READ, event.type);
            amount += ((ByteBuffer)event.context).remaining();
        }
        assertEquals("Didn't receive the same amount of data as was written", testListener.getBytesWritten(), amount);
    }
}