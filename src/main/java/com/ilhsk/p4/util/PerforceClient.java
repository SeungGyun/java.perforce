
package com.ilhsk.p4.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelistSummary;
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
import com.perforce.p4java.option.UsageOptions;
import com.perforce.p4java.option.changelist.SubmitOptions;
import com.perforce.p4java.option.client.AddFilesOptions;
import com.perforce.p4java.option.server.GetChangelistsOptions;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.ServerFactory;
import com.perforce.p4java.server.callback.IProgressCallback;

public class PerforceClient {
	private static final Logger logger = LoggerFactory.getLogger(PerforceClient.class);

	protected static String serverUri = System.getProperty("com.perforce.p4javademo.serverUri", "p4java://127.0.0.1:1666");

	/**
	 * The user name to be used for the demo run. This is retrieved
	 * from the current value of the system property com.perforce.p4javademo.userName;
	 * if no such system property has been set, the default used is
	 * "P4javaDemoUser".
	 */
	protected static String userName = System.getProperty("com.perforce.p4javademo.userName", "test");

	/**
	 * The Perforce client name to be used for the demo run. This is retrieved
	 * from the current value of the system property com.perforce.p4javademo.clientName;
	 * if no such system property has been set, the default used is
	 * "p4javademo".
	 */
	protected static String clientName = System.getProperty("com.perforce.p4javademo.clientName", "test_xx");

	/**
	 * The Perforce user password to be used for the demo run. This is retrieved
	 * from the current value of the system property com.perforce.p4javademo.password;
	 * if no such system property has been set, the default used is
	 * "none" (which is not the same as not having a password).
	 */
	protected static String password = System.getProperty("com.perforce.p4javademo.password", "test");

	protected static class P4ProgressCallback implements IProgressCallback {

		public void start(int key) {
			if(logger.isDebugEnabled())logger.debug("Starting command  : {} " + key);
		}

		public void stop(int key) {
			if(logger.isDebugEnabled())logger.debug("Stopping command  : {} " + key);
		}

		public boolean tick(int key, String tickMarker) {
			if (tickMarker != null) {
				if(logger.isDebugEnabled())logger.debug("tick key {} - tickMarker {} ", key, tickMarker);
			}
			return true;
		}
	}
	
	protected static IServer getServer(Properties props) throws P4JavaException, URISyntaxException {
		IServer server = ServerFactory.getServer(serverUri, props);
		if (server != null) {
			server.connect();
		}
		return server;
	}
	
	protected static IOptionsServer getOptionsServer(Properties props, UsageOptions opts) throws P4JavaException, URISyntaxException {
		IOptionsServer server = ServerFactory.getOptionsServer(serverUri, props, opts);
		if (server != null) {
			server.connect();
		}
		server.registerProgressCallback(new P4ProgressCallback());
		server.setUserName(userName);
		server.login(password);
		logger.info("Depot files on Perforce server at URI '" + serverUri + "':");
		return server;
	}
	

	
	static public boolean sync(String p4RepoPath, String savePath, String revision, String userId) {
		IOptionsServer server = null;
		IClient client = null;		
		try {
			FileUtil.CreateFolder(savePath);
			server = getOptionsServer(null, null);
			client = server.getClient(clientName+userId);		
			if (client == null) {				
				client = new Client();
				client.setName(clientName+userId);
				client.setRoot(savePath);
				client.setServer(server);
				server.setCurrentClient(client);
			}else {
				client.setName(clientName+userId);
				client.setRoot(savePath);
				client.setServer(server);
				server.setCurrentClient(client);
			}
			
			if(client ==null) {				
				server.disconnect();
				logger.info("p4 disconnect");
				throw new IOException("temp client null");
			}			
			
			
			// Creating Client View entry
			ClientViewMapping clientViewMapping = new ClientViewMapping();
			String lastText = p4RepoPath.substring(p4RepoPath.lastIndexOf("/"));
			String right = "//" + client.getName() +( "/...".equals(lastText)? "/..." :  lastText) ;
			// Setting up the mapping properties
			clientViewMapping.setLeft(p4RepoPath);
			clientViewMapping.setRight(right );
			clientViewMapping.setType(EntryType.INCLUDE);
			// Creating Client view
			ClientView clientView = new ClientView();
			// Attaching client view entry to client view
			clientView.addEntry(clientViewMapping);
			client.setClientView(clientView);
			// Registering the new client on the server
			server.createClient(client);
			
			p4RepoPath = p4RepoPath + (!StringUtils.isEmpty(revision) ? "@"+revision :"");
			logger.info("p4RepoPath : {}",p4RepoPath);
			List<IFileSpec> fileList = server.getDepotFiles(FileSpecBuilder.makeFileSpecList(new String[] { p4RepoPath  }), false);
			List<IFileSpec> files = client.sync(fileList, true, false, false, false);
		} catch (Exception e) {
			if(logger.isErrorEnabled())logger.error("p4RepoPath : {}, savePath : {}, userId : {}  error message : {}", p4RepoPath, savePath,userId, e.getMessage(),e);
		} finally {
			if(client != null) {
				
			}
			if (server != null) {
				try { 
					
					server.disconnect();
					logger.info("p4 disconnect");
				} catch (Exception e) { 
					if(logger.isErrorEnabled())logger.error("p4RepoPath : {}, savePath : {}, userId : {}  error message : {}", p4RepoPath, savePath,userId, e.getMessage(),e);
				}
			}
		}
		
		
		return true;
	}
	
