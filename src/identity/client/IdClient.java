package identity.client;

import java.io.UnsupportedEncodingException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import sun.misc.BASE64Encoder;

import identity.calendar.CalendarDB;
import identity.calendar.CalendarEntry;
import identity.server.*;

public class IdClient
{
	private String serverName;
	
	// something new!
	private int registryPort = 5299;
	private IdentityUUID userdb;

	private UserInfo options, modoptions;
	public CalendarEntry calentry;
	public CalendarDB localCalDb ;
	
	private int type;
	public static Hashtable<String, String> argsHash = new Hashtable<String, String>();
	public static void main(String[] args)
	{
		//System.err.println("Beginning Static");
		System.setProperty("javax.net.ssl.trustStore", "./resources/Client_Truststore");
		System.setProperty("java.security.policy", "./resources/mysecurity.policy");
		/*System.setSecurityManager(new RMISecurityManager());*/

		/*if (args.length != 1) {
			System.out.println("usage: java IdClient <IdServer Address> [--switch]");
			System.exit(1);
		}*/

		//
		int numinputs = 2;
		if (args.length < 3) {
			exit_message("Not enough arguements");
		}
		String host = null;
		host = args[0];
		int port = -1;
		if (args[1].startsWith("-")) {
			numinputs = 1;
		}
		else if (args.length > 3) {
			port = Integer.parseInt(args[1]);
			numinputs = 2;
		}
		IdClient client = new IdClient();
		client.parseInput(args);
		//client.printArgsHash();
		client.localCalDb= new CalendarDB("calendar");
		client.parseInput(args);
		client.printArgsHash();
		client.setServerName(host, port);
		client.parse_switches(args, numinputs);
		client.perform();
		

	}

	/** 
	 * run the client.
	 */
	private void perform()
	{
		try {
			//bind server object to object in client
			Registry reg = LocateRegistry.getRegistry(registryPort);
			userdb = (IdentityUUID) reg.lookup("IdentityServer");
			System.out.println("RMI connection successful");

			//invoke method on server object

			//String result = userdb.sayHello();
			//System.out.println("The response from the server is " + result);
			command_line();
		}
		catch (Exception e) {
			//e.printStackTrace();
			System.out.println("Exception occured: " + e);
			StackTraceElement[] st =  e.getStackTrace();
			System.out.println("Trace 0: "+st[0]);
			System.out.println("Trace 1: "+st[1]);
			System.out.println("Trace 2: "+st[2]);
			System.exit(0);
		}
	}

	public String getServerName()
	{
		return serverName;
	}

	/**
	 * Pretty simple, just does what the name says.
	 * @param serverName name of the server
	 * @param port port number, defaults to 5299
	 */
	public void setServerName(String serverName, int port)
	{
		this.serverName = serverName;
		this.registryPort = (port<1) ? registryPort : port ;
		System.out.println("Connecting to Host: "+serverName+" Port: "+registryPort);
	}


	
/*		--create <loginname> [<real name>] [--password <password>] With this op- 
		tion, the client contacts the server and attempts to create the new login name. 
		The client optionally provides the real user name along with the request. In 
		Java, we can merely pass the user.name property as the user’s real name. Use 
		System.getProperty("user.name") to obtain this information. If successful, it 
		returns the UUID (or an Account ob ject from the server that the client can then 
		cache if it so desires). Otherwise it returns an error message. 
		--lookup <loginname> With this option, the client connects with the server and 
		looks up the loginname and displays all information found associated with the 
		login name. 
		--reverse-lookup <UUID> With this option, the client connects with the server and 
		looks up the UUID and displays all information found associated with the UUID. 
		--modify <oldloginname> <newloginname> [--password <password>] The client 
		contacts the server and requests a loginname change. If the new login name is 
		available, the server changes the name (note that the UUID does not ever change, 
		once it has been assigned). If the new login name is taken, then the server returns 
		an error. 
		--get users|uuids|all The client contacts the server and obtains either a list all 
		login names, lits of all UUIDs or a list of user, UUID and string description all 
		accounts (don’t show encrypted passwords in this option). */
	

