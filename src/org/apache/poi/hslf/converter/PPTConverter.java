package org.apache.poi.hslf.converter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hwpf.converter.HtmlDocumentFacade;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import cc.mask.utils.StringUtil;

public class PPTConverter {
	
	private HtmlDocumentFacade htmlDocumentFacade;
	
	private Element window;
	
	private Element topbar;
	
	private Element info;
	
	private Element outline;
	
	private Element page;
	
	private Element ul;
	
	/**
	 * 
	 * 
	 * 
	 * test
	 * 
	 * 
	 * 
	 * @param args
	 * @throws IOException
	 * @throws TransformerException
	 */
	public static void main(String[] args) throws IOException, TransformerException {
		PPTConverter.convert("c:/poi/test.ppt","c:/poi/output/ppt.html");
	}
	
	public static void convert(String filePath, String output) throws IOException, TransformerException{
		
		PPTConverter converter = new PPTConverter();
		converter.init();
		File pptFile = new File(filePath);
		if(!isPPt(pptFile)){
			return;
		}
		
		try {
			
			converter.process(pptFile,output);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		converter.saveAsHtml(output, converter.htmlDocumentFacade.getDocument());
	}
	/**
	 * convert ppt to '.png' file and generate '.html' code.
	 * @param pptFile 
	 * @param output, html save path;
	 * @throws Exception
	 */
	private void process(File pptFile, String output) throws Exception{
		FileInputStream is = new FileInputStream(pptFile);
		SlideShow ppt = new SlideShow(is);
		is.close();
		Dimension pgsize = ppt.getPageSize();
		org.apache.poi.hslf.model.Slide[] slide = ppt.getSlides();
		
//		List<String> imgList = new ArrayList<String>(slide.length);
		
		this.info.setTextContent(StringUtil.getFileName(pptFile.getPath(), true));
		
		for (int i = 0; i < slide.length; i++) {
			
			
			addSlideTitle(slide[i]);
			
			TextRun[] truns = slide[i].getTextRuns();
			for (int k = 0; k < truns.length; k++) {
				
				RichTextRun[] richTexts = truns[k].getRichTextRuns();
				for (int l = 0; l < richTexts.length; l++) {
					
					String fontName = richTexts[l].getFontName();
//					rtruns[l].setFontIndex(1);
//					System.out.println(fontName);
					// POI bug？？？
					if ( isTrueType(fontName) )
						richTexts[l].setFontName("宋体");
					else
						richTexts[l].setFontName(fontName);
				}
			}
			BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = img.createGraphics();
			
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            
            graphics.setColor(Color.white);
            graphics.clearRect(0, 0, pgsize.width, pgsize.height);
            
			
			slide[i].draw(graphics);
			String imgName = StringUtil.getFileName(pptFile.getPath(),false) + "_" + (i + 1) + ".png";
			String imgPath = StringUtil.getFilePath(output) + "/images/";
			if(i == 0){
				new File(imgPath).mkdir();
			}
			FileOutputStream out = new FileOutputStream(imgPath + imgName );
			javax.imageio.ImageIO.write(img, "png", out);
			out.close();
			
			
			addSlide(imgPath + imgName, slide[i].getSlideNumber());
			
			
		}
	}
	
	
	/**
	 *  generate outline;
	 * @param slide
	 */
	private void addSlideTitle(org.apache.poi.hslf.model.Slide slide){
		
		String title = StringUtil.isEmpty(slide.getTitle()) ? 
				slide.getSlideNumber() + ": Untitled" : slide.getSlideNumber() + ": " + slide.getTitle();
		
		Element list = htmlDocumentFacade.createListItem();
		
		Element a = htmlDocumentFacade.createHyperlink("#link"+slide.getSlideNumber());
		a.setTextContent(title);
		
		list.appendChild(a);
		this.ul.appendChild(list);
	}
	/**
	 * generate slide, block style
	 * @param imagePath
	 * @param index
	 */
	private void addSlide(String imagePath, int index){
		Element slideBlock = htmlDocumentFacade.createBlock();
		slideBlock.setAttribute("class", "slide");
		//anchor　pointer
		Element link = htmlDocumentFacade.createBookmark("link" + index);
		
		Element img = htmlDocumentFacade.createImage(imagePath);
//		img.setAttribute("style", "position:relative;top:15%;");
		
		slideBlock.appendChild(link);
		slideBlock.appendChild(img);
		
		this.page.appendChild(slideBlock);
		
	}
	
	
	private void init() {
		try {
			
			// build document
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//			DOMImplementation domImpl = builder.getDOMImplementation();
//			DocumentType doctype = domImpl.createDocumentType("", "-//W3C//DTD HTML 4.01 Transitional//EN", "http://www.w3.org/TR/html4/strict.dtd");
//			Document document = domImpl.createDocument(null, null, doctype);
			Document document = builder.newDocument();
			
			htmlDocumentFacade = new HtmlDocumentFacade(document);
			
			//glob layout
			window = htmlDocumentFacade.createBlock();
			window.setAttribute("id", "window");
			info = htmlDocumentFacade.createBlock();
			info.setAttribute("id", "info");
			outline = htmlDocumentFacade.createBlock();
			outline.setAttribute("id", "outline");
			page = htmlDocumentFacade.createBlock();
			page.setAttribute("id", "page");
			
			
			//outline layout
			ul = htmlDocumentFacade.createUnorderedList();
			outline.appendChild(ul);
			
			window.appendChild(info);
			window.appendChild(outline);
			window.appendChild(page);
			
			htmlDocumentFacade.getBody().appendChild(window);
			
/*			DocumentType docType = htmlDocumentFacade.getDocument().getDoctype();
			
			System.out.println("docType :    -------  " + docType);*/
			
			setCommonStyle(htmlDocumentFacade.getDocument());
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	private void saveAsHtml(String output, org.w3c.dom.Document document) throws IOException, TransformerException{
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
	
	private static boolean isPPt(File file){
		if(!file.canRead())
			return false;
		int sep = file.getPath().lastIndexOf(".");
		if("ppt".equals(file.getPath().substring(sep + 1, file.getPath().length())))
			return true;
		
		return false;
	}
	
	public static boolean isTrueType(String fontName){
		String[] trueType = new String[]{
			"Tahoma","Times New Roman","Calibri","Arial"};
		for(String type : trueType){
			if(type.equals(fontName))
				return true;
		}
		return false;
	}
	
	private void setCommonStyle(Document document){
		Element styleSheet = (Element)document.getElementsByTagName("style").item(0);
		if(styleSheet == null)
			return;
		
		String sep = "\n";
		
		StringBuffer sb = new StringBuffer();
		sb.append(sep);
		sb.append("html{height: 100%;overflow-y:hidden;}");
		sb.append(sep);
		sb.append("body{height:100%;overflow-y: hidden;margin:0; background:#bdc2cd}");
		sb.append(sep);
		sb.append("#window{min-width: 800px;height:100%;}");
		sb.append(sep);
//		sb.append("#topbar{min-height:24px;}");
//		sb.append(sep);
		sb.append("#info{font-size: 24px;font-weight: 800;border-bottom:2px solid gray;height:5%; background:#eee;padding-left:5px;}");
		sb.append(sep);
		sb.append("#outline{float:left;height:95%;width:20%;overflow-y: auto; overflow-x:hidden;background:#fff; " +
				"margin-right:3%; -moz-box-shadow: 4px 4px 12px #2b2b2b;-webkit-box-shadow: 4px 4px 12px #2b2b2b;" +
				"box-shadow: 4px 4px 12px #2b2b2b;}");
		sb.append(sep);
		sb.append("#outline ul li {list-style:none;line-height: 25px;}");
		sb.append(sep);
		sb.append("#outline ul li a{text-decoration:none;white-space:nowrap;text-overflow:ellipsis;}");
		sb.append(sep);
		sb.append("#page{float:left;overflow-y: auto;overflow-x:hidden;height:95%;width:77%;margin-right:-20%;background:#bdc2cd; text-align:center;}");
		sb.append(sep);
		sb.append("#page div{width:100%;height:100%;}");
		sb.append(sep);
		styleSheet.setTextContent(sb.toString());
		
	}
	

}