	/**
	 * @Auth ilhsk ilhsk@nm-neo.com
	 * @Create_Date 2020. 11. 27.
	 * @Description 
	 * <pre>커밋 리스트 정보</pre>
	 * @param p4RepoPath
	 * @param count
	 * @return   
	 */
	static public List<Map<String,Object>> getChangeList(String p4RepoPath, int count){
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		try {
			IOptionsServer server = getOptionsServer(null, null);
			List<IFileSpec> fileSpecsSet = FileSpecBuilder.makeFileSpecList(p4RepoPath);
			List<IChangelistSummary> changelistList = server.getChangelists(fileSpecsSet, 
					new GetChangelistsOptions().setMaxMostRecent(count).setLongDesc(true));
			for (IChangelistSummary iChangelistSummary : changelistList) {
				if (iChangelistSummary == null) {					
					continue;
				}
				IChangelist changelist = server.getChangelist(iChangelistSummary.getId());
				List<IFileSpec> fileList = changelist.getFiles(true);
				List<String> files = new ArrayList<String>();
				for (IFileSpec fileSpec : fileList) {
					if (fileList == null) {						
						continue;
					}
					if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {
						files.add(fileSpec.getDepotPathString());
					}
				}
				
				
				Map<String,Object> info = new HashMap<String, Object>();
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				info.put("revision", Integer.toString(iChangelistSummary.getId()));
				info.put("date", simpleDateFormat.format(iChangelistSummary.getDate()) );
				info.put("id", iChangelistSummary.getClientId() );
				info.put("desc", PerforceClient.getDesc(iChangelistSummary.getDescription()) );
				info.put("files", files );
				results.add(info);
			}
		} catch (P4JavaException e) {
			logger.debug(e.getMessage(),e);
		} catch (URISyntaxException e) {
			logger.debug(e.getMessage(),e);
		}
		return results;
	}
	
	static private String getDesc(String text) {
		try {
			text = text.replaceAll("\r\n", "").replaceAll("\r", "").replaceAll("\n", "");
			if(text.length() > 65) {
				text = text.substring(0,64);
			}
			return text;
		}catch(Exception e ) {
			logger.error(e.getMessage(),e);
		}
		return text; 
	}
	
	/**
	 * @Auth ilhsk ilhsk@nm-neo.com
	 * @Create_Date 2020. 11. 27.
	 * @Description 
	 * <pre>해당 경로 마지막 리비전 번호를 가지고 온다.</pre>
	 * @param p4RepoPath
	 * @return   
	 */
	static public String getRevision(String p4RepoPath) {		
			
		List<Map<String,Object>>  list  = PerforceClient.getChangeList(p4RepoPath, 1);
		for (Map<String, Object> map : list) {
			return map.get("revision").toString();
		}			
	
		return "";	
	}
	
	
	
	
	
