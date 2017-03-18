/**
 * 
 */
package com.albert.domain;

import java.io.Serializable;

/** 
* @ClassName: ResponseEntity 
* @Description: 
* @author albert
* @date 2017年3月17日 下午9:48:16 
*  
*/
public class ResponseEntity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7346458775767254754L;
	/**1 成功 -1 失败*/
	private Integer status;
	private String msg;
	/**打印模板*/
	private String xml;
	/**打印数据json*/
	private String json;
	public ResponseEntity() {
	}
	public ResponseEntity(String xml,String json){
		this.xml = xml;
		this.json = json;
	}
	public static ResponseEntity success(String xml,String json){
		ResponseEntity r = new ResponseEntity(xml, json);
		r.setStatus(1);
		r.setMsg("OK");
		return r;
	}
	public static ResponseEntity failed(String msg){
		ResponseEntity r = new ResponseEntity();
		r.setStatus(-1);
		r.setMsg(msg);
		return r;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
	public String getJson() {
		return json;
	}
	public void setJson(String json) {
		this.json = json;
	}
	
}
