/**
 * 
 */
package com.albert.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Part;

import org.apache.poi.ConvertFacade;
import org.apache.poi.hwpf.converter.DocConverter;

import com.albert.domain.ResponseEntity;

/**
 * @ClassName: PoiServlet
 * @Description:
 * @author albert
 * @date 2017年9月19日 下午12:41:36
 * 
 */
@WebServlet(value = "/poi")
@MultipartConfig
public class PoiServlet extends BaseServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7890806721814771129L;
	private static final String TMP_NAME = "poicache";
	public void convert() throws Exception {
		System.out.println(req.getCharacterEncoding());
		String savePath = req.getServletContext().getRealPath("/uploadFile");
		File dir = new File(savePath);
		if(!dir.exists()){
			dir.mkdirs();
		}
		Collection<Part> parts = req.getParts();
		if (parts.size() == 1) {
			Part part = req.getPart("file");
			String header = part.getHeader("Content-Disposition");
			// 获取文件名
			String fileName = getFileName(header);
			// 把文件写到指定路径
			part.write(savePath + File.separator + fileName);
			String htmlFile = savePath+File.separator +TMP_NAME+".html";
			ConvertFacade.convert(savePath + File.separator + fileName,htmlFile);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(htmlFile))); //这里可以控制编码  
            StringBuffer sb = new StringBuffer();  
            String line = null;  
            while((line = br.readLine()) != null) {  
                sb.append(line);  
            }  
			
			write(resp, ResponseEntity.success(sb.toString()));
		}
	}

	public String getFileName(String header) {
		return TMP_NAME+header.substring(header.lastIndexOf("."),header.length()-1);
	}
}
