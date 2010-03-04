/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html 
 *
 * Copyright (c) 2007-2010, Oracle and/or its affiliates.
 *
 * All rights reserved.
 */

package com.sun.sgs.test.util;

import java.io.Serializable;

/**
 * Define a serializable class in a different package with a non-serializable
 * superclass with a default access no-arguments constructor.
 */
public class PackageSuperclassConstructor extends PackageConstructor
    implements Serializable
{
    private static final long serialVersionUID = 1;
    public PackageSuperclassConstructor() { }
}