	/**
	 * exit_message
	 * @param message to print before exiting.
	 */
	public static void exit_message(String mesg)
	{
		System.err.println("\n"
          + "--create <loginname> [<real name>] --password <password> 		create the new login name.\n"
          + "--lookup <loginname> 	connects with the server and looks up the loginname and displays it.\n"
          + "--reverse-lookup <UUID> 	looks up the UUID and displays all information with that UUID.\n"
          + "--modify <oldloginname> <newloginname> --password <password> modify a given username\n"
          + "--get users|uuids|all 	lists either the entire users,uuids or all information");

		System.err.println("Usage: java IdClient <host> [<registry-port>] [--switch]");
		if (mesg != null) {
			System.err.println(mesg);
		}
		System.exit(1);
	}

	/**
	 * password Takes a string password and modifies it using whatever algorithm is wanted.
	 * @throws UserInfoException for error in hash
	 * @param pass the plaintext password to be encoded
	 * @param uid the user uuid to be used for salting.
	 * @return returns a base64 encoding of the username hash.
	 */
	public String password(String pass, UUID uid) throws UserInfoException
	{
		MessageDigest md = null;
		String hashType = "MD5";
		try {
			// try to get the given hash and then get the data.
			md = MessageDigest.getInstance(hashType);
			md.update( ( pass+uid.toString() ).getBytes("UTF-8") );
		} catch (NoSuchAlgorithmException e) {
			System.err.println("NoSuchAlgorithmException: type "+hashType+"\n"+e);
			throw new UserInfoException("Unable to hash password.",0);
		} catch (UnsupportedEncodingException e) {
			System.err.println("UnsupportedEncodingException: type "+hashType+"\n"+e);
			throw new UserInfoException("Unable to hash password.",0);
		}
		
		byte raw[] = md.digest(); // digest it
		String hash = (new BASE64Encoder()).encode(raw); // encode it in BASE64Encoder
		//System.err.println("(DEBUG) \n\tMD5: "+hash+"\n\traw: "+raw);
		return hash; 
	}

