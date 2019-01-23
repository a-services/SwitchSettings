package io.appery.switchsettings;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.appery.rest.*;

/**
 * Switching backend environments in jQM project.
 * Comparing backend environments.
 */
public class ApperySettingsSwitcher {

	public static String propName = "aset.properties";

	public static final String SERVICE_SETTINGS = "SERVICE_SETTINGS";
	public static final String SC_SERVICE = "SC_SERVICE";
	public static final String REST_SERVICE = "REST_SERVICE";

	public static final String baseSettingsName = "Settings";
	public static final String database_id = "database_id";

	Properties pp;
	ApperyClient apperyClient;

	// See: http://www.studytrails.com/java/json/java-jackson-data-binding/
	ObjectMapper mapper;
	String username;
	String password;
	String projectName;
	ProjectItem projectItem;
	ProjectInfo projectInfo;
	List<AssetInfo> serviceSettings;

	AssetInfo targetInfo;
	AssetData targetData;
	List<String> targetKeys;

	ApperySettingsSwitcher() throws ApperyException {

		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		// Get properties from file
		try {
			pp = loadProperties(propName);
		} catch (IOException e) {
			throw new ApperyException("Properties file not found: " + propName);
		}
		username = getProperty("userName");
		password = getProperty("password");
		projectName = getProperty("projectName");

		// Initialize ApperyClient
		apperyClient = new ApperyClient();
		apperyClient.doLogin(username, password, "/app/");

		try {
			// Get guid of the project
			List<ProjectItem> projectList = mapper.readValue(apperyClient.loadProjectList(),
				new TypeReference<List<ProjectItem>>() {
				});
			projectItem = projectList.stream().filter(it -> projectName.equals(it.getName())).findAny().orElse(null);
			if (projectItem == null) {
				throw new ApperyException("Project not found in Appery: " + projectName);
			}

			// Get info about project assets
			projectInfo = mapper.readValue(apperyClient.loadProjectInfo(projectItem.getGuid()), ProjectInfo.class);

			// Filter out Service Settings
			serviceSettings = projectInfo.getAssets().get(SERVICE_SETTINGS);
			if (serviceSettings == null) {
				throw new ApperyException("Setting items not found in project " + q(projectName));
			}

			// Find `targetSettings` asset
			targetInfo = findSettingsInfo(null);
			targetData = AssetData.parse(apperyClient.loadProjectAssets(projectItem.getGuid(), targetInfo.getAssetId()));
            targetKeys = targetData.getPropertyNames();

		} catch (IOException e) {
			throw new ApperyException(e.getMessage());
		}
	}

	/**
	 * Replace properties in `Setting` item with values from `Setting_env`, making jQM project to use `env` environment.
	 * @param env  Environment to use
	 */
	void switchSettings(String env) throws ApperyException {
		AssetInfo sourceInfo = findSettingsInfo(env);
		env = sourceInfo.getName().substring(baseSettingsName.length() + 1);
		System.out.println(".......... Switching to environment " + e(env) + " in " + q(projectName) + " project");
		AssetData sourceData = AssetData.parse(apperyClient.loadProjectAssets(projectItem.getGuid(), sourceInfo.getAssetId()));
		List<String> sourceKeys = sourceData.getPropertyNames();

		// Check property sets to be identical
		verifyExtraProperties(targetKeys, sourceKeys, null);
		verifyExtraProperties(sourceKeys, targetKeys, env);

		Map sourceProps = sourceData.getProperties();
		targetData.setProperties(sourceProps);
		String result = apperyClient.updateProjectAssets(projectItem.getGuid(), targetData.toJson());
		System.out.println("========== " + e(null) + " updated from " + e(env) + " for " + q(projectName) + " project: " + result);
	}

