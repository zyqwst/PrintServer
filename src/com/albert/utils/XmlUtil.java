/**
 * 
 */
package com.albert.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import com.albert.domain.EntityBase;
import com.albert.domain.Person;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	/**
	 * 读取文件内容到String
	 * @param filePath
	 * @return
	 */
	public static String file2str(String filePath){
		 StringBuilder result = new StringBuilder();
	        try{
	            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
	            String s = null;
	            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
	                result.append(System.lineSeparator()+s);
	            }
	            br.close();    
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	        return result.toString();
	}
	/**
	 * 对象转换为byte[]
	 * @param obj
	 * @return
	 */
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
	/**
	 * list 转换为json 字符串
	 * @param list
	 */
	public static <T extends EntityBase> String domain2JsonFile(List<T> list){
		Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd").create();  
		String json = gson.toJson(list);
		return json;
	}
	/**
	 * 字符串写入到文件
	 * @param str
	 * @param path
	 * @throws Exception 
	 */
	public static void str2File(String str,String path) throws Exception{
		File file = new File(path);
		if(file.isDirectory()){
			throw new Exception("文件名是路径");
		}
		file.createNewFile();
		OutputStream out = new FileOutputStream(file);
		out.write(str.getBytes());
		out.close();
	}
	public static void main(String[] args) throws Exception {
		List<Person> list = new ArrayList<>();
		list.add(new Person("0001", "albert", "浙江省湖州市", new Date()));

		list.add(new Person("0001", "albert", "浙江省湖州市", new Date()));
		list.add(new Person("0001", "albert", "浙江省湖州市", new Date()));
		list.add(new Person("0002", "albert2", "浙江省湖州市", new Date()));
		list.add(new Person("0002", "albert2", "浙江省湖州市", new Date()));
		
		String json = domain2JsonFile(list);
		System.out.println(System.getProperty("user.dir"));
		str2File(json, System.getProperty("user.dir")+"/src/param.json");
	}
}