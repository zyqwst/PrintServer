package com.albert.servlet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.albert.domain.Person;
import com.albert.utils.JasperUtil;

import net.sf.jasperreports.engine.JasperPrint;

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
        	List<Person> list = new ArrayList<>();
        	list.add(new Person("王少汀"));
        	JasperPrint print = JasperUtil.jasperList(path+"test.jrxml", list);
        	
        	writeObject(resp, print);
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void toObject(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ClassNotFoundException{
		InputStream in = req.getInputStream();
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream(); 
		byte[] buff = new byte[in.available()];  
		int rc = 0; 
		while ((rc = in.read(buff, 0, 100)) > 0) { 
		swapStream.write(buff, 0, rc); 
		} 
		byte[] in_b = swapStream.toByteArray(); //in_b为转换之后的结果 
		
		ByteArrayInputStream bis = new ByteArrayInputStream(in_b);
		ObjectInputStream objis = new ObjectInputStream(bis);
		JasperPrint jp = (JasperPrint) objis.readObject();
		System.out.println(jp);
	}
	
}
