package cc.mask.utils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class HtmlUtil {
	
	
	private Element styleSheet = null;
	public Element window = null;
	public Element body = null;
	public Element script = null;
	public Element html = null;
	public Document document = null;
	
	
	public HtmlUtil(){
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void createHTML(){
		html = document.createElement("html");
		
		Element head = document.createElement("head");
		styleSheet = document.createElement("style");
		styleSheet.setAttribute( "type", "text/css" );
		script = document.createElement("script");
		script.setAttribute("language", "text/javascript");
		head.appendChild(script);
		head.appendChild(styleSheet);
		html.appendChild(head);
		
		body = document.createElement("body");
		window = createBlock();
		body.appendChild(window);
		html.appendChild(body);
		
		document.appendChild(html);
		
	}
	
	
	public void addClass(Element ele, String className, String style){
		String oldClazz = ele.getAttribute( "class" );
	    
		
		
		ele.setAttribute("class", isEmpty(oldClazz) ? className : className +" " + oldClazz);
		
	}
	
	public Element createBlock(){
		return document.createElement("div");
	}
	
	public void createStyle(){
		
		
		
	}
	
	public static boolean isEmpty(String str){
		return str == null || str.trim().length() == 0;
	}
	
}
