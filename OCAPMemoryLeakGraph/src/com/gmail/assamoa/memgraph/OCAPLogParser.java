package com.gmail.assamoa.memgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.data.category.DefaultCategoryDataset;

public class OCAPLogParser {
	public static final String TIME_PATTERN = "\\d+:\\d{2}:\\d{1,}"; // xxx:yy.zzz => xxx분 yy초 zzz밀리초

	// GC후 free 메모리를 찍는 로그의 패턴
	public static final String FREE_MEM_LOG_PATTERN = "Heap\\s*\\[\\s*\\d+/\\s*\\d+\\],\\s*Native\\s*\\[\\s*\\d+/\\s*\\d+\\]";

	private static final int MEMORY_SIZE_UNKNOWN = -1;

	private boolean largeData; // GC후 메모리 로그의 양이 Integer.MAX_VALUE를 넘어갔는지 여부

	private int totalHeapSize = MEMORY_SIZE_UNKNOWN; // Heap Memory의 최대값

	private int totalNativeSize = MEMORY_SIZE_UNKNOWN; // Native Memory의 최대값

	private long count; // 메모리 로그 개수 -> 그래프 그릴 값들의 수

	boolean oldGenFound = false; // OLD GEN end 로그를 찾았는지
	boolean youngGenFound = false; // YOUNG GEN end 로그를 찾았는지
	private boolean isTimeIncluded = false; // TODO: JFrame의 시간포함 라디오버튼 설정값 읽어와야 함
	private boolean bParseYoungGen = true; // TODO: JFrame의 YoungGen 포함 라디오버튼 설정값 읽어와야 함

	private DefaultCategoryDataset datasetHeap;
	private DefaultCategoryDataset datasetNative;

	public OCAPLogParser(boolean bParseYoungGen, boolean isTimeIncluded) {
		this.bParseYoungGen = bParseYoungGen;
		this.isTimeIncluded = isTimeIncluded;
	}

