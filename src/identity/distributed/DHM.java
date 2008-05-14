package identity.distributed;

import identity.election.CoordLock;

import java.io.Serializable;
import java.util.UUID;

/**
 * <<Class summary>>
 *
 * @author Jaremy Creechley &lt;&gt;
 * @version 0.1
 */
abstract class DHM implements Serializable, Types
{

	protected int type;
	protected Lamport lamport;
	protected UUID coord;


	public DHM(int type)
	{
		this.type = type;
		this.lamport = new Lamport( CoordLock.getLamport(), CoordLock.getCoordsession());
	}
	protected DHM()
	{}

	public String toString()
	{
		String tostring = "";
		tostring += "type: " + type;
		tostring += " ";
		tostring += "lamport " + lamport;
		return tostring;
	}

}

