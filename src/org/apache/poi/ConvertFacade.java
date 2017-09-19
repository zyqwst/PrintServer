/**
 * 
 */
package org.apache.poi;

import org.apache.poi.hslf.converter.PPTConverter;
import org.apache.poi.hssf.converter.XlsConverter;
import org.apache.poi.hwpf.converter.DocConverter;
import org.apache.poi.xssf.converter.XlsxConverter;
import org.apache.poi.xwpf.converter.DocxConverter;

/** 
* @ClassName: ConvertFacade 
* @Description: 转换门面类
* @author albert
* @date 2017年9月18日 下午4:52:00 
*  
*/
public class ConvertFacade {
	public final static String DOC = "doc";
	public final static String DOCX = "docx";
	public final static String XLS = "xls";
	public final static String XLSX = "xlsx";
	public final static String PPT = "ppt";
	
	public static void convert( String filePath, String output  ) throws Exception{
		if(!filePath.contains(".")) throw new Exception("不合法的文件名");
		switch (filePath.substring(filePath.lastIndexOf(".")+1).toLowerCase()) {
		case DOC:
			DocConverter.convert(filePath, output);
			break;
		case DOCX:
			DocxConverter.convert(filePath, output);
			break;
		case XLS:
			XlsConverter.convert(filePath, output);
			break;
		case XLSX:
			XlsxConverter.convert(filePath, output);
			break;
		case PPT:
			PPTConverter.convert(filePath, output);
			break;
		default:
			throw new Exception("不合法的文件");
		}
	}
}
