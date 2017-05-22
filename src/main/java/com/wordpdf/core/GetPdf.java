package com.wordpdf.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

public class GetPdf {
	
	private Word[] words;
	private File saveLocation;
	
	private Document documentWord;
	private Document documentSen;
	public GetPdf(){}
	
	public GetPdf(Word[] words,File saveLocation){
		this.words = words;
		this.saveLocation = saveLocation;
	}

	public void execute() throws Exception{
		before();
		main();
		after();
	}
	
	private void before() throws IOException, DocumentException{
		File file = new File(saveLocation+"/word.pdf");
		File file2 = new File(saveLocation+"/sentence.pdf");
		if(!file.exists()){
			file.createNewFile();
		}
		if(!file2.exists()){
			file2.createNewFile();
		}
		
       	documentWord = new Document();
       	documentSen = new Document();
        PdfWriter.getInstance(documentWord, new FileOutputStream(file));
        PdfWriter.getInstance(documentSen, new FileOutputStream(file2));
        documentWord.open();
        documentSen.open();
	}
	private void main() throws Exception{
		//获取楷体字体的路径
		String path = getPath("SIMKAI.TTF");
		
		BaseFont baseFont = BaseFont.createFont(path,BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
		Font zhFont = new Font(baseFont, 14, Font.NORMAL, BaseColor.BLACK);
		Font keyFont = new Font(baseFont, 14, Font.NORMAL, BaseColor.RED);
		Font roundFont = new Font(baseFont, 14, Font.NORMAL, BaseColor.BLUE);
		Font chapterFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);
		
		int chapternum = 1;
		int round = 0;
		int cut = 500;
		Chapter chapterWord=null;
		Chapter chapterSentence=null;
		
		for(int i=0;i<words.length;i++){
			Word word = words[i];
			String specific = word.getSpecific();
			if(!specific.equals("")){
				if(round%cut==0){
					if(chapterWord!=null){
						documentWord.add(chapterWord);
					}
					if(chapterSentence!=null){
						documentSen.add(chapterSentence);
					}
					chapterWord = new Chapter(new Paragraph((round+1)+"~"+(round+500),chapterFont),1);
					chapterSentence = new Chapter(new Paragraph((round+1)+"~"+(round+500),chapterFont),1);
					chapterWord.setNumberDepth(0);
					chapterSentence.setNumberDepth(0);
					chapternum++;
				}
				//设置当前是第几个单词
				String name = word.getName();
				Paragraph paragraphSentence = new Paragraph(round+1+" ",roundFont);
				
				//设置当前的单词
				Chunk chunk2 = new Chunk(name,keyFont);
				paragraphSentence.add(chunk2);

				//空格分割
				Chunk chunk3 = new Chunk("  ",zhFont);

				//独立添加音标
				ArrayList<Chunk> list = getIpaList(specific, zhFont);
				paragraphSentence.add(chunk3);
				for(Chunk ch : list){
					paragraphSentence.add(ch);
				}
				chapterSentence.addSection(paragraphSentence,0);
				
				//设置单词PDF的 当前是第几个单词
				Paragraph paragraphWord = new Paragraph(round+1+" ",roundFont);
				
				//添加单词到单词pdf
				Chunk chunk = new Chunk(name,keyFont);
				paragraphWord.add(chunk);

				chapterWord.addSection(paragraphWord,0);
				//插入带音标的释义
				Element dealIPA = dealIPA(specific,zhFont);
				chapterWord.add(dealIPA);
				
				//添加例句到sentencePDF
				ArrayList<EnZh> enzhs = word.getEnzhs();
				for(EnZh enzh:enzhs){
					String en = enzh.getEn();
					String zh = enzh.getZh();
					Paragraph para = getPara(getCouple(en, zh, name)); 
					chapterSentence.add(para);
				}
				round++;
			}
		}
		documentWord.add(chapterWord);//添加到Word PDF
		documentSen.add(chapterSentence);//添加到Sentence PDF
	}
	private void after(){
		documentWord.close();
		documentSen.close();
	}

