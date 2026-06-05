package com.sderhy ;
import java.io.*;
import java.net.*;
import java.util.*;

public class POP3Session {

	protected Socket pop3Sock;
	protected DataInputStream inStream;
	protected DataOutputStream outStream;
	public String userName;
	public String password;
	public String host;
	public int port;

	public POP3Session(String host,String userName,String password) {
		this.host=host;
		this.port=110;
		this.userName= userName;
		this.password= password;
	}
	public POP3Session(String host,int port,String userName,String password) {
		this.host=host;
		this.port= port;
		this.userName= userName;
		this.password= password;
	}
	protected boolean isErrorResponse(String str){
		return (str.charAt(0) != '+');
	}
//--Command STAT : number of messages---------------------------------------------
	public int getMessageCount() throws IOException{
		String response = doCommand("STAT");
		if(isErrorResponse(response)) throw new IOException(response);
		//typical response :+OK 98 4486676
		if( response.length() < 4  ) return 0 ;
		response = response.substring(4); //get the right side of the response only
		response = response.substring(0, response.indexOf(' '));
		try{
			int count=Integer.valueOf(response).intValue();
			return count;
			}
		catch(NumberFormatException e){throw new IOException("Invalid reponse -"+e);}
	}//end of getMessageCount()
//---Command UIDL---------------------------------------------------------------------
	public String[] getUIDL() throws IOException{
		String response = doCommand("UIDL ");
		if(isErrorResponse(response)) throw new IOException(response);
		return getData();
	}//end of getUIDL

//-Commande RETR----------------------------------------------------------------------
	public String[] getMessage(int messageNumber) throws IOException{
		String response = doCommand("RETR "+ messageNumber);
		if(isErrorResponse(response)) throw new IOException(response);
		return getData();
	}//end of getMessage
//--Commande LIST--------------------------------------------------------------------
	public String[] getHeaders() throws IOException{
		String response = doCommand("LIST");
		if(isErrorResponse(response)) throw new IOException(response);
		return getData();
	}//end of getHeaders

	public String getHeader(int messageNumber) throws IOException{
		String response = doCommand("LIST "+messageNumber);
		if(isErrorResponse(response)) throw new IOException(response);
		return response;
	}//end of getHeader

//--Command TOP----------------------------------------------------------------------
	public String[] getMessageHead(int messageNumber, int lineCount) throws IOException{
		String response = doCommand("TOP "+ messageNumber+" "+lineCount);
		if(isErrorResponse(response)) throw new IOException(response);
		return getData();
	}//end of getMessageHead

//---Command DELE------------------------------------------------------------------
	public void deleteMessage(int messageNumber) throws IOException{
		String response = doCommand("DELE "+messageNumber);
		if(isErrorResponse(response)) throw new IOException(response);

	}//end of method DELE

//---Command RSET----------------------------------------------------------------------
	public void reset() throws IOException{
		String response = doCommand("RSET");
		if(isErrorResponse(response)) throw new IOException(response);
	}//end of method RSET
//----Command QUIT---------------------------------------------------------------------
	public void quit() throws IOException{
		String response = doCommand("QUIT");
		if(isErrorResponse(response)) throw new IOException(response);
	}//end of method QUIT

//---Connection to host---------------------------------------------------------------
	public void connect() throws IOException{
		pop3Sock = new Socket(host,port);
		inStream = new DataInputStream(pop3Sock.getInputStream());
		outStream = new DataOutputStream(pop3Sock.getOutputStream());
		String response= inStream.readLine();
		System.out.println("Connexion à "+host );
		System.out.println("Message de connexion  : " + response );
		response = doCommand("USER "+userName);
		if(isErrorResponse(response)) throw new IOException(response);
		response = doCommand("PASS "+password);
		if(isErrorResponse(response)) throw new IOException(response);
	}//end of method!!
//---Close all sockets---------------------------------------------------------------
	public  void close() throws IOException{
		pop3Sock.close();
		pop3Sock = null;
	}
//---Method doCommand sends a command and read the answer ---------------------------
	protected String doCommand(String command) throws IOException{
		outStream.writeBytes(command+"\n");
		String response= inStream.readLine();
		return response;
	}
//---getData() read a . terminated message and sends back a String[]------------------
	protected String[] getData() throws IOException{
		Vector lines = new Vector();
		String line;

		while( (line =inStream.readLine())!= null){
			if(line.equals(".")){
				String response[]= new String[lines.size()];
				lines.copyInto(response);
				return response ;
				}
			if((line.length()>0) && (line.charAt(0)=='.'))
				line = line.substring(1);

		lines.addElement(line);
		}//end while
	throw new IOException("Connection closed");
	}//end of method

}