	/**
	 * Comparing two backend environments.
	 * To ensure that all changes are copied properly between two backend environments,
	 * you can compare them. It will perform the checks to ensure that:
	 * - both databases have the same set of collections
	 * - every collection has the same set of fields
	 * - server-code sources are identical
	 */
	void compareEnvironments(String env_1, String env_2) {
		AssetInfo info_1 = findSettingsInfo(env_1);
		env_1 = info_1.getName().substring(baseSettingsName.length() + 1);
		AssetInfo info_2 = findSettingsInfo(env_2);
		env_2 = info_2.getName().substring(baseSettingsName.length() + 1);
		System.out.println(".......... Comparing environments " + e(env_1) + " and " + e(env_2) + " in " + q(projectName) + " project");

		AssetData data_1 = AssetData.parse(apperyClient.loadProjectAssets(projectItem.getGuid(), info_1.getAssetId()));
		AssetData data_2 = AssetData.parse(apperyClient.loadProjectAssets(projectItem.getGuid(), info_2.getAssetId()));
		List<String> keys_1 = data_1.getPropertyNames();
		List<String> keys_2 = data_2.getPropertyNames();

		// Check property sets to be identical
		verifyExtraProperties(keys_1, keys_2, env_1);
		verifyExtraProperties(keys_2, keys_1, env_2);

		Map props_1 = data_1.getProperties();
		Map props_2 = data_2.getProperties();

		apperyClient.doLogin(username, password, "/bksrv/");
		boolean verifyDatabase = getPropertyFlag("verifyDatabase");
		boolean verifyServerCode = getPropertyFlag("verifyServerCode");

		try {
			if (verifyDatabase) {
				String dbid_1 = data_1.getProperty(database_id);
				if (dbid_1 == null) {
					throw new ApperyException("Property " + q(database_id) + " not found in " + e(env_1) + " of " + q(projectName) + " project");
				}
				String dbid_2 = data_2.getProperty(database_id);
				if (dbid_2 == null) {
					throw new ApperyException("Property " + q(database_id) + " not found in " + e(env_2) + " of " + q(projectName) + " project");
				}
				if (dbid_1.equals(dbid_2)) {
					throw new ApperyException("Items " + e(env_1) + " and " + e(env_2) + " have identical " + q(database_id) + " in " + q(projectName) + " project");
				}

				// Compare datbase structures
				List<DatabaseItem> databaseList = mapper.readValue(apperyClient.loadDatabaseList(), new TypeReference<List<DatabaseItem>>() {
					});

				DatabaseItem db_1 = databaseList.stream().filter(it -> dbid_1.equals(it.get_id())).findAny().orElse(null);
				if (db_1 == null) {
					throw new ApperyException("Database id from " + e(env_1) + " not found in Appery: " + dbid_1);
				}
				DatabaseItem db_2 = databaseList.stream().filter(it -> dbid_2.equals(it.get_id())).findAny().orElse(null);
				if (db_2 == null) {
					throw new ApperyException("Database id from " + e(env_2) + " not found in Appery: " + dbid_2);
				}

				List<DatabaseInfo> collectionList_1 = mapper.readValue(apperyClient.loadCollectionList(dbid_1), new TypeReference<List<DatabaseInfo>>() {
					});
				List<DatabaseInfo> collectionList_2 = mapper.readValue(apperyClient.loadCollectionList(dbid_2), new TypeReference<List<DatabaseInfo>>() {
					});
				List<String> collectionNames_1 = collectionList_1.stream().filter(it -> !it.isSystem()).map(DatabaseInfo::getName).collect(Collectors.toList());
				List<String> collectionNames_2 = collectionList_2.stream().filter(it -> !it.isSystem()).map(DatabaseInfo::getName).collect(Collectors.toList());
				verifyExtraCollections(collectionNames_1, collectionNames_2, db_1.getName());
				verifyExtraCollections(collectionNames_2, collectionNames_1, db_2.getName());

				// Verify extra fields
				for (String collectionName: collectionNames_1) {
					DatabaseInfo dbInfo_1 = collectionList_1.stream().filter(it -> collectionName.equals(it.getName())).findAny().orElse(null);
					DatabaseInfo dbInfo_2 = collectionList_2.stream().filter(it -> collectionName.equals(it.getName())).findAny().orElse(null);
					List<String> fieldNames_1 = dbInfo_1.getFields().stream().map(FieldInfo::getName).collect(Collectors.toList());
					List<String> fieldNames_2 = dbInfo_2.getFields().stream().map(FieldInfo::getName).collect(Collectors.toList());
					verifyExtraFields(fieldNames_1, fieldNames_2, collectionName);
				}
			}

			if (verifyServerCode) {
				// verifyExtraServerCodes
				List<AssetInfo> serverCodes = projectInfo.getAssets().get(SC_SERVICE);
				List<AssetInfo> restServices = projectInfo.getAssets().get(REST_SERVICE);
				serverCodes.addAll(restServices);
				List<String> serverCodeNames = serverCodes.stream().map(AssetInfo::getName).collect(Collectors.toList());
				List<String> serverCodeList = data_1.getPropertyNames();
				serverCodeList.retainAll(serverCodeNames);
				for (String serverCodeName: serverCodeList) {
					String guid_1 = data_1.getProperty(serverCodeName);
					String guid_2 = data_2.getProperty(serverCodeName);
					verifyEqualServerCodes(guid_1, guid_2, serverCodeName, env_1, env_2);
				}
			}

			System.out.println("========== Environments " + e(env_1) + " and " + e(env_2) + " look identical");
		} catch (IOException e) {
			throw new ApperyException(e.getMessage());
		}
	}

