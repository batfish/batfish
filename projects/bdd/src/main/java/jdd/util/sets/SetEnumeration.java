
package jdd.util.sets;


/**
 * An iterator for the set interface.
 *
 * @see Set
 * @see Universe
 */
public interface SetEnumeration {
	/**
	 * free the memory allocated by the iterator (which can be small or huge depending on the implementation).
	 *
	 * <p>NOTE: you can/need not to call this function after the Universe has been freed!
	 * @see Universe
	 */
	void free();

	/**
	 * has the iterator more elements to show us?
	 * @return true if there are still elements in the set.
	 */
	boolean hasMoreElements();

	/**
	 * what is the next element?
	 * @return the next element as an numbered n-tupple.
	 *
	 */
	int [] nextElement();
}
