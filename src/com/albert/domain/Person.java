/**
 * 
 */
package com.albert.domain;

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
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Person(String name){
		this.name = name;
	}
}
