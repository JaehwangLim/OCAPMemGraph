package com.gmail.assamoa.memgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYSeries;

public class OCAPLogParser {
	// GC후 free 메모리를 찍는 로그의 패턴
	public static final String FREE_MEM_LOG_PATTERN = "Heap\\s*\\[\\s*\\d+/\\s*\\d+\\],\\s*Native\\s*\\[\\s*\\d+/\\s*\\d+\\]";

	private static final int MEMORY_SIZE_UNKNOWN = -1;

	private boolean largeData; // GC후 메모리 로그의 양이 Integer.MAX_VALUE를 넘어갔는지 여부

	private int totalHeapSize = MEMORY_SIZE_UNKNOWN; // Heap Memory의 최대값

	private int totalNativeSize = MEMORY_SIZE_UNKNOWN; // Native Memory의 최대값

	private int count; // 메모리 로그 개수 -> 그래프 그릴 값들의 수. JFreeChart 에서는 Integer.MAX_VALUE 개의 데이터만 지원 한다.

	boolean oldGenFound = false; // OLD GEN end 로그를 찾았는지
	boolean youngGenFound = false; // YOUNG GEN end 로그를 찾았는지
	private boolean isTimeIncluded = false; // TODO: JFrame의 시간포함 라디오버튼 설정값 읽어와야 함
	private boolean bParseYoungGen = true; // TODO: JFrame의 YoungGen 포함 라디오버튼 설정값 읽어와야 함

	private SimpleDateFormat timeFormat;
	private XYSeries heapSeries;
	private XYSeries nativeSeries;

	private TimeSeries heapTimeSeries;
	private TimeSeries nativeTimeSeries;
	private int errorCount;

	/**
	* timeFormat
	*	G  Era designator  Text  AD  
	*	y  Year  Year  1996; 96  
	*	Y  Week year  Year  2009; 09  
	*	M  Month in year (context sensitive)  Month  July; Jul; 07  
	*	L  Month in year (standalone form)  Month  July; Jul; 07  
	*	w  Week in year  Number  27  
	*	W  Week in month  Number  2  
	*	D  Day in year  Number  189  
	*	d  Day in month  Number  10  
	*	F  Day of week in month  Number  2  
	*	E  Day name in week  Text  Tuesday; Tue  
	*	u  Day number of week (1 = Monday, ..., 7 = Sunday)  Number  1  
	*	a  Am/pm marker  Text  PM  
	*	H  Hour in day (0-23)  Number  0  
	*	k  Hour in day (1-24)  Number  24  
	*	K  Hour in am/pm (0-11)  Number  0  
	*	h  Hour in am/pm (1-12)  Number  12  
	*	m  Minute in hour  Number  30  
	*	s  Second in minute  Number  55  
	*	S  Millisecond  Number  978  
	*	z  Time zone  General time zone  Pacific Standard Time; PST; GMT-08:00  
	*	Z  Time zone  RFC 822 time zone  -0800  
	*	X  Time zone  ISO 8601 time zone  -08; -0800; -08:00  
	*		
	 * @param bParseYoungGen
	 * @param isTimeIncluded
	 * @param timeFormat
	 */
	public OCAPLogParser(boolean bParseYoungGen, boolean isTimeIncluded, String timeFormat) {
		this.bParseYoungGen = bParseYoungGen;
		this.isTimeIncluded = isTimeIncluded;

		if (isTimeIncluded) {
			this.timeFormat = new SimpleDateFormat(timeFormat);
		}
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
		errorCount = 0;

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line;

			// 로그를 한줄씩 읽어 메모리 로그 여부를 확인한다.
			while ((line = br.readLine()) != null) {
				checkLine(line);
			}
			System.out.println("DONE:" + (largeData ? "A LOT OF" : "" + count) + " memory logs, " + errorCount + " errors.");
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

		heapSeries = new XYSeries("Heap");
		nativeSeries = new XYSeries("Native");

		heapTimeSeries = new TimeSeries("Heap");
		nativeTimeSeries = new TimeSeries("Native");
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
					// 로그수집 시간이 있는경우, count가 아닌, 시간 기준으로 데이터를 입력한다.

					Date d = parseTime(line);
					if (d != null) {
						Millisecond ms = new Millisecond(d);
						heapTimeSeries.add(ms, heapMem);
						nativeTimeSeries.add(ms, nativeMem);
					} else {
						// 시간 값을 제대로 파싱하지 못한 경우
						// 에러 카운트 계산 하자
						errorCount++;
					}
				} else {
					heapSeries.add(count, heapMem);
					nativeSeries.add(count, nativeMem);
				}

				// Heap memory의 MAX값이 설정되지 않은 경우, Heap/Native의 MAX값을 파싱해서 가지고 있는다
				// 추후, 그래프 그릴때 Y축의 범위를 결정할때 사용된다.
				if (totalHeapSize == MEMORY_SIZE_UNKNOWN) {
					parseTotalMemorySize(line);
				}

			} catch (Exception ex) {
				errorCount++;
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
		if (count < Integer.MAX_VALUE) {
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

	private Date parseTime(String log) {
		try {
			return timeFormat.parse(log);
		} catch (Exception e) {
		}
		return null;
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
	public int getCount() {
		return count;
	}

	public XYSeries getHeapSeries() {
		return heapSeries;
	}

	public XYSeries getNativeSeries() {
		return nativeSeries;
	}

	public TimeSeries getHeapTimeSeries() {
		return heapTimeSeries;
	}

	public TimeSeries getNativeTimeSeries() {
		return nativeTimeSeries;
	}
}
