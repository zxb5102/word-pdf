package com.wordpdf.core;

import java.util.concurrent.RecursiveTask;

public class Task extends RecursiveTask<String>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String[] strs;
	public Task(String[] strs){
		this.strs = strs; 
	}
	
	@Override
	protected String compute() {
		// TODO Auto-generated method stub
		return null;
	}

}
