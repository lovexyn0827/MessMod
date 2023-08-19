package lovexyn0827.mess.log.entity;

public enum SideLogStoragePolicy {
	MIXED(true, true), 
	SEPARATED(true, true), 
	SERVER_ONLY(true, false), 
	CLIENT_ONLY(false, true);
	
	public final boolean shouldTickServer;
	public final boolean shouldTickClient;
	
	private SideLogStoragePolicy(boolean shouldTickServer, boolean shouldTickClient) {
		this.shouldTickServer = shouldTickServer;
		this.shouldTickClient = shouldTickClient;
	}
}