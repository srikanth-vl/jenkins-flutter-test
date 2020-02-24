package com.vassarlabs.proj.uniapp.launch.test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;
import com.vassarlabs.common.fileupload.pojo.impl.FileUploadDetails;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.prod.cassandra.config.CassandraConfiguration;
import com.vassarlabs.prod.common.utils.AspectConfig;
import com.vassarlabs.prod.common.utils.DateUtils;
//import com.vassarlabs.prod.kafka.common.ConsumerCreator;
//import com.vassarlabs.prod.kafka.common.ProducerCreator;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.IFileUploadConstants;
import com.vassarlabs.proj.uniapp.generic.upload.service.EntityMetaDataFileUploadService;
import com.vassarlabs.proj.uniapp.generic.upload.service.FieldMetaDataUploadService;
import com.vassarlabs.proj.uniapp.generic.upload.service.MasterDataFileUploadService;
import com.vassarlabs.proj.uniapp.generic.upload.service.UserFileUploadService;
import com.vassarlabs.proj.uniapp.generic.upload.service.UserProjectMappingUploadService;
@Component
@ComponentScan("com.vassarlabs")
@Configuration
@Import(CassandraConfiguration.class)
@EnableCassandraRepositories(basePackages = "com.vassarlabs.proj.uniapp.dsp.repository")
public class UniAppLaunchService {

	ObjectMapper objectMapper = new ObjectMapper();

	@Autowired MasterDataFileUploadService masterDataUpload;
	@Autowired UserFileUploadService userFileUpload;
	@Autowired UserProjectMappingUploadService mappingUploadService;
	@Autowired EntityMetaDataFileUploadService entityMetaDataFileUploadService;
	@Autowired FieldMetaDataUploadService<?> fieldMetaDataUploadService;
	@Autowired
	@Qualifier("threadPoolTaskExecutor")
	ThreadPoolTaskExecutor taskExecutor;
 
	public static void main(String args[]) {

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(UniAppLaunchService.class);
		ctx.register(AspectConfig.class);
		UniAppLaunchService launchService = ctx.getBean(UniAppLaunchService.class);
		try {
			launchService.test();
			launchService.readFiles(args[0]);
			launchService.shutdownThreadTaskService();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidInputException e) {
			e.printStackTrace();
		}
		((AnnotationConfigApplicationContext)ctx).close();		
	}

