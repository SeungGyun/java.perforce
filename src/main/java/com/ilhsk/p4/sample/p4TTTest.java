
package com.ilhsk.p4.sample;

import com.ilhsk.p4.util.PerforceClient;

/**
 * @Description 
 * <pre></pre>
 * @Auth ilhsk    
 */
public class p4TTTest {

	/**
	 * @Description 
	 * <pre></pre>
	 * @Create_Date 2019. 3. 13.
	 * @Auth ilhsk ilhsk@nm-neo.com
	 * @param args   
	 */
	public static void main(String[] args) {
		PerforceClient.sync("//depotest/...", "D:\\fileTemp\\tt", "","id");

	}
}