	/**
	 * @Auth ilhsk ilhsk@nm-neo.com
	 * @Create_Date 2021. 1. 7.
	 * @Description 
	 * <pre>add 한파일이 오류나면 수정 쪽으로 변경한다. (싱크 받을떄 파일을 정확하게 안받을 떄가 있어 수정 인데 신규로 인식하는 경우가 발생하여 퍼포스로 명확하게 하기 위해 ) </pre>
	 * @param errorMessage
	 * @param p4RepoPath
	 * @param targetPath
	 * @param userId
	 * @param addPaths
	 * @param updatePaths
	 * @return   
	 */
	static public String changeAddAndUpdatePath(String errorMessage ,String p4RepoPath, String targetPath,  String userId, List<String> addPaths, List<String> updatePaths) {
		String filePath ="";
		try {
			filePath = errorMessage.split(" ")[0];
			String repoRootPath = p4RepoPath.replace("...", "");
			filePath = filePath.replace(repoRootPath, "");
			filePath = targetPath + filePath;
			//File errorFile = new File(filePath);
			//if (!errorFile.exists()) {
			//	logger.debug("Error file not Exists : " + errorFile.getAbsolutePath());
			//	return "Error file not Exists : " + errorFile.getAbsolutePath();
			//}
			List<String> newAddList = new ArrayList<String>();
			for(String addFilePath : addPaths) {
				if(!addFilePath.equals(filePath)) {
					newAddList.add(addFilePath);
				}else {
					logger.debug("제외 하기   >>> " + addFilePath);
				}
			}
			addPaths = newAddList;
			filePath = filePath.replaceAll("//", "/");
			updatePaths.add(filePath);			
		}catch(Exception e) {
			logger.debug(e.getLocalizedMessage(),e);
		}		
		return "add -> update change File Info : " + filePath;
	}
	/**
	 * @Description 
	 * <pre></pre>
	 * @Create_Date 2019. 7. 10.
	 * @Auth ilhsk ilhsk@nm-neo.com
	 * @param p4RepoPath 퍼포스 위치
	 * @param targetPath 저장되는 위치
	 * @param userId 유저 아이디
	 * @param addPaths 
	 * @param updatePaths
	 * @return   
	 */
	static public String commit(String p4RepoPath, String targetPath,  String userId, List<String> addPaths, List<String> updatePaths, String commet, boolean isReadUnCommit) {
		if(logger.isInfoEnabled())logger.info("====================== P4 COMMIT START ==============================");
		if(logger.isInfoEnabled())logger.info("p4RepoPath : {} targetPath : {} isReadUnCommit : {} ",p4RepoPath ,targetPath, isReadUnCommit );
		if(logger.isInfoEnabled())logger.info("addPaths : {} updatePaths : {}  ",addPaths ,updatePaths );
		if(logger.isInfoEnabled())logger.info("=====================================================================");
		StringBuilder bf = new StringBuilder();
		boolean isSuccess = false;
		
		IOptionsServer server = null;
		IClient client =  null;
		IChangelist changeList  = null;
		try {
			FileUtil.CreateFolder(targetPath);
			server = getOptionsServer(null, null);
			client = server.getClient(clientName+userId);			
			if (client == null) {				
				client = new Client();
				client.setName(clientName+userId);
				client.setRoot(targetPath);
				client.setServer(server);
				server.setCurrentClient(client);
			}else {
				client.setName(clientName+userId);
				client.setRoot(targetPath);
				client.setServer(server);
				server.setCurrentClient(client);
			}			
			if(client ==null) {				
				server.disconnect();
				logger.info("p4 disconnect");
				throw new IOException("p4 client null");
			}			
			// Creating Client View entry
			ClientViewMapping clientViewMapping = new ClientViewMapping();
			// Setting up the mapping properties
			clientViewMapping.setLeft(p4RepoPath);
			clientViewMapping.setRight("//" + client.getName() + "/...");
			clientViewMapping.setType(EntryType.INCLUDE);
			// Creating Client view
			ClientView clientView = new ClientView();
			// Attaching client view entry to client view
			clientView.addEntry(clientViewMapping);
			client.setClientView(clientView);
			// Registering the new client on the server
			server.createClient(client);
			
			changeList  = createNewChangeList(server,client);
			
			if(addPaths != null) {
				AddFilesOptions addFilesOptions = new AddFilesOptions().setChangelistId(changeList.getId());
				List<IFileSpec> fileSpecs = FileSpecBuilder.makeFileSpecList(addPaths);				
				List<IFileSpec> addList = client.addFiles(fileSpecs, addFilesOptions);
				boolean isReAddSetting =false;
				if (addList != null) {
					for (IFileSpec fileSpec : addList) {
						if (fileSpec != null) {
							if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {
								if(logger.isInfoEnabled())logger.info("add file:{}", fileSpec.getDepotPathString());
							} else {
								if(logger.isErrorEnabled())logger.error("add error message :{}", fileSpec.getStatusMessage());
								if(fileSpec.getStatusMessage().indexOf("can't add existing file") > -1) {
									
									//String changeText =  changeAddAndUpdatePath(fileSpec.getStatusMessage(), p4RepoPath, targetPath, userId, addPaths, updatePaths);
									//if(logger.isInfoEnabled())logger.info("add -> modify changeMessage :{}", fileSpec.getStatusMessage());
									//commet += System.lineSeparator()  + System.lineSeparator()  + commet+ changeText + System.lineSeparator() ;
									
								}
								isReAddSetting = true;
								
							}
						}
					}
				}
				/*
				if(isReAddSetting) {
					if(logger.isInfoEnabled())logger.info("2th Add : {} ", addPaths);
					fileSpecs = FileSpecBuilder.makeFileSpecList(addPaths);				
					addList = client.addFiles(fileSpecs, addFilesOptions);
					if (addList != null) {
						for (IFileSpec fileSpec : addList) {
							if (fileSpec != null) {
								if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {
									if(logger.isInfoEnabled())logger.info("add file:{}", fileSpec.getDepotPathString());
								} else {
									if(logger.isErrorEnabled())logger.error("2th add error message :{}", fileSpec.getStatusMessage());
								}
							}
						}
					}
				}
				*/
			}
			
			if(updatePaths != null) {
				if(logger.isInfoEnabled())logger.info("update: {} ", updatePaths);
				List<String> noneCommit = new ArrayList<>();
				List<IFileSpec> fileSpecs = commitFileList(p4RepoPath.replace("...", ""), targetPath, client, updatePaths, noneCommit, changeList.getId(), isReadUnCommit);
				StringBuilder bfnone = new StringBuilder();
				if(noneCommit.size() > 0 ) {
					bfnone.append(System.lineSeparator()).append(" ------------ 제외 목록 ------------").append(System.lineSeparator());
					for (String nonefilePath : noneCommit) {
						bfnone.append(nonefilePath.replace(targetPath, "")).append(System.lineSeparator());
					}
					commet = commet+ bfnone.toString();
				}
				
	            for (IFileSpec fileSpec : changeList.getFiles(true)) {
	                FileSpecOpStatus status = fileSpec.getOpStatus();
	                if (!FileSpecOpStatus.VALID.equals(status) && !FileSpecOpStatus.INFO.equals(status)) {
	                	if(logger.isErrorEnabled())logger.error("modify error message :{}", fileSpec.getStatusMessage());
	                }else {
	                	if(logger.isInfoEnabled())logger.info("modify file: {}", fileSpec.getDepotPathString());
	                }
	            }
			}
			logger.info("p4 commit message : "+ commet);
			changeList.setDescription("[번역툴] userId : "+ userId  + System.lineSeparator() + commet );
			changeList.update();
			
			IChangelist changelist = server.getChangelist(changeList.getId());
            
            changelist.getFiles(true);
            SubmitOptions submitOptions = new SubmitOptions("-f revertunchanged");	//https://www.perforce.com/perforce/doc.091/manuals/cmdref/submit.html		
			List<IFileSpec> submitFiles = changeList.submit(submitOptions);
			if (submitFiles != null) {
				for (IFileSpec fileSpec : submitFiles) {
					if (fileSpec != null) {
						if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {			
							bf.append("submitted : ").append(fileSpec.getDepotPathString()).append("\r\n");
							if(logger.isInfoEnabled())logger.info("submitted message :{}", fileSpec.getDepotPathString());
						} else if (fileSpec.getOpStatus() == FileSpecOpStatus.INFO){
							bf.append("info : ").append(fileSpec.getDepotPathString()).append("\r\n");
							if(logger.isInfoEnabled())logger.info("Info message :{}", fileSpec.getStatusMessage());
							
						} else if (fileSpec.getOpStatus() == FileSpecOpStatus.ERROR){
							client.unlockFiles(changeList.getFiles(true), changeList.getId(), false);
							client.revertFiles(changelist.getFiles(true), false, changeList.getId(), false, false);							
							server.deletePendingChangelist(changeList.getId() );
							bf.append("error : ").append(fileSpec.getDepotPathString()).append("\r\n");
							if(logger.isErrorEnabled())logger.error("error message :{}", fileSpec.getStatusMessage());
							
						}
					}
				}
			}
			
			
			isSuccess = true;
			
		} catch (Exception e) {
			if(logger.isErrorEnabled())logger.error("p4RepoPath : {}, savePath : {}, userId : {}  error message : {}", p4RepoPath, targetPath,userId, e.getMessage(),e);
			
		} finally {
			if (client != null && changeList  != null) {
				try {
					client.unlockFiles(changeList.getFiles(true), changeList.getId(), false);
				} catch (Exception ex) { 
					if(logger.isErrorEnabled())logger.error("unlock Error p4RepoPath : {}, savePath : {}, userId : {}  error message : {}", p4RepoPath, targetPath,userId, ex.getMessage(),ex);
				}
			}
			
			if (!isSuccess && client != null && changeList  != null) {
				try { 
					client.revertFiles(changeList.getFiles(true), false, changeList.getId(), false, false);
					
					server.deletePendingChangelist(changeList.getId() );
				} catch (Exception ex) { 
					if(logger.isErrorEnabled())logger.error("revert error p4RepoPath : {}, savePath : {}, userId : {}  error message : {}", p4RepoPath, targetPath,userId, ex.getMessage(),ex);
				}
			}
			
			if (server != null) {
				try { 
					server.disconnect();
					logger.info("p4 disconnect");
				} catch (Exception e) { 
					if(logger.isErrorEnabled())logger.error("disconnect error p4RepoPath : {}, savePath : {}, userId : {}  error message : {}", p4RepoPath, targetPath,userId, e.getMessage(),e);
				}
			}
		}		
		return bf.toString();
	}
	
	
	public static IChangelist createNewChangeList(IOptionsServer server, IClient client) throws IOException {
		
        try {
            ChangelistSummary summary = new ChangelistSummary(IChangelist.UNKNOWN, client.getName(),
                    server.getUserName(), ChangelistStatus.NEW, new Date(),
                    "Netmarble Trans", false);
            IChangelist newChangeList = new Changelist(summary, server, false);
            newChangeList = client.createChangelist(newChangeList);
           
            return newChangeList;
        } catch (P4JavaException e) {
            throw new IOException("Perforce execution failed: '" + e.getMessage() + "'", e);
        }
    }
	
	
	static public boolean sync(String[] p4RepoPaths, String savePath, String revision, String userId,String repoRoot) {
		IOptionsServer server = null;
		IClient client = null;	
		try {
			FileUtil.CreateFolder(savePath);
			server = getOptionsServer(null, null);
			 client = server.getClient(clientName+userId);			
			if (client == null) {				
				client = new Client();
				client.setName(clientName+userId);
				client.setRoot(savePath);
				client.setServer(server);
				server.setCurrentClient(client);
			}else {
				client.setName(clientName+userId);
				client.setRoot(savePath);
				client.setServer(server);
				server.setCurrentClient(client);
			}
			if(client ==null) {				
				server.disconnect();
				logger.info("p4 disconnect");
				throw new IOException("temp client null");
			}			
			ClientView clientView = new ClientView();
			
			
			for (int i =0 ; i < p4RepoPaths.length ; i++) {
				ClientViewMapping clientViewMapping = new ClientViewMapping();
				
				String right = "//" + client.getName() +"/"+ p4RepoPaths[i] ;				
				clientViewMapping.setLeft(repoRoot + p4RepoPaths[i]);
				clientViewMapping.setRight(right );
				
				
				clientViewMapping.setType(EntryType.INCLUDE);				
				clientView.addEntry(clientViewMapping);
			}			
			client.setClientView(clientView);
			// Registering the new client on the server
			server.createClient(client);
			for (int i =0 ; i < p4RepoPaths.length ; i++) {
				p4RepoPaths[i] = p4RepoPaths[i] + (!StringUtils.isEmpty(revision) ? "@"+revision :"");
			}
			
			logger.info("p4RepoPath : {}",p4RepoPaths.toString());
			
			
			
			List<IFileSpec> fileList = server.getDepotFiles(FileSpecBuilder.makeFileSpecList(p4RepoPaths), false);
			client.sync(fileList, true, false, false, false); // 파일 가져오기
			
		} catch (Exception e) {
			if(logger.isErrorEnabled())logger.error("p4RepoPath : {}, savePath : {}, userId : {}  error message : {}", p4RepoPaths.toString(), savePath,userId, e.getMessage(),e);
		} finally {
			if(client != null) {
				
			}
			if (server != null) {
				try { 
					server.disconnect();
					logger.info("p4 disconnect");
				} catch (Exception e) { 
					if(logger.isErrorEnabled())logger.error("p4RepoPath : {}, savePath : {}, userId : {}  error message : {}", p4RepoPaths.toString(), savePath,userId, e.getMessage(),e);
				}
			}
		}
		
		
		return true;
	}
	
	
	static public void deleteChangeList(String userId) {

		IOptionsServer server = null;
		IClient client =  null;
		try {
			server = getOptionsServer(null, null);
			List<IChangelistSummary> changelistList = server.getChangelists(null,new GetChangelistsOptions().setUserName(userName).setMaxMostRecent(100));
			if (changelistList != null) {
				for (IChangelistSummary changelistSummary : changelistList) {							
					if( (clientName+userId).equals(changelistSummary.getClientId())) {						
						try {
				            if (changelistSummary.getId() != IChangelist.DEFAULT) {
				                if (!ChangelistStatus.SUBMITTED.equals(server.getChangelist(changelistSummary.getId()).getStatus())) {
				                	if(logger.isInfoEnabled())logger.info("workspace_name : {}, changelist Id : {}",changelistSummary.getClientId(),changelistSummary.getId());				                	
				                	server.deletePendingChangelist(changelistSummary.getId() );				                	
				                }
				            }
				        } catch (P4JavaException e) {
				        	logger.error("p4 change list Delete Error error message : {}", e.getMessage(),e);
				        }
					}
				}
			}
		} catch (P4JavaException jexc) {
			logger.error("p4 change list Delete Error error message : {}", jexc.getMessage(),jexc);			
		} catch (Exception exc) {
			logger.error("p4 change list Delete Error error message : {}", exc.getMessage(),exc);
		} finally {
			if(client != null) {
				
			}
			if (server != null) {
				try { 
					server.disconnect();
					logger.info("p4 disconnect");
				} catch (Exception e) { 
					logger.error(e.getMessage(),e);
				}
			}
		}
	}
	
