package com.wordpdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.wordpdf.core.ExtractMsg;


public class WordPdf {
	public static void main(String[] args) throws IOException, InterruptedException {
		Scanner scanner = new Scanner(System.in);
		System.err.println("输入资源路径:");
		String p ;
		ArrayList<File> res = new ArrayList<>();
		while(!(p = scanner.nextLine()).trim().toLowerCase().equals("y")){
			res.add(new File(p));
		}
		System.err.println("输入保存路径:");
		String savelocation = scanner.next().trim();
		ExtractMsg extractMsg = new ExtractMsg(res,new File(savelocation));
		extractMsg.extract();
	}
}