	/**
	* parse_switches parse the switches to know what commands to execute
	* @param args array of command line arguements
	* @param init beginning count of command line switches
	 * @throws UserInfoException 
	*/
	private void parse_switches(String[] argv, int init)
	{
		// args 0 host
		// args 1 port
		// example uuid d8054cbd-31b5-4932-a370-a93d2916c92f
		String get = null;
		String user = null;
		String real = "";
		String pass = null;
		String newuser = null;
		UUID uuid = null;
		type = -1;
		// –c, –l, –r, –m and –g. 
		try {
			// switch 0
			if ( argv[init].equals("--create") || argv[init].equals("-c") ) {
				init++;
				if (argv[init].startsWith("-")) exit_message("Too many switches");
				if (argv[init].length() > 32) throw new UserInfoException("Username too long", 1);
				user = argv[init++];
				// now check for real name
				for (int i = 0; i < 6 && !(argv[init].startsWith("--")); i++)
					real = real + argv[init++];
				// get password
				if (argv[init].equals("--password"))
					//pass = password(argv[++init]);
					pass = argv[++init];
				else
					exit_message("create switch must use --create <loginname> [<real name>] [--password <password>] ");
				type = 0;
			}
			// switch 1
			else if (argv[init].equals("--lookup") || argv[init].equals("-l")) {
				init++;
				// --lookup <loginname>
				if (argv[init].startsWith("-")) exit_message("Too many switches");
				user = argv[init];
				type = 1;
			}
			// switch 2
			else if (argv[init].equals("--reverse-lookup") || argv[init].equals("-r")) {
				init++;
				// --reverse-lookup <UUID>
				if (argv[init].startsWith("-")) exit_message("Too many switches");
				uuid = UUID.fromString(argv[init]);
				type = 2;
			}
			// switch 3
			else if ( argv[init].equals("--modify") || argv[init].equals("-m") ) {
				init++;
				//--modify <oldloginname> <newloginname> [--password <password>]
				// get username
				if (argv[init].startsWith("-")) exit_message("Too many switches");
				if (argv[init].length() > 32 ) throw new UserInfoException("Username too long", 1);
				user = argv[init++];
				// get newusername
				if (argv[init].startsWith("-")) exit_message("Too many switches");
				if (argv[init].length() > 32 ) throw new UserInfoException("Username too long", 1);
				newuser = argv[init++];

				// get password
				if (argv[init].equals("--password"))
					//pass = password(argv[++init]);
					pass = argv[++init]; // don't hash password quite yet
				else
					exit_message("create switch must use --create <loginname> [<real name>] [--password <password>] ");
				type = 3;
			}
			// switch 4
			else if (argv[init].equals("--get") || argv[init].equals("-g")) {
				init++;
				// --get users|uuids|all
				if (argv[init].startsWith("-")) exit_message("Too many switches");
				get = argv[init]; // just using extra var
				user = get;
				if (!( get.equals("users") || get.equals("uuids") || get.equals("all") ))
					exit_message("Incorrect --get request ");
				type = 4;
			}
			//switch 5
			/*new calendar entry*/
			else if (argsHash.containsKey("--new") || argsHash.containsKey("-n"))
			{
				
				if(argsHash.containsValue("cal") && argsHash.containsKey("-u") &&
				  (argsHash.containsKey("--password") || argsHash.containsKey("-p")) &&
				   argsHash.containsKey("-t") && argsHash.containsKey("-sl") && 
				   argsHash.containsKey("-des") && argsHash.containsKey("-du"))
				{
					if(argsHash.get("-des").length() >128)
					  System.out.println("Description too long ..trucating to 128 bytes");
					//truncate cod comes here
					
					int id =getNextSeqNum();
					DateFormat df = DateFormat.getDateTimeInstance();
					Date datetime = df.parse(argsHash.get("-t"));
					boolean isPublic =false;
					if(argsHash.get("-sl").equalsIgnoreCase("public"))
						isPublic =true;
					int duration = Integer.parseInt(argsHash.get("-du"));
					
					user = argsHash.get("-u");
					if(argsHash.containsKey("--password") )
						pass =argsHash.get("--password");
					else
						pass =argsHash.get("-p");
					
					calentry = new CalendarEntry(null, id, datetime, isPublic, argsHash.get("-des"),duration);
							
					type = 5;	
				}
				else
				   exit_message("Incorrect number of parameters to create calendar entry");
				
										
			}
			//switch 6
			/*delelte calendar entry*/
			else if(argsHash.containsKey("--del") || argsHash.containsKey("-d"))
			{
				if(argsHash.containsValue("cal") && argsHash.containsKey("-u") &&
				  (argsHash.containsKey("--password") || argsHash.containsKey("-p")) &&
						   argsHash.containsKey("-s"))
				{
					
					user = argsHash.get("-u");
					if(argsHash.containsKey("--password") )
						pass =argsHash.get("--password");
					else
						pass =argsHash.get("-p");
					
					int id =Integer.parseInt(argsHash.get("-s"));
					calentry = new CalendarEntry(null, id,null, false, null, 0);
					
					type = 6;	
				}
				else
				   exit_message("Incorrect number of parameters to Delete Calendar entry.");
					
			}
			//switch 7
			else if(argsHash.containsKey("--show") || argsHash.containsKey("-s"))
			{
					if(argsHash.containsKey("-rusr") && argsHash.containsKey("-u") &&
					  (argsHash.containsKey("--password") || argsHash.containsKey("-p")) &&
					   argsHash.containsKey("-l") && argsHash.containsValue("cal"))
				    {
						/*Display remote user's calendar entries*/
						user = argsHash.get("-u");
						if(argsHash.containsKey("--password") )
							pass =argsHash.get("--password");
						else
							pass =argsHash.get("-p");
						
						newuser = argsHash.get("-rusr");
			    
						
						type = 7;	
					}
					else if(argsHash.containsKey("-u") && argsHash.containsValue("cal") &&
						   (argsHash.containsKey("--password") || argsHash.containsKey("-p")))
					{
						/*Display user's local calendar entries*/
						user = argsHash.get("-u");
						if(argsHash.containsKey("--password") )
							pass =argsHash.get("--password");
						else
							pass =argsHash.get("-p");	
						
						type = 8;	
					}
					else
					 exit_message("Incorrect number of parameters to Display Calendar.");
				
			}
			else
			{
				exit_message("Unknown switch: " + argv[init]);
			}
		}
		catch (ArrayIndexOutOfBoundsException ne) {
			exit_message("Incorrect number of parameters to parse .");
		}
		catch (UserInfoException e) {
			// TODO Auto-generated catch block
			System.err.println("Remote UserInfoException: " + e);
			e.printStackTrace();
			System.err.println("Printed Stack. Exiting.");
		} 
		catch (Exception e) {
			System.err.println("General Error: "+e);
			StackTraceElement[] st =  e.getStackTrace();
			System.out.println("STE0: "+st[0]);
			System.out.println("STE1: "+st[1]);

		}
		real = (real.equals("")) ? System.getProperty("user.name") : real;
		// now create option and modoption
		options = new UserInfo(uuid, user, pass, real, null, null);
		modoptions = new UserInfo(uuid, newuser, pass, real, null, null);
		
		//need to get the unique id here
		//public UserInfo(final UUID uuid, final String username, final String md5passwd,
		//			final String realname, final String ipaddr, final Date lastdate)

	}

