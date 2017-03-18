/**
 * 
 */
package com.albert.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.albert.domain.ResponseEntity;
import com.albert.utils.XmlUtil;
import com.google.gson.Gson;

/**
 * @ClassName: BaseServlet
 * @Description:
 * @author albert
 * @date 2017年3月17日 下午9:34:23
 * 
 */
public class BaseServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -496209608851406742L;

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String methodName = req.getParameter("method");
		if (methodName == null || methodName.isEmpty()) {
			write(resp, ResponseEntity.failed("请求参数不可为空"));
		}
		Class c = this.getClass();
		Method method = null;
		try {
			method = c.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
		} catch (Exception e) {
			write(resp, ResponseEntity.failed("没有找到" + methodName + "方法，请检查该方法是否存在"));
			throw new RuntimeException("没有找到" + methodName + "方法，请检查该方法是否存在");
		}

		try {
			method.invoke(this, req, resp);// 反射调用方法
		} catch (Exception e) {
			write(resp, ResponseEntity.failed("没有找到" + methodName + "方法，请检查该方法是否存在"));
			throw new RuntimeException(e);
		}

	}

	public void write(HttpServletResponse resp, ResponseEntity r) throws IOException {
		resp.setContentType("application/json;charset=utf-8");
		Gson gson = new Gson();
		String json = gson.toJson(r);
		PrintWriter out = resp.getWriter();
		out.write(json);
		out.flush();
		out.close();
	}
}