	/**
	 * 로그파일을 읽고 파싱한다.
	 * @param file
	 */
	public void readLogFile(File file) {
		init(); // 초기화

		FileReader fr = null;
		BufferedReader br = null;
		largeData = false;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line;

			// 로그를 한줄씩 읽어 메모리 로그 여부를 확인한다.
			while ((line = br.readLine()) != null) {
				checkLine(line);
			}
			System.out.println("DONE:" + (largeData ? "A LOT OF" : "" + count) + " memory logs");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception ex) {
			}
			try {
				fr.close();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * 초기화 한다.
	 */
	private void init() {
		oldGenFound = false; // OLD GEN end 로그를 찾았는지
		youngGenFound = false; // YOUNG GEN end 로그를 찾았는지

		datasetHeap = new DefaultCategoryDataset();
		datasetNative = new DefaultCategoryDataset();
	}

	private void checkLine(String line) {
		if (oldGenFound) {
			if (parseOldGen(line, isTimeIncluded)) {
				oldGenFound = false;
				increaseCount();
			}
		} else if (youngGenFound) {
			if (parseYoungGen(line, isTimeIncluded)) {
				youngGenFound = false;
				increaseCount();
			}
		} else if (line.indexOf("Manual GC END") > -1) {
			oldGenFound = true;
		} else if (bParseYoungGen) {
			if (line.indexOf("END GC") > -1) {
				youngGenFound = true;
			}
		}
	}

	private boolean parseOldGen(String line, boolean parseTime) {
		String freeMemory = getFreeMEM(line);
		if (freeMemory != null) {
			try {
				StringTokenizer token = new StringTokenizer(freeMemory, "|");
				int heapMem = Integer.parseInt(token.nextToken());
				int nativeMem = Integer.parseInt(token.nextToken());

				if (parseTime) { // dataset에 heap/native free memory 량을 추가 한다
					//// 로그시간 처리 테스트용
					long timeValue = System.currentTimeMillis();
					////
					// 로그수집 시간이 있는경우, count가 아닌, 시간 기준으로 데이터를 입력한다.
					datasetHeap.addValue(heapMem, "Free Heap", new Date(timeValue + count));
					datasetNative.addValue(nativeMem, "Free Native", new Date(timeValue + count));
				} else {
					datasetHeap.addValue(heapMem, "Free Heap", "" + count);
					datasetNative.addValue(nativeMem, "Free Native", "" + count);
				}

				// Heap memory의 MAX값이 설정되지 않은 경우, Heap/Native의 MAX값을 파싱해서 가지고 있는다
				// 추후, 그래프 그릴때 Y축의 범위를 결정할때 사용된다.
				if (totalHeapSize == MEMORY_SIZE_UNKNOWN) {
					parseTotalMemorySize(line);
				}

			} catch (Exception ex) {
			}
			return true; // 이 로그는 Old Gen 로그가 맞음
		} else {
			return false; // 이 로그는 Old Gen 로그가 아님
		}
	}

	private boolean parseYoungGen(String line, boolean parseTime) {
		// OCAP에서는 YoungGen과 OldGen의 Free Memory 로그가 동일하므로 OldGen의 parser 사용
		return parseOldGen(line, parseTime);
	}

	/**
	 * 메모리 로그 count를 증가 시킨다.
	 */
	private void increaseCount() {
		if (count < Long.MAX_VALUE) {
			count++;
		} else {
			count = 0;
			largeData = true;
		}
	}

	/**
	 * 전달된 한줄 로그가 Heap/Native Free Memory log가 맞는지를 확인하고, 실제 free memory 값을 뽑아내서 리턴한다.
	 * @param log
	 * @return [free Heap]|[free Native]
	 */
	private String getFreeMEM(String log) {
		Pattern pattern = Pattern.compile(FREE_MEM_LOG_PATTERN); // free memory 로그의 패턴
		Matcher matcher = pattern.matcher(log); // free memory 로그 패턴의 Matcher

		String freeHeapMem = null;
		String freeNativeMem = null;

		if (matcher.find()) { // log가 free memory 로그 패턴과 일치하면
			// heap과 native 의 free memory 로그 패턴으로 각 메모리를 추출한다.
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

	/**
	 * Heap과 Native의 최대 사이즈를 확인한다.
	 * @param log
	 * @return
	 */
	private void parseTotalMemorySize(String log) {
		Pattern pattern = Pattern.compile(FREE_MEM_LOG_PATTERN);
		Matcher matcher = pattern.matcher(log);

		String totalHeapMem = null;
		String totalNativeMem = null;

		if (matcher.find()) {
			Pattern heapMem = Pattern.compile("Heap\\s*\\[\\s*\\d+/\\s*\\d+");
			Pattern nativeMem = Pattern.compile("Native\\s*\\[\\s*\\d+/\\s*\\d+");
			Matcher matcherHeap = heapMem.matcher(log);
			Matcher matcherNative = nativeMem.matcher(log);
			if (matcherHeap.find()) {
				String freeHeap = matcherHeap.group();
				Matcher mem = Pattern.compile("\\d+").matcher(freeHeap);

				if (mem.find() && mem.find()) { // Total memory 는 두번째 패턴
					totalHeapMem = mem.group();
				}
			}
			if (matcherNative.find()) {
				String freeNative = matcherNative.group();
				Matcher mem = Pattern.compile("\\d+").matcher(freeNative);
				if (mem.find() && mem.find()) {
					totalNativeMem = mem.group();
				}
			}
		}
		if (totalHeapMem != null && totalNativeMem != null) {
			try {
				totalHeapSize = Integer.parseInt(totalHeapMem);
				totalNativeSize = Integer.parseInt(totalNativeMem);
			} catch (Exception e) {
				totalHeapSize = MEMORY_SIZE_UNKNOWN;
				totalNativeSize = MEMORY_SIZE_UNKNOWN;
			}
		}
	}

	/**
	 * 메모리 로그의 개수가 Long 의 최대값보다 많은지를 리턴한다.<br>
	 * true인 경우, {@link #getCount()}가 리턴하는 값은 유효하지 않다.<br>
	 * @return
	 */
	public boolean isLargeData() {
		return largeData;
	}

	/**
	 * Heap 메모리의 최대 크기를 리턴한다.
	 * @return
	 */
	public int getTotalHeapSize() {
		return totalHeapSize;
	}

	/**
	 * Native 메모리의 최대 크기를 리턴한다.
	 * @return
	 */
	public int getTotalNativeSize() {
		return totalNativeSize;
	}

	/**
	 * 메모리 로그의 개수를 리턴한다
	 * @return
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Heap 메모리의 그래프 데이터를 리턴한다.
	 * @return
	 */
	public DefaultCategoryDataset getDatasetHeap() {
		return datasetHeap;
	}

	/**
	 * Native 메모리의 그래프 데이터를 리턴한다.
	 * @return
	 */
	public DefaultCategoryDataset getDatasetNative() {
		return datasetNative;
	}
}
