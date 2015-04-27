package edu.infrastructure;

import java.io.IOException;
import java.util.Properties;

public class InfraEndpoints {

	public static Properties APP_PROPERTIES = new Properties();
	
	static {
		try {
			APP_PROPERTIES.load(InfraEndpoints.class.getClassLoader().getResourceAsStream("config.properties"));
		} 
		catch (IOException e) {
//			LOGGER.error("Error Loading Mail Properties",e);
		}
	}
	
	//Admin
	public static String ADMIN_VC_URL = APP_PROPERTIES.getProperty("AdminVCenterURL");
	public static String ADMIN_USERNAME = APP_PROPERTIES.getProperty("AdminUserName");
	public static String ADMIN_PASSWORD = APP_PROPERTIES.getProperty("AdminPassword");
	
	public static String TEAM12_VC_URL = APP_PROPERTIES.getProperty("Team12VCenterURL");
	public static String TEAM12_USERNAME = APP_PROPERTIES.getProperty("Team12UserName");
	public static String TEAM12_PASSWORD = APP_PROPERTIES.getProperty("Team12Password");
	
	public static String OUT_LOG_DIR_PATH = APP_PROPERTIES.getProperty("OutputLogDirPath");
	public static String OUT_FILE_EXTN = APP_PROPERTIES.getProperty("OutputFileExtn");
	
	public static int REFRESH_INTERVAL = 20;
	
	//Use <group>.<name>.<ROLLUP-TYPE> path specification to identify counters.
	public static String[] PERF_COUNTERS = new String[] {	"cpu.usage.average", 
															"mem.usage.average",
															"net.usage.average",
															/*"virtualDisk.read.average",
															"virtualDisk.write.average",*/
															"disk.read.average",
															"disk.write.average"
//															//"datastore.read.average",
//															//"datastore.write.average"
															};
}
