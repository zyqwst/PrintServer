package org.apache.poi.xssf.converter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.converter.HtmlDocumentFacade;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cc.mask.utils.StringUtil;

public class XlsxConverter {
	
	private XSSFWorkbook x;
	private HtmlDocumentFacade htmlDocumentFacade;
	private Element page;
	
	private StringBuilder css = new StringBuilder();
	
	
	
	private XlsxConverter(String filePath, String output) throws IOException, InvalidFormatException, ParserConfigurationException{
		
		OPCPackage op = OPCPackage.open(filePath);
		x = new XSSFWorkbook(op);
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		this.htmlDocumentFacade = new HtmlDocumentFacade(document);
		
		Element window = htmlDocumentFacade.createBlock();
		window.setAttribute("id", "window");
		page = htmlDocumentFacade.createBlock();
		page.setAttribute("id", "page");
		
		window.appendChild(page);
		htmlDocumentFacade.getBody().appendChild(window);
	}
	
	public static void main(String[] args) throws InvalidFormatException, IOException, ParserConfigurationException, TransformerException {
		String name = "test";
		XlsxConverter.convert("c:/poi/" +name+ ".xlsx", "c:/poi/x/" +name+ ".html");
	}
	/**
	 * 
	 * @param filePath
	 * @param output
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static void convert(String filePath, String output) throws InvalidFormatException, IOException, ParserConfigurationException, TransformerException{
		XlsxConverter converter = new XlsxConverter(filePath, output);
		
		Integer sheetNum = converter.x.getNumberOfSheets();
		for(int i = 0; i < sheetNum; i ++){
			XSSFSheet sheet = converter.x.getSheet(converter.x.getSheetName(i));
			String sheetName =  converter.x.getSheetName(i);
			System.out.println("----starting process sheet : " +sheetName);
			// add sheet title
			{
				Element title = converter.htmlDocumentFacade.createHeader2();
				title.setTextContent(sheetName);
				converter.page.appendChild(title);
			}
			
			converter.processSheet(converter.page, sheet);
		}
		
		converter.htmlDocumentFacade.updateStylesheet();
		
		Element style = (Element)converter.htmlDocumentFacade.getDocument().getElementsByTagName("style").item(0);
		
		style.setTextContent(converter.css.append(style.getTextContent()).toString());
		
		converter.saveAsHtml(output, converter.htmlDocumentFacade.getDocument());
	}

	private void processSheet(Element container, XSSFSheet sheet) {
		
		Element table = htmlDocumentFacade.createTable();
		int sIndex = sheet.getWorkbook().getSheetIndex(sheet);
		String sId = "sheet_".concat(String.valueOf(sIndex));
		table.setAttribute("id", sId);
		table.setAttribute("border", "1");
		table.setAttribute("cellpadding", "2");
		table.setAttribute("cellspacing", "0");
		table.setAttribute("style", "border-collapse: collapse;");
		
		// get sheet merge regions
		CellRangeAddress[] ranges  = null;
		{
			int num = sheet.getNumMergedRegions();
			ranges = new CellRangeAddress[num];
			for(int i = 0; i < num ; i ++)
				ranges[i] = sheet.getMergedRegion(i);
		}
		
		css.append("#").append(sId).append(" tr{height:").append(sheet.getDefaultRowHeightInPoints()/28.34).append("cm}\n");
		css.append("#").append(sId).append(" td{width:").append(sheet.getDefaultColumnWidth()*0.21).append("cm}\n");
		
		// cols
		generateColumns(sheet, table);
		
		//rows
		Iterator<Row> rows = sheet.iterator();
		while(rows.hasNext()){
			Row row = rows.next();
			if(row instanceof XSSFRow)
				processRow(table, (XSSFRow)row, sheet, ranges);
		}
		
		container.appendChild(table);
	}
	
	
	/**
	 * generated <code><col><code> tags. 
	 * @param sheet 
	 * @param table container.
	 */
	private void generateColumns(XSSFSheet sheet, Element table) {
		List<CTCols> colsList = sheet.getCTWorksheet().getColsList();
		MathContext mc = new MathContext(3);
		for(CTCols cols : colsList){
			long oldLevel = 1;
			for(CTCol col : cols.getColArray()){
				while(true){
					if(oldLevel == col.getMin()){
						break;
					}
					Element column = htmlDocumentFacade.createTableColumn();
					column.setAttribute("style", "width:2in;");
					table.appendChild(column);
					oldLevel ++;
				}
				Element column = htmlDocumentFacade.createTableColumn();
				String width = new BigDecimal(sheet.getColumnWidth(Long.bitCount(oldLevel))/1440.0, mc ).toString() ;
				column.setAttribute("style", "width:".concat( width ).concat("in;"));
				table.appendChild(column);
				oldLevel ++;
			}
		}
	}
	
