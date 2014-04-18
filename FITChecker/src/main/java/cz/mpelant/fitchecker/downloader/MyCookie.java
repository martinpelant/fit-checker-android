package cz.mpelant.fitchecker.downloader;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.Serializable;
import java.util.Date;

public class MyCookie implements Serializable{
	private static final long serialVersionUID = -423988528614135363L;
	private String name, value, domain, path, comment;
	private Date date;
	private int version;
	private boolean secure;
	
	public MyCookie(Cookie cookie){
		domain=cookie.getDomain();
		name=cookie.getName();
		value=cookie.getValue();
		path=cookie.getPath();
		comment=cookie.getComment();
		date=cookie.getExpiryDate();
		version=cookie.getVersion();
		secure=cookie.isSecure();
	}
	
//	private void setCustom(){
//		if(name.contains("DokuWiki")){
//			name = "";
//			value="";
//		}
//		else{
//			name="";
//			value="";
//		}
//	}
	public MyCookie(String name, String value) {
		this.name=name;
		this.value=value;
	}
	public Cookie getCookie(){
		BasicClientCookie cookie= new BasicClientCookie(name, value);
		cookie.setComment(comment);
		cookie.setDomain(domain);
		cookie.setExpiryDate(date);
		cookie.setPath(path);
		cookie.setSecure(secure);
		cookie.setVersion(version);
		return cookie;
	}
	
	
	@Override
	public String toString(){
		return name + "-" + value + ":::::"+name+";"+value+";"+domain+";"+path+";"+comment+";"+date+";"+version+";"+secure;
	}
}
