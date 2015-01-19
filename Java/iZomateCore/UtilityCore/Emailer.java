package iZomateCore.UtilityCore;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Emailer {
	private String 	mSender;
	private String 	mMailServer;
	private Session mSession= null;
	private String  m_pAddresses= null;

	
	/**
	 * Creates an instance of an Emailer for sending emails using default mail server: "mailbox.izotope.com".
	 * To override default use _SetMailServer()
	 * @param sender
	 */
	public Emailer( String sender ) {
		this.mMailServer= "mailbox.izotope.com";
		this.mSender= sender;
		if( !this.mSender.contains("@"))
			this.mSender+= "@izotope.com";
	}
	
	/**
	 * Comma separated list of email addresses
	 * @param strRecipients
	 * @return
	 * @throws AddressException
	 */
	public Emailer _SetRecipients( String strRecipients ) throws AddressException {
		this.m_pAddresses= strRecipients;
		return this;
	}

	/**
	 * 
	 * @param mailServer
	 * @return
	 */
	public Emailer _SetMailServer( String mailServer ) {
		this.mMailServer= mailServer;
		this.mSession= null; // force the session to refresh upon next sendEmail
		return this;
	}
	
	/**
	 * 
	 * @param subject
	 * @param message
	 */
	public void _SendEmail( String subject, String message ) {
		try {
			// Create a default MimeMessage object.
			MimeMessage email = new MimeMessage( this._getSession() );
			email.setSubject( subject );
			email.setContent( message, "text/html" );  // email.setText( message );
			email.setFrom( new InternetAddress( this.mSender ) );
			
			// For some reason I can't get setRecipients/addRecipients to send to any other email addresses other than the first one in the list!!!
			for( InternetAddress a : InternetAddress.parse( this.m_pAddresses ) ) {
				System.out.println("Sending email to " + a.getAddress() );
				email.setRecipient( Message.RecipientType.TO, a );
				Transport.send(email);
			}
		} catch( Exception e ) {
			System.out.println( "Email failed: " + e.getMessage() );
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private Session _getSession() {
		if( this.mSession == null ) {
			// Setup mail server
			Properties properties = System.getProperties();
			properties.setProperty( "mail.smtp.host", this.mMailServer );
			//properties.setProperty("mail.user", "myuser");
			//properties.setProperty("mail.password", "mypwd");
			this.mSession= Session.getDefaultInstance( properties );
		}
		return this.mSession;
	}
}
