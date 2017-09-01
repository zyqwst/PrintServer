package com.albert.servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.albert.domain.Person;
import com.albert.domain.ResponseEntity;
import com.albert.utils.Cache;
import com.albert.utils.XmlUtil;

/**
 * 
 */

/** 
* @ClassName: PrintServlet 
* @Description: 
* @author albert
* @date 2017年3月17日 下午4:07:31 
*  
*/
@WebServlet(value = "/print")
public class PrintServlet extends BaseServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2163623133927830763L;
	/**
	 * 打印质检单
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	public void zjd() throws ServletException, IOException {
        String path = this.getClass().getClassLoader().getResource("/").getPath();
        try {
        	String xml = XmlUtil.xmlFile2str(path+"demo.jrxml");
        	List<Person> list = new ArrayList<>();
    		list.add(new Person("0001", "albert", "浙江省湖州市", new Date()));
    		list.add(new Person("0001", "albert", "浙江省湖州市", new Date()));
    		list.add(new Person("0001", "albert", "浙江省湖州市", new Date()));
    		list.add(new Person("0002", "albert2", "浙江省湖州市", new Date()));
    		list.add(new Person("0002", "albert2", "浙江省湖州市", new Date()));
    		
    		String json = XmlUtil.domain2JsonFile(list);
    		String key = UUID.randomUUID().toString();
    		System.out.println(key);
    		Cache.Instance().put(key, ResponseEntity.success(xml, json));
        	writeObject(resp, ResponseEntity.success(key));
        } catch (Exception e) {
			e.printStackTrace();
			write(resp,ResponseEntity.failed(e.getMessage()));
		}
	}
	public void printdata() throws IOException {
	    try {
            Object obj = Cache.Instance().get(req.getParameter("key"));
            if(obj==null) throw new Exception("未找到打印数据");
            ResponseEntity r = (ResponseEntity)obj;
            writeObject(resp, r);
        } catch (Exception e) {
            e.printStackTrace();
            writeObject(resp,ResponseEntity.failed(e.getMessage()));
        }
	}
	//...其他方法
}
