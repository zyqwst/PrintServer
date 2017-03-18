/**
 * 
 */
package com.albert.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * @ClassName: XmlUtil
 * @Description:
 * @author albert
 * @date 2017年3月17日 下午4:19:49
 * 
 */
public class XmlUtil {
	/**
	 * xml文件转换为字符串
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static String xmlFile2str(String filePath)throws Exception{  
        //创建SAXReader对象  
        SAXReader reader = new SAXReader();  
        //读取文件 转换成Document  
        Document document = reader.read(new File(filePath));  
        //document转换为String字符串  
        String documentStr = document.asXML();  
        return documentStr;
    }  
	
	public static byte[] toByteArray (Object obj) {      
        byte[] bytes = null;      
        ByteArrayOutputStream bos = new ByteArrayOutputStream();      
        try {        
            ObjectOutputStream oos = new ObjectOutputStream(bos);         
            oos.writeObject(obj);        
            oos.flush();         
            bytes = bos.toByteArray ();      
            oos.close();         
            bos.close();        
        } catch (IOException ex) {        
            ex.printStackTrace();   
        }      
        return bytes;    
    } 
	
	 public static InputStream str2InputStream(String xmlStr)throws Exception{  
		 	InputStream in = new ByteArrayInputStream(xmlStr.getBytes());
	        return in;
	    }  
	
	public static void main(String[] args) throws Exception {
		String x = xmlFile2str(Thread.currentThread().getContextClassLoader().getResource("").getPath() +"test.jrxml");
	}
}