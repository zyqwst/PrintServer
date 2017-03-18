/**
 * 
 */
package com.albert.utils;

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
	private String result;
	private Object params;
	public ResponseEntity() {
	}
	public ResponseEntity(String result,Object params){
		this.result = result;
		this.params = params;
	}
	public static ResponseEntity success(String result,Object params){
		ResponseEntity r = new ResponseEntity(result, params);
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
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public Object getParams() {
		return params;
	}
	public void setParams(Object params) {
		this.params = params;
	}
	
}
