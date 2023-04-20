package com.simple.service;

import com.simple.AbstractHello;

public class Hello extends AbstractHello {
	@Override
	protected String getMsg() {
		return "Service Hello";
	}
}
