package com.wordpdf.core;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TaskGet implements Runnable{

	AtomicInteger index;//0
	Object[] keys;
	Word[] msgs;
	CountDownLatch latch;
	public TaskGet(Object[] keys,Word[] msgs,AtomicInteger index,CountDownLatch latch){
		this.keys = keys;
		this.index = index;
		this.msgs = msgs;
		this.latch = latch;
	}
	
	@Override
	public void run() {
		int num = index.getAndIncrement();
		Word key ;
		try {
			key  = getMsg(num);
		} catch (IOException e) {
			e.printStackTrace();
			key = new Word("出错error");
			key.setExample("");
			key.setSpecific("");
		}finally {
			latch.countDown();
		}
		msgs[num]  = key;
		if(num%50==0){
			System.out.println("---------->"+num);
		}
	}
	
	
	
	public Word getMsg(int index) throws IOException{
		Word key = (Word) keys[index];
		Document doc = Jsoup.connect("http://dict.youdao.com/w/"+key.getName()).get();
		Element element = doc.getElementById("phrsListTab");
		Element example = doc.getElementById("bilingual");
		String str;
		if(element!=null&&example!=null){
				/*
				 * 这里去掉了开头的单词
				 */
				str = element.text();
				int indexOf = str.indexOf(" ");
				str= str.substring(indexOf+1);
				
				Elements uls = example.getElementsByTag("ul");
				
				ArrayList<EnZh> list = new ArrayList<EnZh>();
				Element ul = uls.get(0);
				int size = ul.childNodeSize();
				if(size==7){
					size=3;
				}else if(size==5){
					size=2;
				}else if(size==3){
					size=1;
				}else{
					size=0;
				}
				for(int i=0;i<size;i++){
					
					Element child = ul.child(i);
					Element en = child.child(0);
					Element zh = child.child(1);
					EnZh enZh = new EnZh(en.text(), zh.text());
					list.add(enZh);
				}
				key.setEnzhs(list);
		}else{
			str = ""/*"当前单词"+key.getName()+"无效"*/;
		}

		key.setSpecific(str);//添加爬取的詳細信息
		
		return key;
	}
}