	/** 
	 * Runs code for the various switches. Must be called after parse_switches
	 */
	private void command_line()
	{
		// perform command line actions
		UserInfo result = null;
		boolean retval =false;
		System.out.println("Running Command Line " + type);
		//System.out.println("options: " + options + "\n");

		try {
			
			switch (type) {
			case 0:
				UUID uniqueid = userdb.getUniqueUUID();
				options = options.setUUID(uniqueid);
				options = options.setMd5passwd( password(options.md5passwd,options.uuid) );
				result = userdb.createUUID(options);
				System.out.println("Created user: " + printUser(result));
				break;
			case 1:
				result = userdb.lookupUUID(options.username);
				System.out.println("lookup username: " + printUser(result));
				break;
			case 2:
				result = userdb.revlookupUUID(options.uuid);
				System.out.println("reverse lookup UUID: " + printUser(result));
				break;
			case 3:
				System.out.println("Modify Options: " + modoptions);
				// retreive the user UUID for the given user id.
				UUID userid = null;
				try { 
					userid = (userdb.lookupUUID(options.username)).uuid; 
				} catch (NullPointerException e) {
					throw new UserInfoException("Unknown Username: "+options.username,1);
				}
				options = options.setUUID(userid);
				// hash the password and set it in the options.
				options = options.setMd5passwd(password(options.md5passwd,options.uuid));
				//System.err.println("(DEBUG) pass: "+options.md5passwd);
				result = userdb.modifyUserName(options, modoptions);
				
				System.out.println("\nResult modifyUUID: " + printUser(result) + "\n");
				break;
			case 4:
				UserInfo[] dump = (UserInfo[]) userdb.dumpContents();
				System.out.println("get: ");
				for (int i = 0; i < dump.length; ++i ) {
					if ( options.username.equals("uuids"))
						System.out.println("dump: " + dump[i].uuid);
					else if (options.username.equals("users"))
						System.out.println("dump: " + dump[i].username);
					else if (options.username.equals("all"))
						System.out.println("dump: " + dump[i]);
				}
				break;
			case 5:
				/*Create calendar entry*/
				retval = userdb.addCalendarEntry(calentry, options);
				if(retval ==true)
				{
					//Add the entry to the local database
					localCalDb.addEntry(calentry);
				}
				else
					System.out.println("Adding Calendar entry failed");
			case 6:
				/**/
				retval =userdb.deleteCalendarEntry(calentry, options);
				if(retval ==true)
				{
					//Delete the entry to the local database
					localCalDb.delEntry(calentry);
				}
				else
					System.out.println("Deleting Calendar entry failed");				
				break;
			case 7:
				/*Dispaly other user's calendar entry*/
				boolean mode =false;
			    if(argsHash.get("-l").equalsIgnoreCase("all"))
			    	 mode =true;
			    
				CalendarEntry[] entries = (CalendarEntry[]) userdb.displayCalendarEntries(modoptions, options, mode);
				if(entries !=null)
				{				
					System.out.println("Display: ");
					for (int i = 0; i < entries.length; ++i ) 
						System.out.println(entries[i]);
				}
				else
					System.out.println("UserName does not exists");
							    
				break;
			case 8:
				/*Display personal calendar entry*/
				CalendarEntry[] localEntries = (CalendarEntry[])localCalDb.toArray();
				if(localEntries !=null)
				{				
					System.out.println("Display: ");
					for (int i = 0; i < localEntries.length; ++i ) 
						System.out.println(localEntries[i]);
				}			
			
				break;
			default:
				System.out.println("Invalid command.");
			}
		}
		catch (RemoteException e) {
			System.err.println("Something wrong RemoteException: " + e);
			e.printStackTrace();
		}
		catch (UserInfoException e) {
			System.err.println("Remote UserInfo Error: " + e);
			//e.printStackTrace();
		} catch (java.lang.NullPointerException e) {
			System.err.println("Null Exception: " + e);
			e.printStackTrace();			
		}
	}
	
