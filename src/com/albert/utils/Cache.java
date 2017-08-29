/**
* @{#} Constants.java Created on 2017-8-25 下午3:56:30
*
* Copyright (c) 2017 by SHUANGYI software.
*/
package com.albert.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
/**
* @ClassName: Cache 
* @Description: 模拟ehcache存储
* @author Albert
* @date 2017-8-28 上午8:57:07
 */
public class Cache implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1966019936335308832L;
    private static Map<String,Object> cache;
    
    public static Map<String,Object> Instance(){
        if(cache==null) cache = new HashMap<String, Object>();
        return cache;
    }
    
}
