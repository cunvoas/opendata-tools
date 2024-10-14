package oneshot;

import org.apache.commons.lang3.StringUtils;

/**
 * ne gère pas le passage au 0
 * 			 N<>S  E<>O .
 */
public class OneShotListInspire333m {

	public static void main(String[] args) {

		
		String loc="CRS3035RES200mN3079800E3833800";
		
		String v1=StringUtils.left(loc, 15);
		String v2=StringUtils.mid(loc, 14, 1);
		String v3=StringUtils.mid(loc, 22, 1);
		
		String sLat = StringUtils.mid(loc, 15, 7);
//		System.out.println(sLat);
		int lat = Integer.valueOf(sLat);
		
		String sLon = StringUtils.mid(loc, 23, 7);
//		System.out.println(sLon);
		int lon = Integer.valueOf(sLon);
		
		StringBuilder sb = new StringBuilder("in (");
		for (int iLat=-400; iLat<=400; iLat+=200) {
			for (int iLon=-400; iLon<=400; iLon+=200) {
				int nLat = lat+iLat;
				int nLon = lon+iLon;
				
				String idInspire = v1+ nLat +v3+ nLon;
				
				System.out.println(idInspire);
				
				if (sb.length()>4) {
					sb.append(",");
				}
				sb.append("'").append(idInspire).append("'");
			}
		}
		sb.append(")");

		System.out.println(sb.toString());
	}

}
