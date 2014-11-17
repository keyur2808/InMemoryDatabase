package edu.buffalo.cse562.configuration;

public class DataConfiguration {
	
	private static DataConfiguration configuration = null;
	private String dataPath = null;
	private String swapPath = null;
	private boolean isSwap = false;
	private String indexPath=null;
	private boolean isIndex=false;
	public static DataConfiguration getInstance(){
		
		synchronized (DataConfiguration.class) {
			if(configuration == null){
				configuration = new DataConfiguration();
			}
		}
		return configuration;
	}
	
	private DataConfiguration(){
		
	}
	
	public void setDataPath(String dataPath){
		this.dataPath = dataPath;
	}
	
	public String getDataPath(){
		return this.dataPath;
	}

	public void setSwapPath(String swapPath) {
		this.swapPath = swapPath;
		this.isSwap = true;
	}
	
	public String getSwapPath() {
		return this.swapPath;
	}
	
	public boolean isSwap(){
		return isSwap;
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
		this.isIndex   = true;
	}

	public boolean isIndex() {
		return isIndex;
	}

	public void setIndex(boolean isIndex) {
		this.isIndex = isIndex;
	}

}