/**
 * 
 */
package com.albert.servlet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.albert.domain.ResponseEntity;
import com.google.gson.Gson;

/**
 * @ClassName: BaseServlet
 * @Description: 多个请求多个servlet太过繁琐，只需定义一个子类继承BaseServlet,子类中是各种请求的方法，然后根据request.getParameter("method")调用子类的方法；
 * 				 比如http://ip地址?method=zjd 。那么只需在子类定义zjd方法即可
 * @author albert
 * @date 2017年3月17日 下午9:34:23
 * 
 */
public class BaseServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -496209608851406742L;
	HttpServletRequest req;
	HttpServletResponse resp;
	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException,RuntimeException {
		this.req = req;
		this.resp = resp;
	    String methodName = req.getParameter("method");
		if (methodName == null || methodName.isEmpty()) {
			write(resp, ResponseEntity.failed("请求参数不可为空"));
		}
		Class<? extends BaseServlet> c = this.getClass();
		Method method = null;
		try {
			method = c.getMethod(methodName);
		} catch (Exception e) {
			write(resp, ResponseEntity.failed("没有找到" + methodName + "方法，请检查该方法是否存在"));
			throw new RuntimeException("没有找到" + methodName + "方法，请检查该方法是否存在");
		}

		try {
			method.invoke(this);// 反射调用方法
		} catch (Exception e) {
			write(resp, ResponseEntity.failed("没有找到" + methodName + "方法，请检查该方法是否存在"));
			throw new RuntimeException(e);
		}

	}

	public void write(HttpServletResponse resp, ResponseEntity r) throws IOException {
		resp.setContentType("text/html;charset=utf-8");
		Gson gson = new Gson();
		String json = gson.toJson(r);
		PrintWriter out = resp.getWriter();
		out.write(json);
		out.flush();
		out.close();
	}
	public void writeObject(HttpServletResponse resp, ResponseEntity r) throws IOException{
		ObjectOutputStream oos = new ObjectOutputStream(resp.getOutputStream());
		oos.writeObject(r);
		oos.flush();
		oos.close();
	}
}
