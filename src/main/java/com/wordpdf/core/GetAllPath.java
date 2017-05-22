package com.wordpdf.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class GetAllPath {
	
	/**
	 * 从目录中提取html后缀的文件
	 * @param stack
	 * @return
	 * @throws IOException
	 */
	private ArrayList<Path> f2(ArrayList<Path> stack) throws IOException {
		ArrayList<Path> values = new ArrayList<>();
		int index = stack.size();
		while(index != 0){
			Path path = stack.get(index-1);
			stack.remove(index-1);
			index--;
			DirectoryStream<Path> stream = Files.newDirectoryStream(path);
			for(Path n : stream){
				String name = n.toString();
				File file = new File(name);
				if(file.isDirectory()){
					stack.add(index,n);
					index++;
				}else{
					int indexOf = name.lastIndexOf(".");
					if(indexOf!=-1){
						String substring = name.substring(indexOf+1).toLowerCase();
						if(substring.equals("html")){
							values.add(n);
						}
					}
				}
			}
		}
		return values;
	}

	/**
	 * 判定路径是否有效，如果无效中断运行，
	 * @param files
	 * @return
	 * @throws IOException
	 */
	public ArrayList<Path> getAllPath(ArrayList<File> files) throws IOException{
		ArrayList<Path> stack = new ArrayList<>();
		for(File file:files){
			Path path = file.toPath();
			if(file.exists()){	
				stack.add(path);
			}else{
				System.err.println("输入的目录有误");
				System.exit(0);
			}
		}
		return f2(stack);
	}
}
