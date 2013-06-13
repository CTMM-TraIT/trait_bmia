package gov.nih.nci.nbia.beans.security;

import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 *  Helper class for Shibboleth Attributes 
 */ 
public class ShibbolethMap extends HashMap<String,String>
{
	private static final long serialVersionUID = 4026469448457968111L;
	
	// Current default Shibboleth attributes. 
	// Not all might be supported. 
	final static String SHIB_PERSISTENT_ID  = "persistent-id"; 
	final static String SHIB_USER           = "Shib-user"; 
	final static String SHIB_DISPLAYNAME    = "Shib-displayName";
	final static String SHIB_SURNAME        = "Shib-surName";
	final static String SHIB_COMMONNAME     = "Shib-commonName";
	final static String SHIB_GIVENNAME      = "Shib-givenName";
	final static String SHIB_EDUPERSONPN    = "Shib-eduPersonPN";
	final static String SHIB_EMAIL          = "Shib-email";
	final static String SHIB_HOMEORG        = "Shib-HomeOrg";
	final static String SHIB_UID            = "Shib-uid"; 
	final static String SHIB_USERSTATUS     = "Shib-userStatus";
	final static String SHIB_VONAME         = "Shib-voName";
	final static String SHIB_MEMBEROF       = "Shib-memberOf"; 
	
	/* names of the SAML attributes to display */
	private static String[] shibAttributesNames = 
	{
		SHIB_PERSISTENT_ID,
        SHIB_USER,
        SHIB_DISPLAYNAME,
        SHIB_SURNAME,
        SHIB_COMMONNAME,
        SHIB_GIVENNAME,
        SHIB_EDUPERSONPN,
        SHIB_EMAIL,
        SHIB_HOMEORG,
        SHIB_UID,
        SHIB_USERSTATUS,
        SHIB_VONAME,
        SHIB_MEMBEROF
    };

	public static String[] getDefaultAttributeNames()
	{
		return shibAttributesNames; 
	}
	
	// === instance === 
	
	public ShibbolethMap()
	{
		super(); 
	}
	
	public ShibbolethMap(Map<String,String> attrs)
	{
		super(attrs); 
	}
	
	public String getShibUid()
	{
		return this.get(SHIB_UID); 
	}
	
	public String getShibUser()
	{
		return this.get(SHIB_USER); 
	}
	
	public String getShibPersistantID()
	{
		return this.get(SHIB_PERSISTENT_ID); 
	}
	
	public void parseShibVars(HttpServletRequest request, boolean printAll)
    {
        ShibbolethMap shibMap=this; 
         
        String attrNames[]=ShibbolethMap.getDefaultAttributeNames(); 
        
        // first parse Attributes: 
        for (int i=0; i<attrNames.length; i++)
        {
            String name=attrNames[i];
            Object value=request.getAttribute(attrNames[i]); 
            if (value!=null)
            { 
                infoPrintf(" - [REQ:SHIB:ATTR]: Found Shibboleth Attribute:%s='%s'\n",name,value); 
                shibMap.put(name,value.toString()); 
            }
            else
            {
                infoPrintf(" - [REQ:SHIB:ATTR]: ***NOT FOUND: %s\n",name); 
            }
        }
        
        // alternately parse Headers,check "AJP_" prefix:
        for (int i=0; i<attrNames.length; i++)
        {
            String name=attrNames[i];
            // Headers are prefixed with "AJP_" and UNSAFE! 
            String value=request.getHeader("AJP_"+attrNames[i]); 
            
            if (value!=null)
            {
                if (shibMap.get(name)==null) 
                {
                    warnPrintf(" - [REQ:SHIB:HEADR]: Using (AJP_)Shibboleth *HEADER*:%s='%s'\n",name,value); 
                    shibMap.put(name,value.toString());
                }
                else
                {
                    warnPrintf(" - [REQ:SHIB:HEADR]: Skipping (AJP_)Shibboleth *HEADER*:%s='%s'\n",name,value); 
                }               
            }
        }
        
        // now all: 
        if (printAll)
        {
            infoPrintf(" --- ALL ---\n");
            Enumeration<?> hdrs = request.getHeaderNames();
            while ( hdrs.hasMoreElements() )
            {
                String attr_name = (String) hdrs.nextElement();
                String attr_val  = request.getHeader(attr_name);
                debugPrintf(" - [REQ:HDR] %s='%s'\n",attr_name,attr_val); 
            }
             
            Enumeration<?> attributes = request.getAttributeNames();
            while ( attributes.hasMoreElements() )
            {
                String attr_name = (String) attributes.nextElement();
                Object attr_val  = request.getAttribute(attr_name);
                debugPrintf(" - [REQ:ATTR] %s='%s'\n",attr_name,attr_val); 
            }
            
            // check session: 
            HttpSession sess = request.getSession(); 
            
            attributes = sess.getAttributeNames();
            while ( attributes.hasMoreElements() )
            {
                String attr_name = (String) attributes.nextElement();
                Object attr_val  = sess.getAttribute(attr_name);
                debugPrintf(" - [SESS:ATTR] %s='%s'\n",attr_name,attr_val); 
            }
        }
        
        return; 
    }
    
	// =======
	// Logging  
	// =======
	
	static void debugPrintf(String format, Object... args)
    {
        //logger.info("MYINFO::"+formatString(format,args)); 
        System.out.print("SHIB:DEBUG:"+formatString(format,args)); 
    }
	
	static void infoPrintf(String format, Object... args)
    {
        //logger.info("MYINFO::"+formatString(format,args)); 
        System.out.print("SHIB:INFO:"+formatString(format,args)); 
    }
    
	static void warnPrintf(String format, Object... args)
    {
        //logger.warn("MYWARN::"+formatString(format,args));
        System.out.print("SHIB:WARN:"+formatString(format,args));
    }
    
    // PTdB: MyFormatter:
    static String formatString(String format,Object... args)
    {
        // expensive, but for now: 
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format(format, args); 
        String str=formatter.toString();
        
        formatter.close(); 
    	
        return str; 
    }

}
