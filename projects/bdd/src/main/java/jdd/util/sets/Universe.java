package jdd.util.sets;

/**
 * This is the generic interface for finite universe consisting of a set of subdomain.
 *
 * <p>This is a good representation for n-tupples in a discrete space. The space is specified during
 * construction with an int-vector of length n, defining the number of elements in each subdomain.
 *
 * <p>Each element in the space is represented by an int-vector of length n.
 *
 * @see Set
 * @see jdd.bdd.sets.BDDUniverse
 */
public interface Universe {

  /**
   * create a new set that is empty
   *
   * @return an empty set
   */
  Set createEmptySet();

  /**
   * create a set that contains all possible elements
   *
   * @return a set that holds all the elements in the universe.
   */
  Set createFullSet();

  /**
   * The domainSize if the size of all elements in the universe.
   *
   * @return the size of this domain.
   */
  double domainSize();

  /**
   * @return the number of sub-domains used in this universe
   */
  int subdomainCount();

  /**
   * cleanup the universe and die.
   *
   * <p>NOTE: you must call this function in order to free all used memory. a call to free() also
   * frees all Set and SetEnumeration objects (making then invalid!)
   *
   * @see Set#free
   */
  void free();
}
