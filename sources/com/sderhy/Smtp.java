package com.sderhy ;
import java.io.*;
import java.net.*;
import java.util.*;

public class Smtp {

	public String host;//mail.imaginet.fr
	public String recipient;//name
	public  int port;
	public String senderHost ;//Helo senderHost..
	public String[] message;
	public String sender ;
	protected Socket sessionSocket ;
	protected DataInputStream in ;
	protected DataOutputStream out ;
	public static final boolean debug = true ;
	public static final String crlf ="\r\n" ;
	public Smtp(){}
	public Smtp(String host,
				String recipient, 
				String sender, 
				String senderHost,
				String[] message )
									throws IOException{
		this.host = host;
		this.port=25 ;
		this.recipient = recipient;
		this.message =message ;
		this.sender = sender ;
		this.senderHost = senderHost ;
	}
	public Smtp(String host,
				String sender, 
				String senderHost )
									throws IOException{// pour messages multiples
		this.host = host;
		this.port=25 ;
		//this.recipient = recipient;
		//this.message =message ;
		this.sender = sender ;
		this.senderHost = senderHost ;
		connect() ;
	}
	public void close() /*throws IOException*/{
		
		try{
		out.writeBytes(crlf+"QUIT\r\n");
		in.close();
		out.close() ;
		}catch( IOException e){ e.fillInStackTrace() ;}
		finally{
			try{
				sessionSocket.close() ;
				sessionSocket = null;
				}catch(Exception ignore){ ignore.fillInStackTrace();}
		}
	
	
	}	
	public void connect() throws IOException {
		sessionSocket = new Socket (host ,port ) ;
		dbg("connection  a :"+ host + " port  :"+ port );
		in = new DataInputStream(sessionSocket.getInputStream()) ;
		out= new DataOutputStream(sessionSocket.getOutputStream());
	}
	public String doCommand(String command) throws IOException{
		out.writeBytes(command + crlf) ;
		dbg("j'ai dit : " +command);
		String reponse = getResponse() ;
		dbg("La reponse est :" + reponse );
		return reponse ;
	}
	
	protected void  isErr(String reponse) throws IOException{
		if(reponse.charAt(0) !='2')
			throw new IOException( reponse ) ;
	}

	
	
	public String getResponse() throws IOException{
		String reponse = "" ;
		for(;;){
			String line = in.readLine() ;
			if(line == null) throw new IOException("Mauvaise reponse du serveur") ;
			if(line.length() < 3 )throw new IOException (" Réponse inconnue : " + line );
			reponse += line + "\n";
		//utile :???	
			if((line.length()==3) || (line.charAt(3)!= '-'))
				return reponse ; 
			}//end for
		}//end method getResponse 
	

	public void sendMessages(String recipient,String[] message ) throws IOException{
		//( response from the server...welcome...)
		if(sessionSocket == null) throw new IOException("Session  null ") ;
		if(senderHost == null ) throw new IOException("SenderHost null" ) ;
		
		String reponse =""; 
		reponse = doCommand("HELO "+ senderHost);
		isErr(reponse);
		//attention il faut parfois ajouter le nom de l'hote
		reponse = doCommand("MAIL FROM: <" + sender +">");//sender "<"+sender+">"
		isErr(reponse) 	;
		
		reponse = doCommand("RCPT TO: <" + recipient+">" ) ;
		isErr(reponse ) ;
		
		reponse = doCommand("DATA" ) ;
		//la reponse commence par un 3 si le serveur attend les donnees.
		if(reponse.charAt(0)!= '3' ) throw new IOException(reponse) ;
		
		//envoi du message ligne par ligne :
		for(int i= 0 ; i< message.length ; i++){

				if(message[i].length() == 0){
					out.writeBytes("\r\n");
					continue ;
					}
				if(message[i].charAt(0) == '.'){
					out.writeBytes("."+ message[i] + "\r\n" ) ;
				}else {
						out.writeBytes(message[i] + "\r\n" ) ;
						dbg(  message[i] ) ;
						}
		}//end for
		
		reponse = doCommand(crlf + "." );
		isErr(reponse);

	}//fin de SendMessages.
	
	public void sendMessage(String[] message) throws IOException{
		connect() ;
		String reponse = getResponse() ;
		isErr(reponse);
		//( response from the server...welcome...)
		reponse = doCommand("HELO "+ senderHost);
		isErr(reponse);
		//attention il faut parfois ajouter le nom de l'hote
		reponse = doCommand("MAIL FROM: <" + sender +">");//sender "<"+sender+">"
		isErr(reponse) 	;
		
		reponse = doCommand("RCPT TO: <" + recipient+">" ) ;
		isErr(reponse ) ;
		
		reponse = doCommand("DATA" ) ;
		//la reponse commence par un 3 si le serveur attend les donnees.
		if(reponse.charAt(0)!= '3' ) throw new IOException(reponse) ;
		
		//envoi du message ligne par ligne :
		for(int i= 0 ; i< message.length ; i++){

			if(message[i].length() == 0){
				out.writeBytes("\r\n");
				continue ;
			}
			if(message[i].charAt(0) == '.'){
				out.writeBytes("."+ message[i] + "\r\n" ) ;
			}else {
					out.writeBytes(message[i] + "\r\n" ) ;
					dbg(  message[i] ) ;
			}
		}//end for
		
		reponse = doCommand(crlf+"." +crlf);
		isErr(reponse);
		
	}//fin de methode
		
		public void dbg(String message ){
			if(debug)System.out.println(">>>"+message) ;
		}
}//fin de classe
