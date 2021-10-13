
package com.ilhsk.p4.sample;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ilhsk.p4.sample.ListFilesDemo.DemoProgressCallback;
import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IMapEntry.EntryType;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.impl.generic.client.ClientView;
import com.perforce.p4java.impl.generic.client.ClientView.ClientViewMapping;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.generic.core.ChangelistSummary;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.option.changelist.SubmitOptions;
import com.perforce.p4java.server.IOptionsServer;

/**
 * @Auth ilhsk
 * @Description
 * 
 *              <pre></pre>
 */
public class FileSync extends P4JavaDemo {
	public static void main(String[] args) {
		IOptionsServer server = null;
		String repo = "//depot/DB/Web/Docs/test/test/...";
		try {

			server = getOptionsServer(null, null);
			server.registerProgressCallback(new DemoProgressCallback());

			server.setUserName(userName);
			server.login(password);
			System.out.println("Depot files on Perforce server at URI '" + serverUri + "':");
			
			IClient client = server.getClient("sample");			
			if (client == null) {
				System.out.println("null client");
				client = new Client(server);
				client.setName("sample");
				client.setRoot("D:/p4test/test/");
				//client.setServer(server);
				server.setCurrentClient(client);
				// client = server.getClient(clientName);
			}else {
				client.setName("trans_potal_user_ilhsk");
				client.setRoot("D:/p4test/test/");
				client.setServer(server);
				server.setCurrentClient(client);
			}
			if(client ==null) {
				
				server.disconnect();
				throw new IOException("temp client null");
			}
			
			
			
			// Creating Client View entry
			ClientViewMapping tempMappingEntry = new ClientViewMapping();
			
			// Setting up the mapping properties
			tempMappingEntry.setLeft(repo);			
			tempMappingEntry.setRight("//" + client.getName() + "/...");
			tempMappingEntry.setType(EntryType.INCLUDE);
			/*
			tempMappingEntry.setRight("//" + client.getName() + "/test99.txt");
			tempMappingEntry.setType(EntryType.INCLUDE);
			tempMappingEntry.setLeft("//depot/DB/Web/Docs/test/test99.txt");
			 
			 */
			
			// Creating Client view
			ClientView tempClientView = new ClientView();
			
			// Attaching client view entry to client view
			tempClientView.addEntry(tempMappingEntry);
			client.setClientView(tempClientView);
			
			// Registering the new client on the server
			server.createClient(client);
			
			
			
			//
		
			try {
				/// 파일 동기화1
				 List<IFileSpec> fileSpecsSet = FileSpecBuilder.makeFileSpecList(repo);
				// tempClient.sync(FileSpecBuilder.getValidFileSpecs(fileSpecsSet), true, false, false, false);
				// 파일 동기화 끝1
				// 파일 동기화 2
				//List<IFileSpec> list = server.getDepotFiles(FileSpecBuilder.makeFileSpecList(new String[] { repo }), false);
				
				/*
				FileUtils.forceDelete(new File(client.getRoot()));
				List<IFileSpec> fileList = server.getDepotFiles(FileSpecBuilder.makeFileSpecList(new String[] { repo+"@653000" }), false);
				client.sync(fileList, true, false, false, false); // 파일 가져오기
				*/
				// 파일 동기화 2
				
				
					
				// Syncing up the client
				// tempClient.sync(FileSpecBuilder.getValidFileSpecs(fileList), true, false, false, false);

				// uncomment above line, n comment below if you are cloning only few files
				// tempClient.sync(fileList, true, false, false, false); //파일 가져오기
				//tempClient.editFiles(fileList, false, false, 0, null);
				//파일 만들기
				
				
				server.updateClient(client);			
				List<IFileSpec> fileList = server.getDepotFiles(FileSpecBuilder.makeFileSpecList(new String[] { repo  }), false);
				client.sync(fileList, true, false, false, false); // 파일 가져오기
				client.update();
				server.updateClient(client);
				client.refresh();
				
				IChangelist changeList  = FileSync.createNewChangeList(server,client);
				/* 파일 추가
				AddFilesOptions addFilesOptions = new AddFilesOptions().setChangelistId(changeList.getId());
				
				String filePath = FileSync.fileCreate("D:\\p4test", "test8.txt", "test!");
				File file = new File(filePath);
				String[] addFile = new String[1];
				addFile[0] = file.getAbsolutePath();
				
				
				
				List<IFileSpec> fileSpecs = FileSpecBuilder.makeFileSpecList(addFile);				
				List<IFileSpec> addList = client.addFiles(fileSpecs, addFilesOptions);
				
				System.out.println("------------------------------[2]");
				if (addList != null) {
					for (IFileSpec fileSpec : addList) {
						if (fileSpec != null) {
							if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {
								System.out.println("add file: " + ListFilesDemo.formatFileSpec(fileSpec));
							} else {
								System.err.println("message : "+fileSpec.getStatusMessage());
							}
						}
					}
				}
				 */
				System.out.println("------------------------------------------------------------------------------------------------");
				System.out.println("------------------------------------------------------------------------------------------------");
				System.out.println("------------------------------------검사------------------------------------------------------------");
				String filePath1 = FileSync.fileCreate("D:/p4test/test/", "test7.txt", "test!");
				File file1 = new File("D:/p4test/test/test7.txt");
				String filePath2 = FileSync.fileCreate("D:/p4test/test/", "test8.txt", "test!");
				File file2 = new File("D:/p4test/test/test8.txt");
				List<String> files  = new ArrayList<>();
				files.add(file1.getAbsolutePath());
				files.add(file2.getAbsolutePath());
						
				
				List<String> noneCommit = new ArrayList<>();
				List<IFileSpec> fileSpecsResult = FileSync.commitFileList("//퍼포스위치/", "D:/p4test/test/", client, files, noneCommit, changeList.getId(), false);
				
				
				System.out.println("----------------------------------------list--------------------------------------------------------");
	            for (IFileSpec fileSpec : changeList.getFiles(true)) {
	                FileSpecOpStatus status = fileSpec.getOpStatus();
	               
                	
	                if (!FileSpecOpStatus.VALID.equals(status) && !FileSpecOpStatus.INFO.equals(status)) {
	                    String statusMessage = fileSpec.getStatusMessage();
	                    throw new IOException("Failed opening file for editing: : '" + statusMessage + "'");
	                }else {
	                	System.out.println("--");
	                	System.out.println("!>>  "+fileSpec.toString());
	                 	System.out.println("!>>  "+status.toString());
	                 	System.out.println("--");
	                }
	            }
	            System.out.println("------------------------------------------------------------------------------------------------");
				
	           // fileSpecsResult = client.editFiles(fileSpecs1, false, false, changeList.getId(), null);
	            changeList.update();
				
				changeList.setDescription("javap4 Test add update1\r\n ㅅㅅㅅ");
				IChangelist changelist = server.getChangelist(changeList.getId());	            
	            changelist.getFiles(true);
	            
	            SubmitOptions submitOptions = new SubmitOptions("-f revertunchanged");	            				
				List<IFileSpec> submitFiles = changeList.submit(submitOptions);
				if (submitFiles != null) {
					for (IFileSpec fileSpec : submitFiles) {
						if (fileSpec != null) {
							if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {
								System.out.println("submitted: " + ListFilesDemo.formatFileSpec(fileSpec));
								
							} else if (fileSpec.getOpStatus() == FileSpecOpStatus.INFO){
								System.out.println("Info : "+fileSpec.getStatusMessage());
							} else if (fileSpec.getOpStatus() == FileSpecOpStatus.ERROR){
								System.err.println("Error : "+fileSpec.getStatusMessage());	
								System.out.println("-------fix-------");
								client.revertFiles(changelist.getFiles(true), false, changeList.getId(), false, false);
								server.deletePendingChangelist(changeList.getId() );
							}
						}
					}
				}
				
						
				System.out.println("[3]");			
				
				
				/*
				String filePath2 = FileSync.fileCreate("D:\\p4test", "test7.txt", "test!");
				File file1 = new File("D://p4test/test7.txt");
				
				List<IFileSpec> fileSpecs1 = FileSpecBuilder.makeFileSpecList(file1.getAbsolutePath());
					
				List<Map<String, Object>> resultMaps = server.execMapCmdList(
						CmdSpec.EDIT,
						Parameters.processParameters(
								 new EditFilesOptions(
											false, false, changeList.getId(), null), fileSpecs1, server),
						null);
				List<IFileSpec> resultList = new ArrayList<IFileSpec>();
				if (resultMaps != null) {
					for (Map<String, Object> map : resultMaps) {
						System.out.println(map);
					}
				}*/
			} finally {
				System.out.println("[4]");
				// Removing the temporary client from the server
				//IChangelist defaultChangeList = server.getChangelist(IChangelist.DEFAULT);
				//System.out.println(defaultChangeList.getId());		
				
				//System.out.println(server.deleteClient(client.getName(), false));
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					System.out.println("disconnect");
					server.disconnect();
				} catch (Exception e) {
					System.err.println(e.getLocalizedMessage());
				}
			}
		}
		

	}
	
	
	public static List<IFileSpec> commitFileList(String repoPath, String savePath, IClient client, List<String> fileList, List<String> noneCommit, int changeListid ,boolean isReadCommit ) throws RequestException, ConnectionException, AccessException{
		List<IFileSpec> fileSpecs1 = FileSpecBuilder.makeFileSpecList(fileList);
		boolean isCommit = true;
		
		 List<IFileSpec> fileSpecsResult = client.editFiles(fileSpecs1, false, false, changeListid, null);
		
         for (IFileSpec fileSpec : fileSpecsResult) {
             FileSpecOpStatus status = fileSpec.getOpStatus();
             if (!FileSpecOpStatus.VALID.equals(status) && !FileSpecOpStatus.INFO.equals(status)) {
                 String statusMessage = fileSpec.getStatusMessage();                 
             }else {
            	String resultPath = fileSpec.toString();
            	resultPath = resultPath.replace("INFO: ", "");
            	if(StringUtils.indexOf(resultPath, "locked by")> -1) {
            		isCommit = false;            		
            		noneCommit.add(diskFilePath(resultPath.split(" ")[0],repoPath ,savePath));
            	}else if(isReadCommit && StringUtils.indexOf(resultPath, "opened by")> -1) {
            		isCommit = false;            		
            		noneCommit.add(diskFilePath(resultPath.split(" ")[0],repoPath ,savePath));
            	}
             }
         }
         List<String> newFileList = new ArrayList<>();
         if(!isCommit) { //커밋하지 못하는 대상이 있을경우
        	for (String filePath : fileList) {
        		boolean isExclude = false;
        		for(String noneFilePath : noneCommit) {
        			
        			File noneFile = new File(noneFilePath);
        			
        			if(StringUtils.equals(filePath, noneFile.getAbsolutePath())) {
        				isExclude = true;
        				break;
        			}
        		}
        		if(!isExclude) {
        			newFileList.add(filePath);
        		}
			}
        	List<IFileSpec> revertFiles = FileSpecBuilder.makeFileSpecList(noneCommit);
        	
        	client.revertFiles(revertFiles, false, changeListid, false, false);
        	System.out.println("---------------- 다음 스텝 ---------------");
        	return FileSync.commitFileList(repoPath, savePath, client, newFileList, noneCommit, changeListid, isReadCommit);
         }
		return fileSpecsResult;
	}
	
	public static String diskFilePath(String filePath, String repoPath, String savePath) {
		return  savePath + filePath.replace(repoPath, "");
	}
	
	
	
	
	public static  IChangelist createNewChangeList(IOptionsServer server, IClient client) throws IOException {
		
        try {
            ChangelistSummary summary = new ChangelistSummary(IChangelist.UNKNOWN, client.getName(),
                    server.getUserName(), ChangelistStatus.NEW, new Date(),
                    "test", false);
            IChangelist newChangeList = new Changelist(summary, server, false);
            newChangeList = client.createChangelist(newChangeList);
           
            return newChangeList;
        } catch (P4JavaException e) {
            throw new IOException("Perforce execution failed: '" + e.getMessage() + "'", e);
        }
    }

	public static String fileCreate(String path, String fileName, String text) {
		String fullpath = path +"\\"+fileName;
        
        File file = new File(fullpath);
        file.setWritable(true);
        FileWriter writer = null;
        
        try {
            // 기존 파일의 내용에 이어서 쓰려면 true를, 기존 내용을 없애고 새로 쓰려면 false를 지정한다.
            writer = new FileWriter(file, true);
            writer.write(text);
            writer.flush();
            
            System.out.println("DONE");
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null) writer.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return fullpath;




	}

}
