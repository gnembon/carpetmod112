package redstone.multimeter.util;

import java.util.List;
import java.util.function.Function;

public class ListUtils {
	
	public static <T> int binarySearch(List<T> list, Function<T, Boolean> tooLow) {
		return binarySearch(list, 0, list.size() - 1, tooLow);
	}
	
	public static <T> int binarySearch(List<T> list, int low, int high, Function<T, Boolean> tooLow) {
		if (list.isEmpty()) {
			return -1;
		}
		
		while (high > low) {
			int mid = (low + high) / 2;
			
			if (tooLow.apply(list.get(mid))) {
				low = mid + 1;
			} else {
				high = mid;
			}
		}
		
		return low;
	}
}
