package gov.nih.nci.nbia.jms;

import java.io.Serializable;

public class ImageDeletionMessage implements Serializable{
	private static final long serialVersionUID = -8320007034599986923L;
	
	private String emailAddress;
	private String userName;
		  
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
