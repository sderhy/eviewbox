package com.sderhy;
import java.awt.*;
import java.awt.event.*;
import java.io.* ;
import java.net.* ;

public class MailerWidget extends Frame implements ActionListener , WindowListener{

	TextField mTF, sTF ,smtpTF, senderTF ;
	TextArea TA ;
	Button send , cancel ;
	Button file ;
	static String host = "mail.yourDomain.com";
	String recipient;
	String senderHost = "domain.com"; //domain address : HELO domain.com
	static String sender = "yourName@Domain.com";
	String[] message ;
	public String subject = " " ;
	public static final String crlf = "\r\n" ;
	static boolean firstPass = true ;
	static String lastDir ;
	int count = 0;
	public static final boolean  debug = false ;

	public MailerWidget(String auteur){
		this() ;
		if( auteur == "") this.recipient = "dicom@derhy.com" ;
		else recipient = auteur ;
		mTF.setText(recipient);
		sTF.setText("eViewBox application ...") ;
	}
	public MailerWidget(){

		super("Mail with EViewBox");
		mTF = new TextField(30);
		mTF.addActionListener(this);
		sTF = new TextField(30);
		sTF.addActionListener(this);

		InsetPanel ip = new InsetPanel(Color.pink) ;
		ip.setEtched(true) ;
		Panel ip0 = new Panel() ;
		Panel ip1 = new Panel() ;
		Panel ip3 = new Panel() ;

		ip0.setLayout( new FlowLayout(FlowLayout.LEFT));
		ip1.setLayout( new FlowLayout(FlowLayout.LEFT));
		ip3.setLayout( new FlowLayout(FlowLayout.LEFT));
		ip.setLayout(new GridLayout(3,1,0,0) );
		TA = new TextArea(20,6) ;

		Label yourName = new Label("Mail to : ");
		Label smtpServer = new Label("Smtp server :" ) ;
		ip3.add(smtpServer) ;

		smtpTF = new TextField(host) ;
		ip3.add(smtpTF) ;
		smtpTF.addActionListener(this ) ;

		ip3.add(new Label("Sender :"));
		senderTF = new TextField(sender) ;
		ip3.add(senderTF) ;
		send = new Button( "Send" ) ;
		send.addActionListener(this);
		file = new Button("file...") ;
		file.addActionListener(this) ;
		cancel = new Button( "Cancel " ) ;
		cancel.addActionListener(this);
		//sender = "" ;
		ip0.add( yourName ) ;
		ip0.add(mTF) ;

		ip1.add(new Label("Subject :")) ;
		ip1.add(sTF) ;
		ip1.add(send);
		ip1.add(file) ;
		ip1.add(cancel);

		sTF.setText(subject) ;
		ip.add(ip3);//smtp server//Sender
		ip.add(ip0);
		ip.add(ip1);
		this.add("North",ip);
		this.add("Center",TA );	this.pack() ;

		//must include a WindowListner :
		this.addWindowListener(this);

	}

	public MailerWidget(String subject, String recipient ){
		this() ;
		this.subject =subject ;
		this.recipient = recipient ;
	}


	public void actionPerformed(ActionEvent e){
		if(e.getSource() == file ) {file() ;cancel() ;}
		if( e.getSource() == send) { if(send() )cancel();}
		if( e.getSource() == cancel ) cancel() ;

	}
	public void cancel() {
		 this.hide();
		 this.dispose() ;
	}

	public void file(){
		count =0 ;
		Smtp session = null ;
		sender = senderTF.getText() ;
		host = smtpTF.getText() ;

		FileDialog fd = new FileDialog(this,"Selectionner un fichier ",FileDialog.LOAD) ;
		if( firstPass){// get the file dialog to the user.dir at first time .
			fd.setDirectory(System.getProperty("user.dir"));
			firstPass = false ;
			}// end  if first pass
		else
			fd.setDirectory(lastDir) ;
		fd.pack();  // bug workaround
		fd.show();  // blocks until user selects a file
		if( fd.getFile() == null) return  ;
		String fileString =  fd.getDirectory()+fd.getFile() ;
		if (fd.getDirectory() != null) lastDir = fd.getDirectory() ;
		fd.dispose();



		try{
			FileReader fr = new FileReader(fileString) ;
			BufferedReader br = new BufferedReader(fr) ;
			String line = "" ;
			line = br.readLine() ;

			session = new Smtp(host , sender , senderHost);

			while((line = br.readLine() ) != ".") {
				if(line == null) break;
				send(session, line) ;
			}

		}catch(IOException ignore){	dbg(ignore.toString());}
		finally{ session.close() ;}
	}


