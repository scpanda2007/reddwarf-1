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

import com.sun.sgs.auth.IdentityCredentials;


/**
 * This simple implementation of <code>IdentityCredentials</code> is used to
 * represent a name and password pair.
 */
public class NamePasswordCredentials implements IdentityCredentials
{

    /**
     * The identifier for this type of credentials.
     */
    public static final String TYPE_IDENTIFIER = "NameAndPasswordCredentials";

    // the name and password
    private final String name;
    private final char [] password;

    /**
     * Creates an instance of <code>NamePasswordCredentials</code>.
     *
     * @param name the name
     * @param password the password
     */
    public NamePasswordCredentials(String name, char [] password) {
        this.name = name;
        this.password = password.clone();
    }

    /**
     * {@inheritDoc}
     */
    public String getCredentialsType() {
        return TYPE_IDENTIFIER;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the password.
     *
     * @return the password
     */
    public char [] getPassword() {
        return password.clone();
    }

}
