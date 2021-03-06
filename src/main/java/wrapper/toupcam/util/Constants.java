package wrapper.toupcam.util;

public class Constants {

	public static final String PROJECT_BASE_PATH = "./";
	public static final int INT_SIZE = 4;
	
	public static final long MEM_SIZE_FOR_TOUPCAMINST = 512 * 16;
	
	/**
	 * extraction of dir name in {@code NativeLibExtractor} is 
	 * being made on first '/', don't change these values.
	 */

	//dll folder - nativeLibs/x64
	public static final String x64_TOUPCAM_SO = "x64/libtoupcam.so";
    public static final String x64_TOUPCAM_DLL = "x64/1/toupcam.dll"; //5mb
//    public static final String x64_TOUPCAM_DLL = "x64/toupcam.dll"; //3mb
    
    public static final String x86_TOUPCAM_SO = "x86/libtoupcam.so";
    public static final String x86_TOUPCAM_DLL = "x86/toupcam.dll";
    
    public static final String HELLO_SO = "Hello.so";
    
    //public static final String PATH = "./src/main/resources/";
    public static final String IMAGES_PATH = PROJECT_BASE_PATH + "/capturedImages";
    
    
    public static final String JAR_FILE_NAME = "ToupcamJavaWrapper.jar";
    public static final String NATIVE_LIB_EXTRACTION_DIR = PROJECT_BASE_PATH + "nativeLibs/";
    
    
    /**
     * Exception Messages
     */
    
    public static final String RESTART_STREAM_EXCEP_MSG = "no image listener callback cached";
	
	
}
