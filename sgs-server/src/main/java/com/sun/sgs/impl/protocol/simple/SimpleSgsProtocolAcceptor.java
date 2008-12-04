/*
 * Copyright 2007-2008 Sun Microsystems, Inc.
 *
 * This file is part of Project Darkstar Server.
 *
 * Project Darkstar Server is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation and
 * distributed hereunder to you.
 *
 * Project Darkstar Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sun.sgs.impl.protocol.simple;

import com.sun.sgs.auth.Identity;
import com.sun.sgs.auth.IdentityCoordinator;
import com.sun.sgs.impl.auth.NamePasswordCredentials;
import com.sun.sgs.impl.kernel.StandardProperties;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.sharedutil.PropertiesWrapper;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.nio.channels.AsynchronousChannelGroup;
import com.sun.sgs.nio.channels.AsynchronousServerSocketChannel;
import com.sun.sgs.nio.channels.AsynchronousSocketChannel;
import com.sun.sgs.nio.channels.CompletionHandler;
import com.sun.sgs.nio.channels.IoFuture;
import com.sun.sgs.nio.channels.spi.AsynchronousChannelProvider;
import com.sun.sgs.protocol.ProtocolAcceptor;
import com.sun.sgs.protocol.ProtocolListener;
import com.sun.sgs.protocol.SessionProtocol;
import com.sun.sgs.protocol.SessionProtocolHandler;
import com.sun.sgs.protocol.simple.SimpleSgsProtocol;
import com.sun.sgs.service.TransactionProxy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;

/**
 * A protocol acceptor for connections that speak the {@link SimpleSgsProtocol}.
 */
