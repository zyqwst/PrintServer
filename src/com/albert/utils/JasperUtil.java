package com.albert.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.albert.domain.EntityBase;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * jasperSoft报表生成 调用工具
 * 
 * @author Albert
 * 
 */
public class JasperUtil {

   
    public static <T extends EntityBase> JasperPrint jasperList(String fileName,
            List<T> list) throws JRException {
        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        InputStream inReport = null;
        try {
            inReport = new FileInputStream(fileName);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            jasperReport = JasperCompileManager.compileReport(inReport);
            jasperPrint = JasperFillManager.fillReport(jasperReport, null,
                    new JRBeanCollectionDataSource(list));
        } catch (JRException e) {
            e.printStackTrace();
        }
       return jasperPrint;
    }

  
    /**
     * 数据库源
     * 
     * @param fileName
     * @param list
     * @throws JRException
     */
    public static JasperPrint jasperDb(String fileName, Map<String, Object> params,
            Connection conn) throws JRException {
        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        InputStream inReport = null;
        try {
            inReport = new FileInputStream(fileName);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            jasperReport = JasperCompileManager.compileReport(inReport);
            jasperPrint = JasperFillManager.fillReport(jasperReport, params,
                    conn);
        } catch (JRException e) {
            e.printStackTrace();
        }
        return jasperPrint;
    }

}
