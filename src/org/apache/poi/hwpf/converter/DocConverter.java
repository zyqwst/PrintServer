package org.apache.poi.hwpf.converter;

import static org.apache.poi.hwpf.converter.AbstractWordUtils.TWIPS_PER_INCH;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.FontReplacer.Triplet;
import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.ListLevel;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.Bookmark;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.OfficeDrawing;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Converts Word files (95-2007) into HTML files.
 * <p>
 * This implementation doesn't create images or links to them. This can be
 * changed by overriding {@link #processImage(Element, boolean, Picture)}
 * method.
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class DocConverter extends CustomerAbsConverter
{
	/**
	 * 
	 * 
	 * 
	 * 
	 * test
	 * 
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String filePath = "c:\\poi\\test.doc";
//		String filePath = "c:\\poi\\××××内部文档保密系统20120425 v4.1 [秘密]1.doc";
		String output = "c:/poi/output/test.html";
		
		DocConverter.convert(filePath, output);
		
		
	}
	
	
	/** 文档图片集 */
    PicturesTable picstab = null;
    /** added pageContainer */
    static Element pageContainer = null;
    
    private static String output;
    
    private static String fileName;
    /** 转换文档 */
    public static String convert( String filePath, String output){
    	
		HtmlDocumentFacade facade = null;
		
//		File docFile = new File(docPath);
		try {
			facade = new HtmlDocumentFacade(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
			
			String s1 = "background-color:gray"	;
			facade.addStyleClass(facade.body, "body", s1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
       if(AbstractWordUtils.isEmpty(filePath) || AbstractWordUtils.isEmpty(output)){
    	   System.err.println("docPath OR output is empty. >>!quit");
    	   return "";
       }
       	
        System.out.println( "Converting " + filePath );
        System.out.println( "Saving output to " + output );
        
        DocConverter.output = output.substring(0, output.lastIndexOf(".html")).concat(File.separator).concat("images").concat(File.separator);
        // get fileName
        
        new File(DocConverter.output).mkdirs();
        
        try{
        	Document doc = null;
        	if(facade == null)
        		doc = DocConverter.process( new File( filePath ) );
        	else{
        		Document document = facade.getDocument();
        		Element window = document.createElement("div");
        		Element center = document.createElement("center");
        		center.appendChild(window);
    			facade.addStyleClass(window, "window", "border:2px solid green;width:800px!important; margin:0 auto; background-color:#fff; text-align:left;");
    			facade.body.appendChild(center);
    			pageContainer = window;
    			
        		doc = DocConverter.process(new File(filePath), facade);
        	}

            FileWriter out = new FileWriter( output );
            DOMSource domSource = new DOMSource( doc );
            StreamResult streamResult = new StreamResult( out );

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            // TODO set encoding from a command argument
            serializer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
            serializer.setOutputProperty( OutputKeys.INDENT, "yes" );
            serializer.setOutputProperty( OutputKeys.METHOD, "html" );
            serializer.transform( domSource, streamResult );
            out.close();
        }
        catch ( Exception e ){
            e.printStackTrace();
        }
        return "";
    }
	
	
    static Document process( File docFile ) throws Exception{
    	
        final HWPFDocument wordDocument = new HWPFDocument(new FileInputStream(docFile));
        DocConverter converter = new DocConverter( DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument() );
        converter.processDocument( wordDocument );
        return converter.getDocument();
    }
    
    static Document process(File docFile,HtmlDocumentFacade htmlDocumentFacade ) throws Exception{
    	
    	final HWPFDocument wordDocument = new HWPFDocument(new FileInputStream(docFile));
    	DocConverter converter = new DocConverter(htmlDocumentFacade);
    	converter.processDocument(wordDocument);
    	return converter.getDocument();
    }
    
    
    @Override
    public void processDocument(HWPFDocument wordDocument) {
    	 if(wordDocument.getPicturesTable().getAllPictures().size() > 0){
    		 this.picstab = wordDocument.getPicturesTable();
         }
    	super.processDocument(wordDocument);
    }

    
    
    
    
    /**
     * Holds properties values, applied to current <tt>p</tt> element. Those
     * properties shall not be doubled in children <tt>span</tt> elements.
     */
    private static class BlockProperies
    {
        final String pFontName;
        final int pFontSize;

        public BlockProperies( String pFontName, int pFontSize )
        {
            this.pFontName = pFontName;
            this.pFontSize = pFontSize;
        }
    }
    
    private static final POILogger logger = POILogFactory.getLogger( DocConverter.class );

    private static String getSectionStyle( Section section )
    {
        float leftMargin = section.getMarginLeft() / TWIPS_PER_INCH;
        float rightMargin = section.getMarginRight() / TWIPS_PER_INCH;
        float topMargin = section.getMarginTop() / TWIPS_PER_INCH;
        float bottomMargin = section.getMarginBottom() / TWIPS_PER_INCH;

        String style = "margin: " + topMargin + "in " + rightMargin + "in " + bottomMargin + "in " + leftMargin + "in;";

        if ( section.getNumColumns() > 1 )
        {
            style += "column-count: " + ( section.getNumColumns() ) + ";";
            if ( section.isColumnsEvenlySpaced() )
            {
                float distance = section.getDistanceBetweenColumns() / TWIPS_PER_INCH;
                style += "column-gap: " + distance + "in;";
            }
            else
            {
                style += "column-gap: 0.25in;";
            }
        }
        return style;
    }



    private final Stack<BlockProperies> blocksProperies = new Stack<BlockProperies>();

    private final HtmlDocumentFacade htmlDocumentFacade;

    private Element notes = null;

    /**
     * Creates new instance of {@link DocConverter}. Can be used for
     * output several {@link HWPFDocument}s into single HTML document.
     * 
     * @param document
     *            XML DOM Document used as HTML document
     */
    public DocConverter( Document document )
    {
        this.htmlDocumentFacade = new HtmlDocumentFacade( document );
    }

    public DocConverter( HtmlDocumentFacade htmlDocumentFacade )
    {
        this.htmlDocumentFacade = htmlDocumentFacade;
    }

    @Override
    protected void afterProcess()
    {
        if ( notes != null )
            htmlDocumentFacade.getBody().appendChild( notes );

        htmlDocumentFacade.updateStylesheet();
    }

    public Document getDocument()
    {
        return htmlDocumentFacade.getDocument();
    }

    @Override
    protected void outputCharacters( Element pElement,
            CharacterRun characterRun, String text )
    {
        Element span = htmlDocumentFacade.document.createElement( "span" );
        pElement.appendChild( span );

        StringBuilder style = new StringBuilder();
        BlockProperies blockProperies = this.blocksProperies.peek();
        Triplet triplet = getCharacterRunTriplet( characterRun );

        if ( WordToHtmlUtils.isNotEmpty( triplet.fontName )
                && !WordToHtmlUtils.equals( triplet.fontName,
                        blockProperies.pFontName ) )
        {
            style.append( "font-family:" + triplet.fontName + ";" );
        }
        if ( characterRun.getFontSize() / 2 != blockProperies.pFontSize )
        {
            style.append( "font-size:" + characterRun.getFontSize() / 2 + "pt;" );
        }
        if ( triplet.bold )
        {
            style.append( "font-weight:bold;" );
        }
        if ( triplet.italic )
        {
            style.append( "font-style:italic;" );
        }

        WordToHtmlUtils.addCharactersProperties( characterRun, style );
        if ( style.length() != 0 )
            htmlDocumentFacade.addStyleClass( span, "s", style.toString() );

        Text textNode = htmlDocumentFacade.createText( text );
        span.appendChild( textNode );
    }

    @Override
    protected void processBookmarks( HWPFDocument wordDocument,
            Element currentBlock, Range range, int currentTableLevel,
            List<Bookmark> rangeBookmarks )
    {
        Element parent = currentBlock;
        for ( Bookmark bookmark : rangeBookmarks )
        {
            Element bookmarkElement = htmlDocumentFacade
                    .createBookmark( bookmark.getName() );
            parent.appendChild( bookmarkElement );
            parent = bookmarkElement;
        }

        if ( range != null )
            processCharacters( wordDocument, currentTableLevel, range, parent );
    }

    @Override
    protected void processDocumentInformation(
            SummaryInformation summaryInformation )
    {
        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getTitle() ) )
            htmlDocumentFacade.setTitle( summaryInformation.getTitle() );

        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getAuthor() ) )
            htmlDocumentFacade.addAuthor( summaryInformation.getAuthor() );

        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getKeywords() ) )
            htmlDocumentFacade.addKeywords( summaryInformation.getKeywords() );

        if ( WordToHtmlUtils.isNotEmpty( summaryInformation.getComments() ) )
            htmlDocumentFacade
                    .addDescription( summaryInformation.getComments() );
    }

    @Override
    public void processDocumentPart( HWPFDocument wordDocument, Range range )
    {
        super.processDocumentPart( wordDocument, range );
        afterProcess();
    }

    @Override
    protected void processDrawnObject( HWPFDocument doc,
            CharacterRun characterRun, OfficeDrawing officeDrawing,
            String path, Element block )
    {
        Element img = htmlDocumentFacade.createImage( path );
        block.appendChild( img );
    }

    @Override
    protected void processEndnoteAutonumbered( HWPFDocument wordDocument,
            int noteIndex, Element block, Range endnoteTextRange )
    {
        processNoteAutonumbered( wordDocument, "end", noteIndex, block,
                endnoteTextRange );
    }

    @Override
    protected void processFootnoteAutonumbered( HWPFDocument wordDocument,
            int noteIndex, Element block, Range footnoteTextRange )
    {
        processNoteAutonumbered( wordDocument, "foot", noteIndex, block,
                footnoteTextRange );
    }

    @Override
    protected void processHyperlink( HWPFDocument wordDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String hyperlink )
    {
        Element basicLink = htmlDocumentFacade.createHyperlink( hyperlink );
        currentBlock.appendChild( basicLink );

        if ( textRange != null )
            processCharacters( wordDocument, currentTableLevel, textRange,
                    basicLink );
    }

    @SuppressWarnings("deprecation")
	protected void processImage( Element currentBlock, boolean inlined,
            Picture picture, String imageSourcePath )
    {
        final int aspectRatioX = picture.getHorizontalScalingFactor();
        final int aspectRatioY = picture.getVerticalScalingFactor();

        StringBuilder style = new StringBuilder();

        final float imageWidth;
        final float imageHeight;

        final float cropTop;
        final float cropBottom;
        final float cropLeft;
        final float cropRight;

        if ( aspectRatioX > 0 )
        {
            imageWidth = picture.getDxaGoal() * aspectRatioX / 1000
                    / TWIPS_PER_INCH;
            cropRight = picture.getDxaCropRight() * aspectRatioX / 1000
                    / TWIPS_PER_INCH;
            cropLeft = picture.getDxaCropLeft() * aspectRatioX / 1000
                    / TWIPS_PER_INCH;
        }
        else
        {
            imageWidth = picture.getDxaGoal() / TWIPS_PER_INCH;
            cropRight = picture.getDxaCropRight() / TWIPS_PER_INCH;
            cropLeft = picture.getDxaCropLeft() / TWIPS_PER_INCH;
        }

        if ( aspectRatioY > 0 )
        {
            imageHeight = picture.getDyaGoal() * aspectRatioY / 1000
                    / TWIPS_PER_INCH;
            cropTop = picture.getDyaCropTop() * aspectRatioY / 1000
                    / TWIPS_PER_INCH;
            cropBottom = picture.getDyaCropBottom() * aspectRatioY / 1000
                    / TWIPS_PER_INCH;
        }
        else
        {
            imageHeight = picture.getDyaGoal() / TWIPS_PER_INCH;
            cropTop = picture.getDyaCropTop() / TWIPS_PER_INCH;
            cropBottom = picture.getDyaCropBottom() / TWIPS_PER_INCH;
        }

        Element root;
        if ( cropTop != 0 || cropRight != 0 || cropBottom != 0 || cropLeft != 0 )
        {
            float visibleWidth = Math
                    .max( 0, imageWidth - cropLeft - cropRight );
            float visibleHeight = Math.max( 0, imageHeight - cropTop
                    - cropBottom );

            root = htmlDocumentFacade.createBlock();
            htmlDocumentFacade.addStyleClass( root, "d",
                    "vertical-align:text-bottom;width:" + visibleWidth
                            + "in;height:" + visibleHeight + "in;" );

            // complex
            Element inner = htmlDocumentFacade.createBlock();
            htmlDocumentFacade.addStyleClass( inner, "d",
                    "position:relative;width:" + visibleWidth + "in;height:"
                            + visibleHeight + "in;overflow:hidden;" );
            root.appendChild( inner );

            Element image = htmlDocumentFacade.createImage( imageSourcePath );
            htmlDocumentFacade.addStyleClass( image, "i",
                    "position:absolute;left:-" + cropLeft + ";top:-" + cropTop
                            + ";width:" + imageWidth + "in;height:"
                            + imageHeight + "in;" );
            inner.appendChild( image );

            style.append( "overflow:hidden;" );
        }
        else
        {
            root = htmlDocumentFacade.createImage( imageSourcePath );
            root.setAttribute( "style", "width:" + imageWidth + "in;height:"
                    + imageHeight + "in;vertical-align:text-bottom;" );
        }

        currentBlock.appendChild( root );
    }

    @Override
    protected void processImageWithoutPicturesManager( Element currentBlock,
            boolean inlined, Picture picture )
    {
        // no default implementation -- skip
        currentBlock.appendChild( htmlDocumentFacade.document
                .createComment( "Image link to '"
                        + picture.suggestFullFileName() + "' can be here" ) );
    }

    @Override
    protected void processLineBreak( Element block, CharacterRun characterRun )
    {
        block.appendChild( htmlDocumentFacade.createLineBreak() );
    }

    protected void processNoteAutonumbered( HWPFDocument doc, String type,
            int noteIndex, Element block, Range noteTextRange )
    {
        final String textIndex = String.valueOf( noteIndex + 1 );
        final String textIndexClass = htmlDocumentFacade.getOrCreateCssClass(
                "a", "vertical-align:super;font-size:smaller;" );
        final String forwardNoteLink = type + "note_" + textIndex;
        final String backwardNoteLink = type + "note_back_" + textIndex;

        Element anchor = htmlDocumentFacade.createHyperlink( "#"
                + forwardNoteLink );
        anchor.setAttribute( "name", backwardNoteLink );
        anchor.setAttribute( "class", textIndexClass + " " + type
                + "noteanchor" );
        anchor.setTextContent( textIndex );
        block.appendChild( anchor );

        if ( notes == null )
        {
            notes = htmlDocumentFacade.createBlock();
            notes.setAttribute( "class", "notes" );
        }

        Element note = htmlDocumentFacade.createBlock();
        note.setAttribute( "class", type + "note" );
        notes.appendChild( note );

        Element bookmark = htmlDocumentFacade.createBookmark( forwardNoteLink );
        bookmark.setAttribute( "href", "#" + backwardNoteLink );
        bookmark.setTextContent( textIndex );
        bookmark.setAttribute( "class", textIndexClass + " " + type
                + "noteindex" );
        note.appendChild( bookmark );
        note.appendChild( htmlDocumentFacade.createText( " " ) );

        Element span = htmlDocumentFacade.getDocument().createElement( "span" );
        span.setAttribute( "class", type + "notetext" );
        note.appendChild( span );

        this.blocksProperies.add( new BlockProperies( "", -1 ) );
        try
        {
            processCharacters( doc, Integer.MIN_VALUE, noteTextRange, span );
        }
        finally
        {
            this.blocksProperies.pop();
        }
    }

    @Override
    protected void processPageBreak( HWPFDocument wordDocument, Element flow )
    {
        flow.appendChild( htmlDocumentFacade.createLineBreak() );
    }

    protected void processPageref( HWPFDocument hwpfDocument,
            Element currentBlock, Range textRange, int currentTableLevel,
            String pageref )
    {
        Element basicLink = htmlDocumentFacade.createHyperlink( "#" + pageref );
        currentBlock.appendChild( basicLink );

        if ( textRange != null )
            processCharacters( hwpfDocument, currentTableLevel, textRange,
                    basicLink );
    }

    protected void processParagraph( HWPFDocument hwpfDocument,
            Element parentElement, int currentTableLevel, Paragraph paragraph,
            String bulletText )
    {
        final Element pElement = htmlDocumentFacade.createParagraph();
        parentElement.appendChild( pElement );
        /*if(itemSymbol)
        	System.out.println(itemSymbol);*/
        if(itemSymbol){
        	Element span = htmlDocumentFacade.getDocument().createElement("span");
        	htmlDocumentFacade.addStyleClass(span, "itemSymbol", "font-size:12.0pt;line-height:150%;font-family:Wingdings;mso-ascii-font-family:Wingdings;mso-hide:none;mso-ansi-language:EN-US;mso-fareast-language:ZH-CN;font-weight:normal;mso-bidi-font-weight:normal;font-style:normal;mso-bidi-font-style:normal;text-underline:windowtext none;text-decoration:none;background:transparent");
        	span.setTextContent("Ø");
        	pElement.appendChild(span);
        	itemSymbol = false;
        }

        StringBuilder style = new StringBuilder();
        WordToHtmlUtils.addParagraphProperties( paragraph, style );

        final int charRuns = paragraph.numCharacterRuns();
        if ( charRuns == 0 ){
            return;
        }

        {
            final String pFontName;
            final int pFontSize;
            final CharacterRun characterRun = paragraph.getCharacterRun( 0 );
            if("".equals(paragraph.text().trim() )){
            	pElement.setTextContent(String.valueOf(UNICODECHAR_NO_BREAK_SPACE));
            }
            if ( characterRun != null )
            {
                Triplet triplet = getCharacterRunTriplet( characterRun );
                pFontSize = characterRun.getFontSize() / 2;
                pFontName = triplet.fontName;
                WordToHtmlUtils.addFontFamily( pFontName, style );
                WordToHtmlUtils.addFontSize( pFontSize, style );
            }
            else
            {
                pFontSize = -1;
                pFontName = WordToHtmlUtils.EMPTY;
            }
            blocksProperies.push( new BlockProperies( pFontName, pFontSize ) );
        }
        try
        {
            if ( WordToHtmlUtils.isNotEmpty( bulletText ) )
            {
                if ( bulletText.endsWith( "\t" ) )
                {
                    /*
                     * We don't know how to handle all cases in HTML, but at
                     * least simplest case shall be handled
                     */
                    final float defaultTab = TWIPS_PER_INCH / 2;
                    float firstLinePosition = paragraph.getIndentFromLeft()
                            + paragraph.getFirstLineIndent() + 20; // char have
                                                                   // some space

                    float nextStop = (float) ( Math.ceil( firstLinePosition
                            / defaultTab ) * defaultTab );

                    final float spanMinWidth = nextStop - firstLinePosition;

                    Element span = htmlDocumentFacade.getDocument()
                            .createElement( "span" );
                    htmlDocumentFacade
                            .addStyleClass( span, "s",
                                    "display: inline-block; text-indent: 0; min-width: "
                                            + ( spanMinWidth / TWIPS_PER_INCH )
                                            + "in;" );
                    pElement.appendChild( span );

                    Text textNode = htmlDocumentFacade.createText( bulletText
                            .substring( 0, bulletText.length() - 1 )
                            + UNICODECHAR_ZERO_WIDTH_SPACE
                            + UNICODECHAR_NO_BREAK_SPACE );
                    span.appendChild( textNode );
                }
                else
                {
                    Text textNode = htmlDocumentFacade.createText( bulletText
                            .substring( 0, bulletText.length() - 1 ) );
                    pElement.appendChild( textNode );
                }
            }

            processCharacters( hwpfDocument, currentTableLevel, paragraph,
                    pElement );
        }
        finally
        {
            blocksProperies.pop();
        }

        if ( style.length() > 0 )
            htmlDocumentFacade.addStyleClass( pElement, "p", style.toString() );

        WordToHtmlUtils.compactSpans( pElement );
        return;
    }

    protected void processSection( HWPFDocument wordDocument, Section section, int sectionCounter ){
    	//TODO   解析章节
        Element div = htmlDocumentFacade.createBlock();
        htmlDocumentFacade.addStyleClass( div, "d", getSectionStyle( section ) );
//      htmlDocumentFacade.body.appendChild( div );
        pageContainer.appendChild(div);

        processParagraphes( wordDocument, div, section, Integer.MIN_VALUE );
    }

    protected void processSingleSection( HWPFDocument wordDocument,
            Section section )
    {
        htmlDocumentFacade.addStyleClass( htmlDocumentFacade.body, "b",
                getSectionStyle( section ) );

        processParagraphes( wordDocument, htmlDocumentFacade.body, section,
                Integer.MIN_VALUE );
    }
    /** 解析table */
    protected void processTable( HWPFDocument hwpfDocument, Element flow, Table table ){
        Element tableHeader = htmlDocumentFacade.createTableHeader();
        Element tableBody = htmlDocumentFacade.createTableBody();

        final int[] tableCellEdges = WordToHtmlUtils.buildTableCellEdgesArray( table );
        final int tableRows = table.numRows();
        

        int maxColumns = Integer.MIN_VALUE;
        for ( int r = 0; r < tableRows; r++ )
        {
            maxColumns = Math.max( maxColumns, table.getRow( r ).numCells() );
        }

        for ( int r = 0; r < tableRows; r++ )
        {
            TableRow tableRow = table.getRow( r );

            Element tableRowElement = htmlDocumentFacade.createTableRow();
            StringBuilder tableRowStyle = new StringBuilder();
            
            
            WordToHtmlUtils.addTableRowProperties( tableRow, tableRowStyle );

            // index of current element in tableCellEdges[]
            int currentEdgeIndex = 0;
            final int rowCells = tableRow.numCells();
            for ( int c = 0; c < rowCells; c++ )
            {
                TableCell tableCell = tableRow.getCell( c );

                if ( tableCell.isVerticallyMerged() && !tableCell.isFirstVerticallyMerged() )
                {
                    currentEdgeIndex += getNumberColumnsSpanned( tableCellEdges, currentEdgeIndex, tableCell );
                    continue;
                }

                Element tableCellElement;
                if ( tableRow.isTableHeader() )
                {
                    tableCellElement = htmlDocumentFacade.createTableHeaderCell();
                }
                else
                {
                    tableCellElement = htmlDocumentFacade.createTableCell();
                }
                StringBuilder tableCellStyle = new StringBuilder();
                WordToHtmlUtils.addTableCellProperties( tableRow, tableCell, r == 0, r == tableRows - 1, c == 0, c == rowCells - 1, tableCellStyle );
                
                int colSpan = getNumberColumnsSpanned( tableCellEdges,
                        currentEdgeIndex, tableCell );
                currentEdgeIndex += colSpan;

                if ( colSpan == 0 )
                    continue;

                if ( colSpan != 1 )
                    tableCellElement.setAttribute( "colspan",
                            String.valueOf( colSpan ) );

                final int rowSpan = getNumberRowsSpanned( table,
                        tableCellEdges, r, c, tableCell );
                if ( rowSpan > 1 )
                    tableCellElement.setAttribute( "rowspan",
                            String.valueOf( rowSpan ) );

                processParagraphes( hwpfDocument, tableCellElement, tableCell,
                        table.getTableLevel() );

                if ( !tableCellElement.hasChildNodes() )
                {
                    tableCellElement.appendChild( htmlDocumentFacade
                            .createParagraph() );
                }
                if ( tableCellStyle.length() > 0 )
                    htmlDocumentFacade.addStyleClass( tableCellElement,
                            tableCellElement.getTagName(),
                            tableCellStyle.toString() );

                tableRowElement.appendChild( tableCellElement );
            }

            if ( tableRowStyle.length() > 0 )
                tableRowElement.setAttribute( "class", htmlDocumentFacade
                        .getOrCreateCssClass( "r", tableRowStyle.toString() ) );

            if ( tableRow.isTableHeader() )
            {
                tableHeader.appendChild( tableRowElement );
            }
            else
            {
                tableBody.appendChild( tableRowElement );
            }
        }

        final Element tableElement = htmlDocumentFacade.createTable();
        tableElement
                .setAttribute(
                        "class",
                        htmlDocumentFacade
                                .getOrCreateCssClass( "t",
                                        "table-layout:fixed;border-collapse:collapse;border-spacing:0;" ) );
        if ( tableHeader.hasChildNodes() )
        {
            tableElement.appendChild( tableHeader );
        }
        if ( tableBody.hasChildNodes() )
        {
            tableElement.appendChild( tableBody );
            flow.appendChild( tableElement );
        }
        else
        {
            logger.log( POILogger.WARN, "Table without body starting at [",
                    Integer.valueOf( table.getStartOffset() ), "; ",
                    Integer.valueOf( table.getEndOffset() ), ")" );
        }
    }

    
    
    /** 加入图片 */
    protected void processImage(Element parent, CharacterRun cr ){
    	 if(this.picstab.hasPicture(cr)){
         	Picture pic = picstab.extractPicture(cr, false); 
         	
         	String fileName = pic.suggestFullFileName(); 
         	OutputStream out = null;
				try {
					
//					TWIPS_PER_INCH/
					out = new FileOutputStream(new File(output.concat(File.separator).concat(fileName)));
					pic.writeImageContent(out); 
					Element img = htmlDocumentFacade.getDocument().createElement("img");
					String[] arr = output.split("/");
					String uri = arr[arr.length-3].concat(File.separator).concat(arr[arr.length-2]).concat(File.separator).concat(arr[arr.length-1]).concat(File.separator).concat(fileName);
					img.setAttribute("src",uri );
					if(pic.getWidth() > 600)
						img.setAttribute("style", "width: 600px;");
					Element imgBlock = htmlDocumentFacade.createBlock();
					this.htmlDocumentFacade.addStyleClass(imgBlock, "imgs", "text-align:center;");
					imgBlock.appendChild(img);
					parent.appendChild(imgBlock);
					
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if(out != null)
						try {
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}  
         }
    }// 图片END
    
    private boolean itemSymbol = false;
    @Override 
    protected void processParagraphes( HWPFDocument wordDocument, Element flow, Range range, int currentTableLevel ){
    	//TODO  mc process paragraphes 
    	
        final ListTables listTables = wordDocument.getListTables();
        int currentListInfo = 0;
        
        final int paragraphs = range.numParagraphs();
        for ( int p = 0; p < paragraphs; p++ )
        {
            Paragraph paragraph = range.getParagraph( p );
            
//			加入图片
            CharacterRun cr = paragraph.getCharacterRun(0);
            this.processImage(flow,cr);
//          table
            if ( paragraph.isInTable() && paragraph.getTableLevel() != currentTableLevel )
            {
                if ( paragraph.getTableLevel() < currentTableLevel )
                    throw new IllegalStateException( "Trying to process table cell with higher level (" + paragraph.getTableLevel() + ") than current table level (" + currentTableLevel + ") as inner table part" );

                Table table = range.getTable( paragraph );
                processTable( wordDocument, flow, table );

                p += table.numParagraphs();
                p--;
                continue;
            }
//          换页
            if ( paragraph.text().equals( "\u000c" ) )
            {
                processPageBreak( wordDocument, flow );
            }
            if ( paragraph.getIlfo() != currentListInfo )
            {
                currentListInfo = paragraph.getIlfo();
            }
//          嵌套段落
            if ( currentListInfo != 0 )
            {
                if ( listTables != null )
                {	
                	
                    final ListFormatOverride listFormatOverride = listTables.getOverride( paragraph.getIlfo() );

                    String label = getBulletText( listTables,paragraph, listFormatOverride.getLsid());
                    
                    if("".equals(label)){
                    	itemSymbol = true;
                    	/*
                    	Element span = htmlDocumentFacade.getDocument().createElement("span");
                    	span.setAttribute("style", "font-size:12.0pt;line-height:150%;font-family:Wingdings;mso-ascii-font-family:Wingdings;mso-hide:none;mso-ansi-language:EN-US;mso-fareast-language:ZH-CN;font-weight:normal;mso-bidi-font-weight:normal;font-style:normal;mso-bidi-font-style:normal;text-underline:windowtext none;text-decoration:none;background:transparent");
                    	span.setTextContent("Ø");
                    	
                    	flow.appendChild(span);
                    	*/
                    }
                    
                    processParagraph( wordDocument, flow, currentTableLevel,paragraph, label );
                }
                else
                {
                    logger.log( POILogger.WARN,
                            "Paragraph #" + paragraph.getStartOffset() + "-"
                                    + paragraph.getEndOffset()
                                    + " has reference to list structure #"
                                    + currentListInfo
                                    + ", but listTables not defined in file" );

                    processParagraph( wordDocument, flow, currentTableLevel,
                            paragraph, AbstractWordUtils.EMPTY );
                }
            }
            else
            {
                processParagraph( wordDocument, flow, currentTableLevel, paragraph, AbstractWordUtils.EMPTY );
            }
        }

    }
    
    ///////////////
    private static String getBulletText( ListTables listTables,
            Paragraph paragraph, int listId )
    {
        final ListLevel listLevel = listTables.getLevel( listId,
                paragraph.getIlvl() );
        
        if ( listLevel.getNumberText() == null )
            return "";

        StringBuffer bulletBuffer = new StringBuffer();
        char[] xst = listLevel.getNumberText().toCharArray();
        for ( char element : xst )
        {
            if ( element < 9 )
            {
                ListLevel numLevel = listTables.getLevel( listId, element );

                int num = numLevel.getStartAt();
                bulletBuffer.append( NumberFormatter.getNumber( num,
                        listLevel.getNumberFormat() ) );

                if ( numLevel == listLevel )
                {
                    numLevel.setStartAt( numLevel.getStartAt() + 1 );
                }

            }
            else
            {
                bulletBuffer.append( element );
            }
        }

        byte follow = listLevel.getTypeOfCharFollowingTheNumber();
        switch ( follow )
        {
        case 0:
            bulletBuffer.append( "\t" );
            break;
        case 1:
            bulletBuffer.append( " " );
            break;
        default:
            break;
        }

        return bulletBuffer.toString();
    }


}
