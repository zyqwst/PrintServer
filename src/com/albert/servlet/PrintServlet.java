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
import com.albert.domain.ResponseEntity;
import com.albert.utils.JasperUtil;
import com.albert.utils.XmlUtil;

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
        	String xml = XmlUtil.xmlFile2str(path+"demo.jrxml");
        	String json = XmlUtil.file2str(path+"param.json");
        	write(resp, ResponseEntity.success(xml, json));
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
