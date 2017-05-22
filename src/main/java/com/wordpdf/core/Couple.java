package com.wordpdf.core;

public class Couple {

	private String key;
	private String[] parts;
	private String zh;
	public Couple(String key, String[] parts, String zh) {
		super();
		this.key = key;
		this.parts = parts;
		this.zh = zh;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String[] getParts() {
		return parts;
	}
	public void setParts(String[] parts) {
		this.parts = parts;
	}
	public String getZh() {
		return zh;
	}
	public void setZh(String zh) {
		this.zh = zh;
	}
}
