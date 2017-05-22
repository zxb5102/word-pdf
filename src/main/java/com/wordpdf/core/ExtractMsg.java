package com.wordpdf.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class ExtractMsg {

	private ArrayList<File> res;
	private File saveLocation;

	public ExtractMsg(ArrayList<File> res, File saveLocation) {
		this.res = res;
		this.saveLocation = saveLocation;
	}
	public void extract() throws IOException, InterruptedException {
		ArrayList<Path> paths = new GetAllPath().getAllPath(res);
		Object[] keys = getAllkeys(paths);
		int length = keys.length;
		System.out.println("总共 " + length + "个单词");
		CountDownLatch latch = new CountDownLatch(length);
		Word[] values = new Word[length];
		AtomicInteger atomicInteger = new AtomicInteger(0);

		ThreadPoolExecutor pool = new ThreadPoolExecutor(20, 500, 2, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(length));
		for (int i = 0; i < keys.length; i++) {
			pool.execute(new TaskGet(keys, values, atomicInteger, latch));
		}
		pool.shutdown();
		latch.await();
		GetPdf getPdf = new GetPdf(values, saveLocation);
		try {
			getPdf.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Object[] getKeys(File in) throws IOException {
		String charsetName = "utf-8";
		Document parse = Jsoup.parse(in, charsetName);
		String text = parse.text();
		String[] split = text.split("\\s+");
		Object[] array = Arrays.stream(split).parallel().filter(n -> {
			if (n.matches("[\\s\\S]*[^A-Za-z][\\s\\S]*")) {
				return false;
			} else {
				return true;
			}
		}).map(n -> {
			return n.toLowerCase();
		}).toArray();
		return array;
	}


	public Object[] getAllkeys(ArrayList<Path> paths) {
		Supplier<ArrayList<String>> suppler = () -> new ArrayList<>();
		BiConsumer<ArrayList<String>, Object> accmulator = (ArrayList<String> list, Object obj) -> {
			String charsetName = "utf-8";
			Path path = (Path) obj;
			File in = path.toFile();
			Document parse;
			try {
				parse = Jsoup.parse(in, charsetName);
				String text = parse.text();
				String[] split = text.split("\\s+");
				Arrays.stream(split).parallel().filter(n -> {
					if (n.matches("[\\s\\S]*[^A-Za-z][\\s\\S]*")) {
						return false;
					} else {
						return true;
					}
				}).map(n -> {
					return n.toLowerCase();
				}).forEach((item) -> {
					list.add(item);
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
		BiConsumer<ArrayList<String>, ArrayList<String>> combiner = (one, two) -> {
			one.addAll(two);
		};
		Object[] array = paths.toArray();
		ArrayList<String> collect = Arrays.stream(array).parallel().collect(suppler, accmulator, combiner);
		return sort(collect);
	}

	private Object[] sort(ArrayList<String> collect) {
		Object[] objs = collect.toArray();
		Map<Object, List<Object>> map = Arrays.stream(objs).parallel().filter(n -> {
			if (n != null) {
				return true;
			} else {
				System.out.println("出现null");
				return false;
			}
		}).collect(Collectors.groupingBy(n -> {
			return n;
		}));

		Object[] arraynos = map.entrySet().toArray();
		Supplier<ArrayList<Word>> supplier = () -> {
			return new ArrayList<Word>();
		};
		BiConsumer<ArrayList<Word>, Object> accumulator = (list, item) -> {
			Entry<String, ArrayList<String>> entry = (Entry<String, ArrayList<String>>) item;
			String key = entry.getKey();
			int size = entry.getValue().size();
			Word word = new Word(key);
			word.setCount(size);
			list.add(word);
		};
		BiConsumer<ArrayList<Word>, ArrayList<Word>> combiner2 = (list1, list2) -> {
			list1.addAll(list2);
		};
		ArrayList<Word> collect2 = Arrays.stream(arraynos).parallel().collect(supplier, accumulator, combiner2);
		Comparator<? super Object> comparator = (one, two) -> {
			Word o = (Word) one;
			Word t = (Word) two;
			return t.getCount() - o.getCount();
		};
		Object[] sorted = Arrays.stream(collect2.toArray()).parallel().sorted(comparator).toArray();
		return sorted;
	}
}
