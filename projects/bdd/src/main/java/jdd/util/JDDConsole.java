
package jdd.util;

/**
 * out own version of System.out, can be stdout, a file or even some AWT component ...
 *
 * @see PrintTarget
 */
public class JDDConsole {
	public static PrintTarget out = new StdoutTarget();
}