	public void send(Smtp session, String line){

		mTF.setText(line);
		subject = sTF.getText() ;
		count++ ;
		line.trim();
		if( line.indexOf("@" ) == -1 ) return  ;
		if(line.charAt(0) =='#') return ;
		line = parseEmail(line);
		if(line == "") return ;
		recipient = line;
		dbg(line +" n°  " + count );
		if(!debug){
			String body  =  TA. getText() ;
			message = new String[13] ;
			int j = 0 ;
			message[j++] = "From : "+ sender ; ;//1
			message[j++] = "Organization: " + "EViewBox Organization" ;//2
			message[j++] = "X-Mailer : RDMailer_EviewBox" ;//3
			message[j++] = "X-Sender: " + sender ;//4
			message[j++] = "To: " + recipient ;		//5
			message[j++] = "Subject: "+ subject  ;//6
			message[j++] = "MIME-Version: 1.0" ;//7
			message[j++] =  "X-Priority: " + "3 (Normal)" ;//8
			message[j++] = "Content-Type: text/plain; charset=us-ascii";//9
			message[j++] = "Content-Transfert-encoding: 7bit" ;//10
			message[j++] =  crlf;//11
			message[j++] = parseBody( body );//12
			message[j++] = "" ; ///Signature13
			try{
				session.sendMessages(line,message);
				}
			catch( java.io.IOException e){
				tools.Tools.debug("erreur ligne " + line + " :\n\t"+e);
			}
			return ;
		}//endifdebug
	}

	public boolean  send(){
		sender =senderTF.getText();
		sender.trim();
		host= smtpTF.getText() ;
		host.trim();
		recipient = mTF.getText();
		subject = sTF.getText() ;
		String body  =  TA. getText() ;
		message = new String[13] ;
		int j = 0 ;
	//	message[j++] = "Date: "+ " " ;
		message[j++] = "From :"+sender ;//1
		message[j++] = "Organization: " + "EViewBox Organization" ;//2
	//	message[j++] = "X-Sender: "+ host ;
		message[j++] = "X-Mailer : RDMailer_EViewBox" ;//3
		message[j++] = "X-Sender: " + sender ;//4
		message[j++] = "To: " + recipient ;		//5
		message[j++] = "Subject: "+ subject ;//6
	//	message[j++] =  crlf;
	//	message[j++] = "Cc: " ;
// 		message[j++] = "Reply to: " + host ;
		message[j++] = "MIME-Version: 1.0" ;//7
		message[j++] =  "X-Priority: " + "3 (Normal)" ;//8
		message[j++] = "Content-Type: text/plain; charset=us-ascii";//9
		message[j++] = "Content-Transfert-encoding: 7bit" ;//10
		message[j++] =  crlf;//11
		message[j++] = parseBody(body ) ;//12//retrieve crlf
		message[j++] = "" ; ///Signature13

		try{
			com.sderhy.Smtp session = new com.sderhy.Smtp(host , recipient, sender , senderHost, message );
			session.connect() ;
			session.sendMessage(message);
			session.close() ;
		}
		catch( java.io.IOException e){
			new AlertBox(this,"Networking exception" ,e.toString());
			return false ;
		}
	//	for(int i = 0 ; i <message.length ;i ++ )
	//			System.out.println( message[i] );
		return true ;
		}


	public String parseEmail(String email){
		String token ="";
		java.util.StringTokenizer st = new java.util.StringTokenizer(email);
		while(st.hasMoreElements()){
				token = st.nextToken() ;
				token.trim() ;
				if(validAdrs(token)) return token ;
			}
		return token ;
	}
	public String parseBody(String body ){//traduit lf en crlf ...
		String parsed ="" ;
		int last_i =0 ;
		char[] array = body.toCharArray();
		for(int i = 1; i< array.length ; i++ ){
			if(array[i] == '\n' && array[i-1] != '\r'){
				parsed += body.substring(last_i , i) + crlf ;
				last_i = i+1 ;
			}
		}
		if(last_i <  body.length())
			parsed += body.substring(last_i , body.length() ) + crlf;


		return parsed ;
	}
	protected boolean validAdrs(String email ){
			if(email.indexOf("@") <= 0) return false  ;
			if(email.indexOf("/")!= -1 )return false  ;
			if(email.indexOf(":")!=-1)return false  ;
			if(email.indexOf(",")!=-1)return false  ;
			if(email.indexOf(";")!=-1)return false  ;
		// at least a point :
			if(email.indexOf(".") <= 0)return false  ;
			if(email.indexOf("#") !=-1)return false  ;
			return true;
	}



	public void dbg(String m){
		if(true) System.out.print(m) ;
		}

	public static void main(String[] args){
		MailerWidget MW = new MailerWidget() ;
		MW.setVisible(true );
	}
	////windowListener
  public void windowClosing(WindowEvent e) { cancel(); }
  public void windowOpened(WindowEvent e) {}
  public void windowClosed(WindowEvent e) {}
  public void windowIconified(WindowEvent e) {}
  public void windowDeiconified(WindowEvent e) {}
  public void windowActivated(WindowEvent e) {}
  public void windowDeactivated(WindowEvent e) {}


}