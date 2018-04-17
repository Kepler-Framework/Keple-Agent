package com.kepler.connection.location;

import java.util.List;

import com.kepler.host.Host;

/**
 * @author KimShen
 *
 */
public interface DelegateLocation {

	/**
	 * 获取位置路径集合
	 * 
	 * @param host
	 * @return
	 */
	public List<String> locations(Host host) throws Exception;
}
