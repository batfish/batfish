
package jdd.util.sets;

/**
 * This is the generic interface for finite SETS
 *
 * @see Universe
 */
public interface Set {

	/**
	 * free the memory used by this set. make the set invalid after the call!
	 * <p>NOTE: you can/need not to call this function after theUniverse has been freed!
	 * @see Universe
	 */
	void free();

	/**
	 * @return the cardinality of the set
	 */
	double cardinality();

	/**
	 * insert a value into the set.
	 * @return true if the value was previously not in the set.
	 *
	 */
	boolean insert(int [] value);


	/**
	 * remove a value from the set.
	 * @return true if the value was previously in the set and is now removed.
	 *
	 */
	boolean remove(int [] value);


	/**
	 * set membership test.
	 * @return true if value is in the set
	 *
	 */
	boolean member(int [] value);

	/**
	 * NOTE: both sets must be created with the same type of universe!
	 * @return 0 if equal, -1 if this \subset s, +1 if s \subset this, Integer.MAX_VALUE otherwise
	 */
	int compare(Set s);

	/**
	 * NOTE: the sets must be created with the same type of universe!
	 * @return true if the two sets are equal.
	 */
	boolean equals(Set s);

	/**
	 * @return true if the set is empty
	 */
	boolean isEmpty();
	// boolean isFull();

	/**
	 * @return the inverse set. that is, (UNIVERSE - this set)
	 */
	Set invert();

	/**
	 * creates a deep copy of this set.
	 */
	Set copy();

	/**
	 * empties the set.
	 *
	 * <p> NOTE: this does not return the memory allocated by this set. to do that, call free()
	 *
	 * @see #free
	 */
	void clear();

	/**
	 *
	 * @return union of two sets (sets must be from the same universe)
	 */
	Set union(Set s1);

	/**
	 * @return intersection of two sets (sets must be from the same universe)
	 */
	Set intersection(Set s1);

	/**
	 *
	 * <p>NOTE: sets must be from the same universe
	 *
	 * @return difference between this set and s1. i.e.  [this - s1]
	 */
	Set diff(Set s1);


	/**
	 * @return the special set enumerator for this type of set.
	 */
	SetEnumeration elements();

}
