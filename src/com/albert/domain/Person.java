/**
 * 
 */
package com.albert.domain;

import java.util.Date;

/** 
* @ClassName: Person 
* @Description: 
* @author albert
* @date 2017年3月18日 上午9:09:26 
*  
*/
public class Person implements EntityBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2090200164895843531L;
	private String code;
	private String name;
	private String address;
	private Date creDate;
	/**
	 * 
	 */
	public Person() {
	}
	public Person(String code, String name, String address, Date creDate) {
		super();
		this.code = code;
		this.name = name;
		this.address = address;
		this.creDate = creDate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Person(String name){
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Date getCreDate() {
		return creDate;
	}
	public void setCreDate(Date creDate) {
		this.creDate = creDate;
	}
}
