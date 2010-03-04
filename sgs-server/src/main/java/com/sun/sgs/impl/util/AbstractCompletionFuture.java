/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html 
 *
 * Copyright (c) 2007-2010, Oracle and/or its affiliates.
 *
 * All rights reserved.
 */

package com.sun.sgs.impl.util;

import static com.sun.sgs.impl.sharedutil.Objects.checkNull;
import com.sun.sgs.protocol.RequestCompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This future is an abstract implementation for the futures
 * passed to the {@code RequestCompletionHandler} used by the
 * {@code ProtocolListener} and {@code SessionProtocolHandler}
 * APIs.
 *
 * @param <T> the future's result type
 */
public abstract class AbstractCompletionFuture<T>
    implements Future<T>
{
    /** The completion handler for the associated request. */
    private final RequestCompletionHandler<T> completionHandler;

    /**
     * Indicates whether the operation associated with this future
     * is complete.
     */
    private boolean done = false;
    
    /** Lock for accessing the {@code done} field. */
    private final Object lock = new Object();
    
    /** An exception cause, or {@code null}. */
    private volatile Throwable exceptionCause = null;
    
    /**
     * Constructs an instance with the specified {@code completionHandler}.
     *
     * @param completionHandler a completion handler
     */
    protected AbstractCompletionFuture(
	RequestCompletionHandler<T> completionHandler)
    {
	checkNull("completionHandler", completionHandler);
	this.completionHandler = completionHandler;
    }

    /**
     * Returns the value associated with this future.
     *
     * @return	the value for this future
     */
    protected abstract T getValue();
	    
    /**
     * Returns the value associated with this future, or throws
     * {@code ExecutionException} if there is a problem
     * processing the operation associated with this future.
     *
     * @return	the value for this future
     * @throws	ExecutionException if there is a problem processing
     *		the operation associated with this future
     */
    private T getValueInternal() throws ExecutionException {
	if (exceptionCause != null) {
	    throw new ExecutionException(exceptionCause);
	} else {
	    return getValue();
	}
    }

    /** {@inheritDoc} */
    public boolean cancel(boolean mayInterruptIfRunning) {
	return false;
    }

    /** {@inheritDoc} */
    public T get() throws InterruptedException, ExecutionException {
	synchronized (lock) {
	    while (!done) {
		lock.wait();
	    }
	}
	return getValueInternal();
    }

    /** {@inheritDoc} */
    public T get(long timeout, TimeUnit unit)
	throws InterruptedException, ExecutionException, TimeoutException
    {
	synchronized (lock) {
	    if (!done) {
		unit.timedWait(lock, timeout);
	    }
	    if (!done) {
		throw new TimeoutException();
	    }
	    return getValueInternal();
	}
    }

    /** {@inheritDoc} */
    public boolean isCancelled() {
	return false;
    }

    /**
     * Sets the exception cause for this future to the specified
     * {@code throwable}.  The given exception will be used as
     * the cause of the {@code ExecutionException} thrown by
     * this future's {@code get} methods.
     *
     * @param	throwable an exception cause
     */
    protected void setException(Throwable throwable) {
	checkNull("throwable", throwable);
	exceptionCause = throwable;
	done();
    }

    /** {@inheritDoc} */
    public boolean isDone() {
	synchronized (lock) {
	    return done;
	}
    }

    /**
     * Indicates that the operation associated with this future is
     * complete and notifies the associated completion
     * handler. Subsequent invocations to {@link #isDone isDone}
     * will return {@code true}.
     *
     * @throws	IllegalStateException if this method has already been
     *		invoked 
     */
    protected void done() {
	synchronized (lock) {
	    if (done) {
		throw new IllegalStateException("already completed");
	    }
	    done = true;
	    lock.notifyAll();
	}	
	completionHandler.completed(this);
    }
}
