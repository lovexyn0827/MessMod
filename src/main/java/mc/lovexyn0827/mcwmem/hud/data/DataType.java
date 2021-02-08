package mc.lovexyn0827.mcwmem.hud.data;

public enum DataType {
	INTEGER{
		@Override
		public String getStringOf(Object ob) {
			return ((Integer)ob).toString();
		}
	},
	FLOAT{
		@Override
		public String getStringOf(Object ob) {
			return ((Float)ob).toString();
		}
	},
	DOUBLE{
		@Override
		public String getStringOf(Object ob) {
			return ((Double)ob).toString();
		}
	},
	STRING{
		@Override
		public String getStringOf(Object ob) {
			return (String) ob;
		}
	};
	
	public abstract String getStringOf(Object ob);
}
