/**
 * 
 */
package com.ilhsk.p4.sample;

import java.text.SimpleDateFormat;
import java.util.List;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.GetChangelistsOptions;
import com.perforce.p4java.server.IOptionsServer;

/**
 * Simple P4Java changelist listing sample / demo class.<p>
 * 
 * Program retrieves and logs into a Perforce server, then
 * retrieves a maximum of ten changelist summaries as a list from the
 * server for the named user. The summary list is iterated across and used
 * to also get each full changelist object back from the server, allowing
 * the app to print the associated list of files for each changelist.
 */
public class ListChangelistsDemo extends P4JavaDemo {

	public static void main(String[] args) {
		try {		
			IOptionsServer server = getOptionsServer(null, null);
			
			server.setUserName(userName);
			server.login(password);
			
			 List<IFileSpec> fileSpecsSet = FileSpecBuilder.makeFileSpecList("//depot//...");
			 
			/*	
			List<IChangelistSummary> changelistList = server.getChangelists(
					null, 
					new GetChangelistsOptions().setUserName(userName).setMaxMostRecent(10)
					);
			*/
			 
			 List<IChangelistSummary> changelistList = server.getChangelists(
					 fileSpecsSet, 
						new GetChangelistsOptions().setMaxMostRecent(20).setLongDesc(true)
						);
			 
			 
			 
			 
			if (changelistList != null) {
				for (IChangelistSummary changelistSummary : changelistList) {
					if (changelistSummary == null) {
						System.err.println("null in latest changelists");
						continue;
					}
					//System.out.println(formatChangelist(changelistSummary));
					System.out.println(">>");
					IChangelist changelist = server.getChangelist(changelistSummary.getId());
					
					List<IFileSpec> fileList = changelist.getFiles(true);
					
					System.out.println("::::::::::::::::::: file List  :::::::::::::::::::::::::");
					
					for (IFileSpec fileSpec : fileList) {
						if (fileList == null) {
							System.err.println("null filespec in filespec list");
							continue;
						}
						if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {
							System.out.println("\t" + ListFilesDemo.formatFileSpec(fileSpec));
						} else {
							//System.err.println(fileSpec.getStatusMessage());
						}
					}
					
					
					/*
					for (IFileSpec fileSpec : fileList) {
						if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {
							System.out.println("\t" + ListFilesDemo.formatFileSpec(fileSpec));
						} else {
							System.err.println(fileSpec.getStatusMessage());
						}
					}
					
					System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
					
					try {
			            if (changelistSummary.getId() != IChangelist.DEFAULT) {
			                if (!ChangelistStatus.SUBMITTED.equals(server.getChangelist(changelistSummary.getId()).getStatus())) {
			                	server.deletePendingChangelist(changelistSummary.getId() );				                
			                }
			            }
			        } catch (P4JavaException e) {
			        	System.out.println("Perforce execution failed: '" + e.getMessage() + "'");
			        }
			        */
					
					
				}
			}
		} catch (P4JavaException jexc) {
			System.err.println(jexc.getLocalizedMessage());
			jexc.printStackTrace();
		} catch (Exception exc) {
			System.err.println(exc.getLocalizedMessage());
			exc.printStackTrace();
		}
	}
	
	protected static String formatChangelist(IChangelistSummary changelist) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return changelist.getId() + " " + simpleDateFormat.format(changelist.getDate()) 
						+ " " + changelist.getClientId()
						+ " " + changelist.getDescription();
	}
}