	/**
	 * 将音标分割为一个一个的Chunk，用list返回
	 * @param str
	 * @param zhFont
	 * @return
	 * @throws Exception
	 */
	private ArrayList<Chunk> getIpaList(String str, Font zhFont) throws Exception {
		//获取音标的输出字体
		String path = getPath("lingoes2.ttf");
		
		BaseFont baseFont = BaseFont.createFont(path,BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
		Font ipaFont = new Font(baseFont, 14, Font.NORMAL, BaseColor.BLACK);
		
		Pattern pattern = Pattern.compile("\\[[^\u4e00-\u9fa5]*?\\]");
		Matcher matcher = pattern.matcher(str);
		ArrayList<String> ipa = new ArrayList<>();
		ArrayList<String> zhpart = new ArrayList<>();
		int left = 0;
		while(matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			String substring = str.substring(start, end);
			ipa.add(substring);
			String substring2 = str.substring(left,start);
			zhpart.add(substring2);
			left = end;
		}
		zhpart.add((String) str.subSequence(left, str.length()));
		int i = 0;
		ArrayList<Chunk> list = new ArrayList<>();
		for(;i<ipa.size();i++){
			Chunk chunk = new Chunk(zhpart.get(i),zhFont);
			Chunk chunk2 = new Chunk(ipa.get(i),ipaFont);
			list.add(chunk);
			list.add(chunk2);
		}
		
		return list;
	}
	/**
	 * 返回带音标的释义
	 * @param str
	 * @param zhFont
	 * @return
	 * @throws Exception
	 */
	private Element dealIPA(String str, Font zhFont) throws Exception {
		//获取音标的输出字体
		String path = getPath("lingoes2.ttf");
		
		BaseFont baseFont = BaseFont.createFont(path,BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
		Font ipaFont = new Font(baseFont, 14, Font.NORMAL, BaseColor.BLACK);
		
		Pattern pattern = Pattern.compile("\\[[^\u4e00-\u9fa5]*?\\]");
		Matcher matcher = pattern.matcher(str);
		ArrayList<String> ipa = new ArrayList<>();
		ArrayList<String> zhpart = new ArrayList<>();
		int left = 0;
		while(matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			String substring = str.substring(start, end);
			ipa.add(substring);
			String substring2 = str.substring(left,start);
			zhpart.add(substring2);
			left = end;
		}
		zhpart.add((String) str.subSequence(left, str.length()));
		Paragraph paragraph = new Paragraph();
		int i = 0;
		for(;i<ipa.size();i++){
			Chunk chunk = new Chunk(zhpart.get(i),zhFont);
			Chunk chunk2 = new Chunk(ipa.get(i),ipaFont);
			paragraph.add(chunk);
			paragraph.add(chunk2);
		}
			paragraph.add(new Chunk(zhpart.get(i),zhFont));
		
		return paragraph;
	}

	/**
	 * 获取某个资源的路径
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	private String getPath(String name) throws Exception{
		URL url = this.getClass().getClassLoader().getSystemResource(name);
		URI uri = url.toURI();
		File file = new File(uri);
		String path = file.getPath();
		return path;
	}

	
	private Paragraph getPara(Couple couple) throws Exception{
		String[] parts = couple.getParts();
		String key = couple.getKey();
		String zh = couple.getZh();
		
		Font partFont = new Font(FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.BLACK);
		
		String path = getPath("SIMKAI.TTF");
		BaseFont baseFont = BaseFont.createFont(path,BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
		Font zhFont = new Font(baseFont, 14, Font.NORMAL, BaseColor.BLACK);
		Font keyFont = new Font(baseFont, 14, Font.NORMAL, BaseColor.RED);
		Paragraph paragraph = new Paragraph();
		int length = parts.length;
		for(int i=0;i<length;i++){
			String part = parts[i];
			Chunk chunk = new Chunk(part,zhFont);
			paragraph.add(chunk);
			if(i!=length-1){
				Chunk keyChunk = new Chunk(key,keyFont);
				paragraph.add(keyChunk);
			}
		}
		
		Chunk zhChunk = new Chunk("\r\n"+zh,zhFont);
		paragraph.add(zhChunk);
		return paragraph;
	}

	private Couple getCouple(String en,String zh,String key){
		
		String keyTrue="";
		boolean flag = true;
		Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(en);
		int left = 0;
		ArrayList<String> list = new ArrayList<String>();
		while(matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			if(flag){
				keyTrue = (String) en.subSequence(start, end);
			}
			String substring = "";
			if(start!=0){
				substring = en.substring(left, start);
			}
			list.add(substring);
			left = end;
		}
		if(left==en.length()){
			list.add("");
		}else{
			list.add(en.substring(left,en.length()));
		}
		
		String[] split = new String[list.size()];
		for(int i=0;i<split.length;i++){
			split[i] = list.get(i);
		}
		return new Couple(keyTrue,split,zh);
	}
}