	public static List<IFileSpec> commitFileList(String repoPath, String savePath, IClient client, List<String> fileList, List<String> noneCommitList, int changeListid ,boolean isReadUnCommit ) throws RequestException, ConnectionException, AccessException{
		List<IFileSpec> fileSpecs1 = FileSpecBuilder.makeFileSpecList(fileList);
		boolean isCommit = true;
		
		 List<IFileSpec> fileSpecsResult = client.editFiles(fileSpecs1, false, false, changeListid, null);
		
         for (IFileSpec fileSpec : fileSpecsResult) {
             FileSpecOpStatus status = fileSpec.getOpStatus();
             if (!FileSpecOpStatus.VALID.equals(status) && !FileSpecOpStatus.INFO.equals(status)) {
             }else {
            	String resultPath = fileSpec.toString();
            	resultPath = resultPath.replace("INFO: ", "");
            	if(StringUtils.indexOf(resultPath, "locked by")> -1) {
            		isCommit = false;            		
            		noneCommitList.add(diskFilePath(resultPath.split(" ")[0],repoPath ,savePath));
            	}else if(isReadUnCommit && StringUtils.indexOf(resultPath, "opened by")> -1) {
            		isCommit = false;            		
            		noneCommitList.add(diskFilePath(resultPath.split(" ")[0],repoPath ,savePath));
            	}
             }
         }
         List<String> newFileList = new ArrayList<>();
         if(!isCommit) { //커밋하지 못하는 대상이 있을경우
        	logger.info("--- 제외 목록 있음 --");
        	logger.info("noneCommitList : {} ",noneCommitList);
        	for (String filePath : fileList) {
        		boolean isExclude = false;
        		for(String noneFilePath : noneCommitList) {
        			if(StringUtils.equals(filePath, noneFilePath)) {
        				isExclude = true;
        				break;
        			}
        		}
        		if(!isExclude) {
        			newFileList.add(filePath);
        		}
			}
        	List<IFileSpec> revertFiles = FileSpecBuilder.makeFileSpecList(noneCommitList);        	
        	client.revertFiles(revertFiles, false, changeListid, false, false);
        	return commitFileList(repoPath, savePath, client, newFileList, noneCommitList, changeListid, isReadUnCommit);
         }
		return fileSpecsResult;
	}
	
	public static String diskFilePath(String filePath, String repoPath, String savePath) {
		File noneFile = new File(savePath + filePath.replace(repoPath, ""));
		return noneFile.getAbsolutePath() ;
	}
	
}
