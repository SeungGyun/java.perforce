/**
 * @File fileSync.java
 * @package netmarble.neo.tran.p4
 * @Description
 * @Modification Information
 * @ProjectName NetmarbleNeo Project
 * @Author ilhsk
 * @Since 2019. 1. 18.
 * @Copyright Copyright (c) 2019 NetmarbleNeo, Corp. All Rights Reserved.
 */
package com.ilhsk.p4.sample;

import com.ilhsk.p4.util.PerforceClient;

import lombok.extern.slf4j.Slf4j;

/**
 * @Auth ilhsk
 * @Description
 * 
 *              <pre></pre>
 */
@Slf4j
public class P4CommitTest extends P4JavaDemo {
	public static void main(String[] args) {
		PerforceClient.sync("//depot/DB/Web/Docs/test/test/...", "D:/p4test2" , "" ,"hycho");

	}
		

}
