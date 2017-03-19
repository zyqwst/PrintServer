package com.albert.servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.albert.domain.Person;
import com.albert.domain.ResponseEntity;
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
	public void zjd(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        	write(resp, ResponseEntity.success(xml, json));
        } catch (Exception e) {
			e.printStackTrace();
			write(resp,ResponseEntity.failed(e.getMessage()));
		}
	}
	//...其他方法
}
