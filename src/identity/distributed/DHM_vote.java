/**
 * 
 */
package identity.distributed;

/**
 * @author jaremy
 *
 */
public class DHM_vote extends DHM {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1017197010755188589L;

	/**
	 * @param type
	 * @param lamport
	 */
   public DHM_vote(int vote_msg) {
      super(vote_msg);
   }

   public DHM_vote(int vote_msg, Lamport lamport) {
      this.type = vote_msg;
      this.lamport = lamport;
   }

}
