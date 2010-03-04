/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html 
 *
 * Copyright (c) 2007-2010, Oracle and/or its affiliates.
 *
 * All rights reserved.
 */

package com.sun.sgs.impl.auth;

import com.sun.sgs.auth.Identity;

import java.io.Serializable;


/**
 * This is a basic implementation of <code>Identity</code> that maps a name
 * to the identity.
 */
public class IdentityImpl implements Identity, Serializable
{
    private static final long serialVersionUID = 1L;

    // the name of this identity
    private final String name;

    /**
     * Creates an instance of <code>Identity</code> associated with the
     * given name.
     *
     * @param name the name of this identity
     */
    public IdentityImpl(String name) {
        if (name == null) {
            throw new NullPointerException("Null names are not allowed");
        }

        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public void notifyLoggedIn() {

    }

    /**
     * {@inheritDoc}
     */
    public void notifyLoggedOut() {

    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if ((o == null) || (!(o instanceof IdentityImpl))) {
            return false;
        }
        return ((IdentityImpl) o).name.equals(name);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
	return getClass().getName() + "[" + name + "]";
    }
}
