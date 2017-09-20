/**
 * 
 */
package com.albert.test;

import org.apache.poi.xwpf.converter.DocxConverter;

/** 
* @ClassName: Test 
* @Description: 
* @author albert
* @date 2017年9月19日 下午4:56:28 
*  
*/
public class Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		DocxConverter.convert("/Users/albert/Desktop/test.docx", "/Users/albert/Desktop/test.html");
	}

}