	private void parseInput(String[] ags)
	{
		int argCount  = 0;
		//System.out.println("Args count"+ags.length);
		while(argCount < ags.length)
		{
			
			String key =null;
			
			if(ags[argCount].startsWith("-") ||ags[argCount].startsWith("--"))
			{
				key = ags[argCount];
				//System.out.println("Key "+ags[argCount]);
				argCount ++;
				String value ="";
				boolean stop =false;
				//System.out.println("before Value "+argCount);
				while(stop ==false && argCount < ags.length )
				{
				   value = value + ags[argCount];
				   //System.out.println("Value "+value);

				     argCount ++;
	         		if(argCount <ags.length )
					 {
				     //System.out.println("after Value "+argCount);
					 if(ags[argCount].startsWith("-") ||ags[argCount].startsWith("--")  )
						stop=true;
        			 }
				     else
					   break;
				}
				   if(!argsHash.containsKey(key))
				   {
					   argsHash.put(key, value);
					   
				   }
				//System.out.println("after Value "+argCount);
				

	
			}
			else
				argCount ++;
			
				

		}
			
	
		
	}
	private int getNextSeqNum()
	{
		
		if(localCalDb.db.isEmpty())
		{				
			
			int[] seqId = new int[localCalDb.db.size()];
			for (int i=0;i<localCalDb.db.size();i++)
			{
				seqId[i] = Integer.parseInt((localCalDb.db.keySet().toArray())[i].toString());
				
			}
			Arrays.sort(seqId);
			
			return seqId[localCalDb.db.size()] +1;
			 
		}	
		else
			return 1;
	}
	private void  printArgsHash()
	{
		System.out.println("Arguments Hash is : \n" +argsHash.toString());
	}
	private String printUser(UserInfo u)
	{
		if (u == null)
			return "Empty";
		return "\nUUID: " + ((u.uuid == null) ? null : u.uuid)
		       + "\nUsername: " + u.username
		       + "\nReal Name: '" + u.realname +"'"
		       //+ "'\n(DEBUGGIN) passwd:" + u.md5passwd
		       + "\n" + u.ipaddr
		       + "\nDate Modified: "+u.lastdate;
	}
}