	String getScriptSource(String guid, String serverCodeName, String env) throws IOException {
		try {
			ScriptSourceInfo scriptSourceInfo = mapper.readValue(apperyClient.downloadScript(guid), ScriptSourceInfo.class);
			return scriptSourceInfo.getSourceCode().trim();
		} catch (ApperyException e) {
			throw new ApperyException("Server-code GUID " + q(guid) +" not found in Appery for " + q(serverCodeName) + " in " + e(env));
		}
	}

	void verifyEqualServerCodes(String guid_1, String guid_2, String serverCodeName, String env_1, String env_2) throws IOException {
		if (!guid_1.equals(guid_2)) {
			String source_1 = getScriptSource(guid_1, serverCodeName, env_1);
			String source_2 = getScriptSource(guid_2, serverCodeName, env_2);
			if (source_1.equals(source_2)) {
				System.out.println("[VERIFIED] Server-code " + q(serverCodeName) + " is the same");
			} else {
				/*
				System.out.println("--------- source_1");
				System.out.println(source_1);
				System.out.println("--------- source_2");
				System.out.println(source_2);
				*/
				throw new ApperyException("Differences found in " + q(serverCodeName) + " server-code");
			}
		} else {
			System.out.println("[VERIFIED] Server-code " + q(serverCodeName) + " has the same GUID " + q(guid_1));
		}
	}

	void verifyExtraFields(List<String> keys_1, List<String> keys_2, String name_1) {
		List<String> diff = new LinkedList<>(keys_1);
		diff.removeAll(keys_2);
		if (diff.size() != 0) {
			throw new ApperyException("Extra fields in " + q(name_1) + " collection: " + diff);
		} else {
			System.out.println("[VERIFIED] No extra fields in " + q(name_1) + " collection");
		}
	}

	void verifyExtraCollections(List<String> keys_1, List<String> keys_2, String name_1) {
		List<String> diff = new LinkedList<>(keys_1);
		diff.removeAll(keys_2);
		if (diff.size() != 0) {
			throw new ApperyException("Extra collections in " + q(name_1) + " database: " + diff);
		} else {
			System.out.println("[VERIFIED] No extra collections in " + q(name_1));
		}
	}

	void verifyExtraProperties(List<String> keys_1, List<String> keys_2, String name_1) {
		List<String> diff = new LinkedList<>(keys_1);
		diff.removeAll(keys_2);
		if (diff.size() != 0) {
			throw new ApperyException("Extra properties in " + e(name_1) + ": " + diff);
		} else {
			System.out.println("[VERIFIED] No extra properties in " + e(name_1));
		}
	}

	// -------------- Utils

	String q(String name) {
		return "`" + name + "`";
	}

	String e(String env) {
		return q(baseSettingsName + (env == null? "": "_" + env));
	}

	AssetInfo findSettingsInfo(String env) throws ApperyException {
		String settingsName = baseSettingsName + (env == null? "": "_" + env);
		AssetInfo settings = serviceSettings.stream().filter(it -> settingsName.toLowerCase().equals(it.getName().toLowerCase())).findAny().orElse(null);
		if (settings == null) {
			throw new ApperyException(q(settingsName) + " item not found in project " + q(projectName));
		}
        return settings;
	}

	Properties loadProperties(String propName) throws IOException {
		Properties pp = new Properties();
		FileInputStream fin = new FileInputStream(propName);
		pp.load(fin);
		fin.close();
		return pp;
	}

	String getProperty(String key) throws ApperyException {
		String value = pp.getProperty(key);
		if (value == null) {
			throw new ApperyException("Missing required parameter in properties file: " + key);
		}
		return value;
	}

	boolean getPropertyFlag(String key) {
		String value = pp.getProperty(key);
		if (value == null) {
			return false;
		}
		return value.trim().toLowerCase().equals("on");
	}

}