	private void processRow(Element table, XSSFRow row, XSSFSheet sheet, CellRangeAddress[] ranges) {
		Element tr = htmlDocumentFacade.createTableRow();
		Iterator<Cell> cells = row.cellIterator();
		if(row.isFormatted()){
			//TODO build row style...
			//row.getRowStyle();
		}
		
		if(row.getCTRow().getCustomHeight())
			tr.setAttribute("style", "height:".concat(String.valueOf(row.getHeightInPoints())).concat("pt;"));
		
		while(cells.hasNext()){
			XSSFCell cell = (XSSFCell)cells.next();
			Element td = htmlDocumentFacade.createTableCell();
			
			CellRangeAddress range = null;
			for(CellRangeAddress temp : ranges)
				if(temp.isInRange(cell.getRowIndex(), cell.getColumnIndex())){
					range = temp;	break;
				}
			
			boolean mergedCell = false;
			if(range != null ){
				if(cell.getRowIndex() == range.getFirstRow() && cell.getColumnIndex() == range.getFirstColumn()){
					//colspan
					if ( range.getFirstColumn() != range.getLastColumn() )
	                    td.setAttribute("colspan", String.valueOf( range.getLastColumn()- range.getFirstColumn() + 1 ) );
					//rowspan
	                if ( range.getFirstRow() != range.getLastRow() )
	                    td.setAttribute("rowspan", String.valueOf( range.getLastRow()- range.getFirstRow() + 1 ) );
	                System.out.println(td);
				}else{
					mergedCell = true;
				}
			}
			
			if(!mergedCell){
				getCellContent(td, (XSSFCell)cell);
				tr.appendChild(td);
			}
			range = null;
		}
		table.appendChild(tr);
	}
	

	private void getCellContent(Element td, XSSFCell cell) {
		
//		System.out.println(cell.getRowIndex() + " : " + cell.getColumnIndex());
		
		Object value ;
		switch(cell.getCellType()){
		case Cell.CELL_TYPE_BLANK : value = "\u00a0";	break;
		case Cell.CELL_TYPE_NUMERIC : value = cell.getNumericCellValue();	break;
		case Cell.CELL_TYPE_BOOLEAN : value =  cell.getBooleanCellValue();	break;
		case Cell.CELL_TYPE_FORMULA : value = cell.getNumericCellValue();	break;
		default : value = cell.getRichStringCellValue();	break;
		}
		if(value instanceof XSSFRichTextString){
			processCellStyle(td, cell.getCellStyle(), (XSSFRichTextString)value);
			td.setTextContent(value.toString());
		}
		else{
			processCellStyle(td, cell.getCellStyle(), null);
			td.setTextContent(value.toString());
		}
	}

	private void processCellStyle(Element td, XSSFCellStyle style, XSSFRichTextString rts) {
		StringBuilder sb = new StringBuilder();
		
		if(rts != null){
			XSSFFont font = rts.getFontOfFormattingRun(1);
			if(font != null){
				sb.append("font-family:").append(font.getFontName()).append(";");
//				sb.append("color:").append(font.getColor() ).append(";");
				sb.append("font-size:").append(font.getFontHeightInPoints()).append("pt;");
				if(font.getXSSFColor()!= null){
					String color = font.getXSSFColor().getARGBHex().substring(2);
					sb.append("color:#").append(color).append(";");
				}
				if(font.getItalic())
					sb.append("font-style:italic;");
				if(font.getBold())
					sb.append("font-weight:").append(font.getBoldweight() ).append(";");
				if(font.getStrikeout()){
					sb.append("text-decoration:underline;");
				}
				
			}
		}
		if(style.getAlignment() != 1){
			switch(style.getAlignment()){
			case 2: sb.append("text-align:").append("center;");	break;
			case 3: sb.append("text-align:").append("right;");	break;
			}
		}
/*		if(style.getBorderBottom() != 0 )
			sb.append("border-bottom:").append(style.getBorderBottom()).append("px;");
		if( style.getBorderLeft() != 0 )
			sb.append("border-left:").append(style.getBorderLeft()).append("px;");
		if(style.getBorderTop() != 0 )
			sb.append("border-top:").append(style.getBorderTop()).append("px;");
		if(style.getBorderRight() != 0 )
			sb.append("border-right:").append(style.getBorderRight()).append("px;");
		if(style.getFillBackgroundXSSFColor()!=null){
			XSSFColor color = style.getFillBackgroundXSSFColor();
		}*/
		
//		System.out.println(style.getFillBackgroundXSSFColor());
		if( style.getFillBackgroundXSSFColor()!= null){
			sb.append("background:#ccc;");
		}
		htmlDocumentFacade.addStyleClass(td, "td", sb.toString());
	}

	/**
	 * @param output
	 * @param document
	 * @throws IOException
	 * @throws TransformerException
	 */
	private void saveAsHtml(String output, org.w3c.dom.Document document) throws IOException, TransformerException{
		
//		check path
		File folder = new File(StringUtil.getFilePath(output));	
		if(!folder.canRead()) 
			folder.mkdirs();
		folder = null;
		
		
		FileWriter out = new FileWriter( output );
        DOMSource domSource = new DOMSource(document );
        StreamResult streamResult = new StreamResult( out );

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        // TODO set encoding from a command argument
        serializer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
        serializer.setOutputProperty( OutputKeys.INDENT, "yes" );
        serializer.setOutputProperty( OutputKeys.METHOD, "html" );
        serializer.setOutputProperty(OutputKeys.STANDALONE , "yes"); 
        serializer.setOutputProperty( OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD HTML 4.01 Transitional//EN"); //
        serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/html4/strict.dtd");
        
        serializer.transform( domSource, streamResult );
        out.close();
	}
	
}
