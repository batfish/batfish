/*
 * Note: We obtained permission from the author of Javabdd, John Whaley, to use
 * the library with Batfish under the MIT license. The email exchange is included
 * in LICENSE.email file.
 *
 * MIT License
 *
 * Copyright (c) 2013-2017 John Whaley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package net.sf.javabdd;

/**
 * An exception caused by an invalid BDD operation.
 *
 * @author John Whaley
 * @version $Id: BDDException.java,v 1.2 2005/04/29 06:43:31 joewhaley Exp $
 */
public class BDDException extends RuntimeException {
  /** Version ID for serialization. */
  public BDDException() {
    super();
  }

  public BDDException(String s) {
    super(s);
  }
}
