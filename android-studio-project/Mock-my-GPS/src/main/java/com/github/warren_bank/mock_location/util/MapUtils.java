package com.github.warren_bank.mock_location.util;

// copied from:
//   https://github.com/osmandapp/Osmand/blob/2.0.0/OsmAnd-java/src/main/java/net/osmand/util/MapUtils.java

import com.github.warren_bank.mock_location.util.GeoPointParserUtil.GeoParsedPoint;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MapUtils {

	/**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Base64 Alphabet" equivalents as specified
     * in Table 1 of RFC 2045.
     */
    private static final char intToBase64[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '~'
    };

	public static GeoParsedPoint decodeShortLinkString(String s) {
		// convert old shortlink format to current one
		s = s.replaceAll("@", "~");
		int i = 0;
		long x = 0;
		long y = 0;
		int z = -8;

		for (i = 0; i < s.length(); i++) {
			int digit = -1;
			char c = s.charAt(i);
			for (int j = 0; j < intToBase64.length; j++)
				if (c == intToBase64[j]) {
					digit = j;
					break;
				}
			if (digit < 0)
				break;
			if (digit < 0)
				break;
			// distribute 6 bits into x and y
			x <<= 3;
			y <<= 3;
			for (int j = 2; j >= 0; j--) {
				x |= ((digit & (1 << (j + j + 1))) == 0 ? 0 : (1 << j));
				y |= ((digit & (1 << (j + j))) == 0 ? 0 : (1 << j));
			}
			z += 3;
		}
		double lon = x * Math.pow(2, 2 - 3 * i) * 90. - 180;
		double lat = y * Math.pow(2, 2 - 3 * i) * 45. - 90;
		// adjust z
		if (i < s.length() && s.charAt(i) == '-') {
			z -= 2;
			if (i + 1 < s.length() && s.charAt(i + 1) == '-')
				z++;
		}
		return new GeoParsedPoint(lat, lon, z);
	}
}