	private void readFiles(String args) throws IOException, InvalidInputException, InvalidInputException, InvalidInputException  {
		String filePath = args + "/files";
		String configPath = args + "/config";
		String errorFilePath = filePath + "/errors/";
		File errorFolder = new File(errorFilePath);
		if (!errorFolder.exists()){
			errorFolder.mkdir();
		}
		File folder = new File(filePath);
		Map<String, String> uploadPendingMap = new TreeMap<>();
		String userConfigPath = null, masterDataConfigPath = null, mappingConfigPath = null, fieldMetaConfigPath = null, entityConfig = null;
		for (File fileEntry : folder.listFiles()) {
			if(!(fileEntry.isHidden())) {
				if(fileEntry.getName().toLowerCase().contains("master_data")) {
					uploadPendingMap.put("03" + fileEntry.getName(), fileEntry.getAbsolutePath());
				} else if (fileEntry.getName().toLowerCase().contains("user_project_mapping")) {
					uploadPendingMap.put("04" + fileEntry.getName(), fileEntry.getAbsolutePath());
				} else if (fileEntry.getName().toLowerCase().contains("user_data")) {
					uploadPendingMap.put("02" + fileEntry.getName(), fileEntry.getAbsolutePath());
				} else if (fileEntry.getName().toLowerCase().contains("field_meta_data")) {
					uploadPendingMap.put("01" + fileEntry.getName(), fileEntry.getAbsolutePath());
				} else if (fileEntry.getName().toLowerCase().contains("entity_meta_data")) {
					uploadPendingMap.put("05" + fileEntry.getName(), fileEntry.getAbsolutePath());
				} 
			}
		}
		folder = new File(configPath);
		for (File fileEntry : folder.listFiles()) {
			if(!(fileEntry.isHidden())) {
				if(fileEntry.getName().contains("user-details-upload.json")) {
					userConfigPath = fileEntry.getAbsolutePath();
				} else if(fileEntry.getName().contains("master-data-upload.json")) {
					masterDataConfigPath = fileEntry.getAbsolutePath();
				} else if(fileEntry.getName().contains("user-proj-mapping-upload.json")) {
					mappingConfigPath = fileEntry.getAbsolutePath();
				} else if(fileEntry.getName().contains("field-meta-data-upload.json")) {
					fieldMetaConfigPath = fileEntry.getAbsolutePath();
				} else if(fileEntry.getName().contains("entity-meta-data-upload.json")) {
					entityConfig = fileEntry.getAbsolutePath();
				}
			}
		}
		long startTS = System.currentTimeMillis();
		for(String fileName : uploadPendingMap.keySet()) {
			if(fileName.toLowerCase().contains("master_data")) {
				IFileUploadDetails fileUploadDetails = createFileUploadObbject(uploadPendingMap.get(fileName), fileName, errorFilePath, CommonConstants.MASTER_DATA_CLASSNAME, masterDataConfigPath);
				if(masterDataConfigPath == null) {
					System.out.println("No launch file path found");
				} else {
					masterDataUpload.uploadFile(fileUploadDetails);
				}
			} else if(fileName.toLowerCase().contains("user_data")) {
				IFileUploadDetails fileUploadDetails = createFileUploadObbject(uploadPendingMap.get(fileName), fileName, errorFilePath, CommonConstants.USER_DATA_CLASSNAME, userConfigPath);
				if(userConfigPath == null) {
					System.out.println("No launch file path found");
				} else {
					userFileUpload.uploadFile(fileUploadDetails);
				}
			} else if(fileName.toLowerCase().contains("user_project_mapping")) {
				IFileUploadDetails fileUploadDetails = createFileUploadObbject(uploadPendingMap.get(fileName), fileName, errorFilePath, CommonConstants.USER_MAPPING_CLASSNAME, mappingConfigPath);

				if(mappingConfigPath == null) {
					System.out.println("No launch file path found");
				} else {
					mappingUploadService.uploadFile(fileUploadDetails);
				}
			} else if(fileName.toLowerCase().contains("field_meta_data")) {
				IFileUploadDetails fileUploadDetails = createFileUploadObbject(uploadPendingMap.get(fileName), fileName, errorFilePath, CommonConstants.FIELDMETADATA_CLASSNAME, fieldMetaConfigPath);
				fileUploadDetails.setDelimiter("\\|");
				if(fieldMetaConfigPath == null) {
					System.out.println("No launch file path found");
				} else {
					fieldMetaDataUploadService.uploadFile(fileUploadDetails);
				}
			}
			else if(fileName.toLowerCase().contains("entity_meta_data")) {
				IFileUploadDetails fileUploadDetails = createFileUploadObbject(uploadPendingMap.get(fileName), fileName, errorFilePath, CommonConstants.ENTITY_META_DATA_CONFIG_CLASSNAME, entityConfig);
//				fileUploadDetails.setDelimiter("\\|");
				if(fieldMetaConfigPath == null) {
					System.out.println("No launch file path found");
				} else {
					entityMetaDataFileUploadService.uploadFile(fileUploadDetails);
				}
			}
		}
		System.out.println("File Upload Completed :: Total Time taken - " + (System.currentTimeMillis() - startTS));
	}

	private IFileUploadDetails createFileUploadObbject(String filePath, String fileName, String errorFilePath, String className, String configPath) throws JsonProcessingException, IOException {

		IFileUploadDetails fileUploadDetails = new FileUploadDetails();
		fileUploadDetails.setFileName(fileName);
		fileUploadDetails.setClassName(className);
		fileUploadDetails.setFileFullPath(filePath);
		fileUploadDetails.setBatchSize(CommonConstants.MAX_BATCH_SIZE);
		fileUploadDetails.setDelimiter("#");
		fileUploadDetails.setErrorFileName(errorFilePath + fileName + "_error_" + System.currentTimeMillis() + ".csv");
		JsonNode jsonNode = objectMapper.readTree(new File(configPath));
		Properties properties = new Properties();
		properties.setProperty(CommonConstants.JSON_CONFIG, objectMapper.writeValueAsString(jsonNode));
		properties.setProperty(IFileUploadConstants.MasterDataUploadConstants.DATE, String.valueOf(DateUtils.getYYYYMMdd(System.currentTimeMillis())));
		fileUploadDetails.setProperties(properties);
		return fileUploadDetails;
	}

	private void test() throws JsonProcessingException {
		
	}

	private void shutdownThreadTaskService() {

		try {
			if (!taskExecutor.getThreadPoolExecutor().awaitTermination(5000000, TimeUnit.SECONDS)) {
				System.out.println("Executor still not terminate after waiting time...");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}