package cc.mask.utils;



public class StringUtil {
	
    /**
     * Returns <code>true</code> if the provided String is either <code>null</code>
     * or the empty String <code>""</code>.<p> 
     * 
     * @param value the value to check
     * 
     * @return true, if the provided value is null or the empty String, false otherwise
     */
    public static boolean isEmpty(String value) {

        return (value == null) || (value.length() == 0);
    }
    
    /**
     * extract file name form the given file path
     * @param filePath path to the file, like 'c:/test.jpg', 'c:\\test.jpg'
     * @param withExtention indicate contain file.extention. true : contain | false : ignore
     * @return fileName file.name;
     */
	public static String getFileName(String filePath,boolean withExtention){
		int sep = filePath.lastIndexOf("\\") == -1 ? filePath.lastIndexOf("/") : filePath.lastIndexOf("\\");
		if(withExtention)
			return filePath.substring( sep + 1 );
		return filePath.substring( sep + 1  , filePath.lastIndexOf("."));
	}
	
	/**
	 * get path to the given file , e.g. : c:\test\aaa.html -> c:\test\
	 * @param fileFullPath path to file;
	 * @return
	 */
	public static String getFilePath(String fileFullPath){
		int sep = fileFullPath.lastIndexOf("\\") == -1 ? fileFullPath.lastIndexOf("/") : fileFullPath.lastIndexOf("\\");
		return fileFullPath.substring(0, sep + 1);
	}
	
	

}