public class SimpleSgsProtocolAcceptor
    extends AbstractService
    implements ProtocolAcceptor
{
    /** The package name. */
    public static final String PKG_NAME = "com.sun.sgs.impl.protocol.simple";
    
    /** The logger for this class. */
    private static final LoggerWrapper logger =
	new LoggerWrapper(Logger.getLogger(PKG_NAME + "acceptor"));

    /**
     * The server listen address property.
     * This is the host interface we are listening on. Default is listen
     * on all interfaces.
     */
    private static final String LISTEN_HOST_PROPERTY =
        PKG_NAME + ".listen.address";
    
    /** The name of the acceptor backlog property. */
    private static final String ACCEPTOR_BACKLOG_PROPERTY =
        PKG_NAME + ".acceptor.backlog";

    /** The default acceptor backlog (&lt;= 0 means default). */
    private static final int DEFAULT_ACCEPTOR_BACKLOG = 0;

    /** The name of the read buffer size property. */
    private static final String READ_BUFFER_SIZE_PROPERTY =
        PKG_NAME + ".buffer.read.max";

    /** The default read buffer size: {@value #DEFAULT_READ_BUFFER_SIZE} */
    private static final int DEFAULT_READ_BUFFER_SIZE = 128 * 1024;
    
    /** The identity manager. */
    private final IdentityCoordinator identityManager;
    
    /** The port for accepting connections. */
    private final int appPort;

    /** The read buffer size for new connections. */
    private final int readBufferSize;

    /** The protocol listener. */
    private final ProtocolListener protocolListener;

    /** The async channel group for this service. */
    private final AsynchronousChannelGroup asyncChannelGroup;
    
    /** The acceptor for listening for new connections. */
    private final AsynchronousServerSocketChannel acceptor;
    
    /** The currently-active accept operation, or {@code null} if none. */
    volatile IoFuture<?, ?> acceptFuture;
    
    private final AcceptorListener acceptorListener =
	new AcceptorListener();
    
    /**
     * Constructs an instance with the specified {@code properties},
     * {@code systemRegistry}, {@code txnProxy}, and {@code protocolListener}.
     *
     * @param	properties the configuration properties
     * @param	systemRegistry the system registry
     * @param	txnProxy a transaction proxy
     * @param	protocolListener a protocol listener
     *
     * @throws	Exception if a problem occurs
     */
    public SimpleSgsProtocolAcceptor(Properties properties,
				     ComponentRegistry systemRegistry,
				     TransactionProxy txnProxy,
				     ProtocolListener protocolListener)
	throws Exception
    {
	super(properties, systemRegistry, txnProxy, logger);
	
	logger.log(Level.CONFIG,
		   "Creating SimpleSgsProtcolAcceptor properties:{0}",
		   properties);

	if (protocolListener == null) {
	    throw new NullPointerException("null protocolListener");
	}
	this.protocolListener = protocolListener;
	
	PropertiesWrapper wrappedProps = new PropertiesWrapper(properties);
	try {
            String hostAddress =
		properties.getProperty(LISTEN_HOST_PROPERTY);
            appPort = wrappedProps.getRequiredIntProperty(
                StandardProperties.APP_PORT, 1, 65535);
	    int acceptorBacklog = wrappedProps.getIntProperty(
	                ACCEPTOR_BACKLOG_PROPERTY, DEFAULT_ACCEPTOR_BACKLOG);
            readBufferSize = wrappedProps.getIntProperty(
                READ_BUFFER_SIZE_PROPERTY, DEFAULT_READ_BUFFER_SIZE,
                8192, Integer.MAX_VALUE);
	    identityManager =
		systemRegistry.getComponent(IdentityCoordinator.class);
	    
	    InetSocketAddress listenAddress =
                hostAddress == null ?
		new InetSocketAddress(appPort) :
		new InetSocketAddress(hostAddress, appPort);
	    AsynchronousChannelProvider provider =
		// TODO fetch from config
		AsynchronousChannelProvider.provider();
	    asyncChannelGroup =
		// TODO fetch from config
		provider.openAsynchronousChannelGroup(
 		    Executors.newCachedThreadPool());
	    acceptor =
		provider.openAsynchronousServerSocketChannel(asyncChannelGroup);
	    try {
		acceptor.bind(listenAddress, acceptorBacklog);
		if (logger.isLoggable(Level.CONFIG)) {
		    logger.log(
		        Level.CONFIG, "bound to port:{0,number,#}",
			getListenPort());
		}
	    } catch (Exception e) {
		logger.logThrow(Level.WARNING, e,
				"acceptor failed to listen on {0}",
				listenAddress);
		try {
		    acceptor.close();
                } catch (IOException ioe) {
                    logger.logThrow(Level.WARNING, ioe,
                        "problem closing acceptor");
                }
		throw e;
	    }
	    
	    // TBD: check service version?
	    
	} catch (Exception e) {
	    if (logger.isLoggable(Level.CONFIG)) {
		logger.logThrow(
		    Level.CONFIG, e,
		    "Failed to create SimpleSgsProtcolAcceptor");
	    }
	    throw e;
	}
    }

    /* -- Implement AbstractService -- */
    
    /** {@inheritDoc} */
    protected void handleServiceVersionMismatch(
	Version oldVersion, Version currentVersion)
    {
	throw new IllegalStateException(
	    "unable to convert version:" + oldVersion +
	    " to current version:" + currentVersion);
    }
    
    /** {@inheritDoc} */
    public void doReady() {
	accept();
    }
    
    /** {@inheritDoc} */
    public void doShutdown() {
	final IoFuture<?, ?> future = acceptFuture;
	acceptFuture = null;
	if (future != null) {
	    future.cancel(true);
	}
	
	if (acceptor != null) {
	    try {
		acceptor.close();
	    } catch (IOException e) {
		logger.logThrow(Level.FINEST, e, "closing acceptor throws");
		// swallow exception
	    } 
	}
	
	if (asyncChannelGroup != null) {
	    asyncChannelGroup.shutdown();
	    boolean groupShutdownCompleted = false;
	    try {
		groupShutdownCompleted =
		    asyncChannelGroup.awaitTermination(1, TimeUnit.SECONDS);
	    } catch (InterruptedException e) {
		logger.logThrow(Level.FINEST, e,
				"shutdown acceptor interrupted");
		Thread.currentThread().interrupt();
	    }
	    if (!groupShutdownCompleted) {
		logger.log(Level.WARNING, "forcing async group shutdown");
		try {
		    asyncChannelGroup.shutdownNow();
		} catch (IOException e) {
		    logger.logThrow(Level.FINEST, e,
				    "shutdown acceptor throws");
		    // swallow exception
		}
	    }
	}
	logger.log(Level.FINEST, "acceptor shutdown");
    }

    /* -- Public methods -- */

    /**
     * Returns the port this service is listening on for incoming
     * client session connections.
     *
     * @return the port this service is listening on
     * @throws IOException if an IO problem occurs
     */
    public int getListenPort() throws IOException {
        return ((InetSocketAddress) acceptor.getLocalAddress()).getPort();
    }
    
    /* -- Package access methods -- */

    /**
     * Returns the authenticated identity for the specified {@code name} and
     * {@code password}.
     *
     * @param	name a name
     * @param	password a password
     * @return	the authenticated identity
     * @throws	LoginException if a problem occurs authenticating the name and
     *		password 
     */
    Identity authenticate(String name, String password) throws LoginException {
	return identityManager.authenticateIdentity(
	    new NamePasswordCredentials(name, password.toCharArray()));
    }
    
    /**
     * Schedules a non-durable, non-transactional {@code task}.
     *
     * @param	task a non-durable, non-transactional task
     */
    void scheduleNonTransactionalTask(KernelRunnable task) {
        taskScheduler.scheduleTask(task, taskOwner);
    }
    
    /* -- Private methods and classes -- */

    /**
     * Asynchronously accepts the next connection.
     */
    private void accept() {
	    
	acceptFuture = acceptor.accept(acceptorListener);
	SocketAddress addr = null;
	try {
	    addr = acceptor.getLocalAddress();
	} catch (IOException ioe) {
	    // ignore
	}
	
	if (logger.isLoggable(Level.CONFIG)) {
	    logger.log(Level.CONFIG, "listening on {0}", addr);
	}
    }

    /** A completion handler for accepting connections. */
    private class AcceptorListener
	implements CompletionHandler<AsynchronousSocketChannel, Void>
    {
	/** Handle new connection or report failure. */
	public void completed(
			      IoFuture<AsynchronousSocketChannel, Void> result)
	{
	    try {
		try {
		    AsynchronousSocketChannel socketChannel =
			result.getNow();
		    logger.log(Level.FINER, "Accepted {0}", socketChannel);
		    
		    /*
		     * The protocol will call the ProtocolListener's
		     * newConnection method if the authentication succeeds.
		     */
		    new SimpleSgsProtocolImpl(
			protocolListener,
			SimpleSgsProtocolAcceptor.this,
			socketChannel, readBufferSize);
		    
		    // Resume accepting connections
		    accept();
		    
		} catch (ExecutionException e) {
		    throw (e.getCause() == null) ? e : e.getCause();
		}
	    } catch (CancellationException e) {               
		logger.logThrow(Level.FINE, e, "acceptor cancelled"); 
		//ignore
	    } catch (Throwable e) {
		SocketAddress addr = null;
		try {
		    addr = acceptor.getLocalAddress();
		} catch (IOException ioe) {
		    // ignore
		}
		
		logger.logThrow(
				Level.SEVERE, e, "acceptor error on {0}", addr);
		
		// TBD: take other actions, such as restarting acceptor?
	    }
	}
    }
}
