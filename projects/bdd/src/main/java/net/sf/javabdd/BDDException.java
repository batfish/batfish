// BDDException.java, created Jan 29, 2003 9:50:57 PM by jwhaley
// Copyright (C) 2003 John Whaley
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package net.sf.javabdd;

/**
 * An exception caused by an invalid BDD operation.
 * 
 * @author John Whaley
 * @version $Id: BDDException.java,v 1.2 2005/04/29 06:43:31 joewhaley Exp $
 */
public class BDDException extends RuntimeException {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3761969363112243251L;
    
    public BDDException() {
        super();
    }
    public BDDException(String s) {
        super(s);
    }
}
