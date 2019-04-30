package org.hl7.fhir.utg;

import java.util.HashMap;
import java.util.Map;

public class URLLookup {

	private static final Map<String, String> OID_URL_MAP = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			// put("2.16.840.1.113883.6.301.9", "http://some.url");
		}
	};
	
	public static String getUrl(String oid) {
		return OID_URL_MAP.get(oid);
	}

	public static boolean hasUrlOverride(String oid) {
		return OID_URL_MAP.containsKey(oid);
	}
}
