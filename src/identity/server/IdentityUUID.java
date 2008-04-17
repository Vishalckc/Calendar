/**
 * 
 */
package identity.server;
import identity.calendar.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * @author jaremy
 * Interface for the Server. This defines the basic methods available to interface with 
 * the server.
 */
public interface IdentityUUID extends Remote {
	/**
	 * Debugging method. Keep in for future debugging.
	 * @deprecated
	 * @return
	 * @throws java.rmi.RemoteException
	 */
	String sayHello() throws java.rmi.RemoteException;

/*    The server presents a suitable RPC/RMI interface for clients to access the id functions. 
   The server must export remote methods that allow a client to create a new login name, 
   to lookup a login name, to reverse lookup a UUID as well as to modify an existing 
   login name. 
*/

   public UserInfo createUUID( UserInfo reqUserInfo ) throws UserInfoException,java.rmi.RemoteException;
   public UserInfo lookupUUID( String username ) throws UserInfoException,java.rmi.RemoteException;
   public UserInfo revlookupUUID( UUID requuid ) throws UserInfoException,java.rmi.RemoteException;
   public UserInfo[] dumpContents( ) throws UserInfoException,java.rmi.RemoteException;
   public UserInfo modifyUUID( UserInfo reqUserInfo, UserInfo newUser) throws UserInfoException,java.rmi.RemoteException;
   public UserInfo modifyUserName( UserInfo reqUserInfo, UserInfo newUser) throws UserInfoException,java.rmi.RemoteException;
   public UUID	getUniqueUUID()  throws UserInfoException,java.rmi.RemoteException;

   //debugging method, creates new array
   boolean addCalendarEntry(CalendarEntry calEntry, UserInfo auth) throws UserInfoException,java.rmi.RemoteException;
   boolean deleteCalendarEntry(CalendarEntry calEntry, UserInfo auth) throws UserInfoException,java.rmi.RemoteException;
   CalendarEntry[] displayCalendarEntries(UserInfo newUserinfo,UserInfo auth,boolean mode) throws UserInfoException,java.rmi.RemoteException;
   ArrayList<Date> getFreeTimeSlots(UserInfo newUserinfo, UserInfo auth,
			long req_Start_time,long req_Stop_time) 
			throws UserInfoException, RemoteException, ParseException;
}
