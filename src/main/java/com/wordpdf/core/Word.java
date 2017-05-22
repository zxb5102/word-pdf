package com.wordpdf.core;

import java.util.ArrayList;

public class Word {

	private String name;
	private Integer count;
	private Double per;
	private String example;
	private String specific;
	private ArrayList<EnZh> enzhs;
	
	public ArrayList<EnZh> getEnzhs() {
		return enzhs;
	}

	public void setEnzhs(ArrayList<EnZh> enzhs) {
		this.enzhs = enzhs;
	}

	public String getSpecific() {
		return specific;
	}

	public void setSpecific(String specific) {
		this.specific = specific;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public Word(String name) {
		this.name = name;
		this.count = 0;
		this.per = 0d;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Double getPer() {
		return per;
	}

	public void setPer(Double per) {
		this.per = per;
	}

	@Override
	public String toString() {
		return "Word [name=" + name + ", count=" + count + ", per=" + per + "]";
	}

}
