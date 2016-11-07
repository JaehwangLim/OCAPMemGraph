package com.gmail.assamoa.MemGraph;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCAPLogParser {
	public static final String SAMPLE = "847:04:720Total Size: 71303168, Allocated: 46604288, Free: 24698880 test";
	public static final String TIME_PATTERN = "\\d+:\\d{2}:\\d{1,}"; // xxx:yy:zzz
																		// =>
																		// xxx분
																		// yy초
																		// zzz밀리초
	public static final String BMEM_PATTERN = "Total Size: \\d+, Allocated: \\d+, Free: \\d+";
	public static final String MEM_PATTERN = "Heap\\s*\\[\\s*\\d+/\\s*\\d+\\],\\s*Native\\s*\\[\\s*\\d+/\\s*\\d+\\]";

	public String getFreeBMEM(String log) {
		Pattern pattern = Pattern.compile(BMEM_PATTERN);
		Matcher matcher = pattern.matcher(log);
		if (matcher.find()) {
			Pattern number = Pattern.compile("Free: \\d+");
			Matcher matcherMem = number.matcher(log);

			if (matcherMem.find()) {
				String freeMem = matcherMem.group();
				Matcher mem = Pattern.compile("\\d+").matcher(freeMem);
				if (mem.find()) {
					String freeMemory = mem.group();
					return freeMemory;
				}
			}
		}
		return null;
	}

	public String getFreeMEM(String log) {
		// System.out.println("check log:" + log);
		Pattern pattern = Pattern.compile(MEM_PATTERN);
		Matcher matcher = pattern.matcher(log);

		String freeHeapMem = null;
		String freeNativeMem = null;

		if (matcher.find()) {
			Pattern heapMem = Pattern.compile("Heap\\s*\\[\\s*\\d+/\\s*\\d+");
			Pattern nativeMem = Pattern.compile("Native\\s*\\[\\s*\\d+/\\s*\\d+");
			Matcher matcherHeap = heapMem.matcher(log);
			Matcher matcherNative = nativeMem.matcher(log);
			if (matcherHeap.find()) {
				String freeHeap = matcherHeap.group();
				Matcher mem = Pattern.compile("\\d+").matcher(freeHeap);
				if (mem.find()) {
					freeHeapMem = mem.group();
				}
			}
			if (matcherNative.find()) {
				String freeNative = matcherNative.group();
				Matcher mem = Pattern.compile("\\d+").matcher(freeNative);
				if (mem.find()) {
					freeNativeMem = mem.group();
				}
			}
		}
		if (freeHeapMem != null && freeNativeMem != null) {
			return freeHeapMem + "|" + freeNativeMem;
		}
		return null;
	}

	public String getMaxMem(String log) {
		Pattern pattern = Pattern.compile(MEM_PATTERN);
		Matcher matcher = pattern.matcher(log);

		String freeHeapMem = null;
		String freeNativeMem = null;

		if (matcher.find()) {
			Pattern heapMem = Pattern.compile("Heap\\s*\\[\\s*\\d+/\\s*\\d+");
			Pattern nativeMem = Pattern.compile("Native\\s*\\[\\s*\\d+/\\s*\\d+");
			Matcher matcherHeap = heapMem.matcher(log);
			Matcher matcherNative = nativeMem.matcher(log);
			if (matcherHeap.find()) {
				String freeHeap = matcherHeap.group();
				Matcher mem = Pattern.compile("\\d+").matcher(freeHeap);

				if (mem.find() && mem.find()) {
					freeHeapMem = mem.group();
				}
			}
			if (matcherNative.find()) {
				String freeNative = matcherNative.group();
				Matcher mem = Pattern.compile("\\d+").matcher(freeNative);
				if (mem.find() && mem.find()) {
					freeNativeMem = mem.group();
				}
			}
		}
		if (freeHeapMem != null && freeNativeMem != null) {
			return freeHeapMem + "|" + freeNativeMem;
		}
		return null;
	}
}
