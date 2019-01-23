package io.appery.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Access to Appery.io REST API services.
 */
public class ApperyClient extends ApperyRestClient {

    ObjectMapper mapper = new ObjectMapper();

    // -------------- Login

    /**
     * Performs SAML login to access Appery.io backend functions.
     */
    public boolean doLogin(String username, String password) throws ApperyException {
        return doLogin(username, password, "/bksrv/");
    }

    /**
     * Performs SAML login into Appery.io site.
     */
    public boolean doLogin(String username, String password, String targetPath) throws ApperyException {
        new ApperySecurity(this).doLogin(username, password, targetPath);
        return true;
    }

    // -------------- Appery projects

    /**
     * Get list of existing projects in Appery.io workspace.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadProjectList() throws ApperyException {
        return traceGet("List of projects", "/app/rest/projects");
    }

    /**
     * Get information about project in Appery.io workspace.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadProjectInfo(String guid) throws ApperyException {
        HashMap<String, String> data = new HashMap<>();
        data.put("guid", guid);
        return traceGet("Project information", "/app/rest/html5/project", data);
    }

    /**
     * Get available templates to create projects in Appery.io workspace.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadProjectTemplates() throws ApperyException {
        return traceGet("Project templates", "/app/rest/html5/plugin/wizardProject");
    }

    /**
     * Create project in Appery.io workspace.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String createApperyProject(String projectName, int projectType) throws ApperyException {
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", projectName);
        data.put("templateId", projectType);
        try {
            String str = mapper.writeValueAsString(data);
            return tracePost("Project creation result", "/app/rest/projects", str);
        } catch (JsonProcessingException e) {
            throw new ApperyException(e.getMessage());
		}
    }

    /**
     * Load list of assets for Appery.io project.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadProjectAssets(String projectGuid, String... assets) throws ApperyException {

        // convert each asset id into HashMap
        List<Map<String, Object>> items = new LinkedList<>();
        for (String aid: assets) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("id", aid);
            items.add(item);
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("assets", items);
        try {
            String str = mapper.writeValueAsString(data);
            return tracePost("List of assets", "/app/rest/html5/project/" + projectGuid + "/asset/data", str);
        } catch (JsonProcessingException e) {
            throw new ApperyException(e.getMessage());
		}
    }

    /**
     * Update list of assets in Appery.io project.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String updateProjectAssets(String projectGuid, String assetsData) throws ApperyException {
        return tracePut("List of assets update result", "/app/rest/html5/project/" + projectGuid + "/asset/data", assetsData);
    }

    /**
     * Load ids of project source files.
     * @param projectGuidType  Project GUID concatenated with project type.
     *                         Example: projectGuid + '/IONIC/'
     */
    public String loadSourceInfo(String projectGuidType) throws ApperyException {
        try {
            return traceGet("Source info", "/app/rest/html5/ide/source/read/" + projectGuidType);
        } catch (ApperyException e) {
            return null;
        }
    }

    /**
     * Load source file by id.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadSource(String srcId) throws ApperyException {
        return makeGet("/app/rest/html5/ide/source/" + srcId + "/read/data");
    }

    /**
     * Get list of certificates in Appery.io workspace.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadCertificateList() throws ApperyException {
        return traceGet("List of certificates", "/app/rest/certificates");
    }

    /**
     * Get information about certificate in Appery.io workspace.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadCertificateInfo(String uuid) throws ApperyException {
        return traceGet("List of certificates", "/app/rest/certificates/" + uuid);
    }

    // -------------- Server code scripts

    /**
     * Returns list of server code scripts and libraries in Appery.io workspace.
     * Metainformation about scripts included.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadServerCodesList() throws ApperyException {
        return traceGet("List of server code scripts and libraries", "/bksrv/rest/1/code/admin/script/?light=true");
    }

    /**
     * Returns list of server code folders in Appery.io workspace. Metainformation
     * included.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadServerCodesFolders() throws ApperyException {
        return traceGet("List of server code folders", "/bksrv/rest/1/code/admin/folders/");
    }

    /**
     * Download server code script.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String downloadScript(String scriptGuid) throws ApperyException {
        return traceGet("Server code script", "/bksrv/rest/1/code/admin/script/" + scriptGuid);
    }

    // -------------- Database

    /**
     * Get list of existing databases in Appery.io workspace.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadDatabaseList() throws ApperyException {
        return traceGet("List of databases", "/bksrv/rest/1/admin/databases");
    }

    /**
     * Get list of collections in Appery.io database.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadCollectionList(String dbid) throws ApperyException {
        HashMap<String, String> data = new HashMap<>();
        data.put("X-Appery-Database-Id", dbid);
        return traceGet("List of collections", "/bksrv/rest/1/admin/collections", null, data);
    }

    // -------------- API Express

    /**
     * Get list of AEX projects in Appery.io workspace.
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadAexProjectList() throws ApperyException {
        return traceGet("List of AEX projects", "/apiexpress/rest/projects");
    }

    /**
     * Returns list of AEX folders in Appery.io workspace
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadAexFolders(String projectRootId) throws ApperyException {
        return traceGet("List of AEX folders", "/apiexpress/rest/folders/" + projectRootId + "/children");
    }

    /**
     * Returns list of AEX services in project folder
     *
     * @throws IOException
     * @throws ApperyException
     */
    public String loadAexServices(String folderId) throws ApperyException {
        return traceGet("List of AEX services", "/apiexpress/rest/service/custom/" + folderId + "/children");
    }

    // -------------- Utils

    String traceGet(String title, String url) throws ApperyException {
        return traceMeta(title, url, null, null, null, "get");
    }

    String traceGet(String title, String url, Map<String, String> params) throws ApperyException {
        return traceMeta(title, url, params, null, null, "get");
    }

    String traceGet(String title, String url, Map<String, String> params, Map<String, String> headers)
            throws ApperyException {
        return traceMeta(title, url, params, headers, null, "get");
    }

    String tracePost(String title, String url, String body) throws ApperyException {
        return traceMeta(title, url, null, null, body, "post");
    }

    String tracePut(String title, String url, String body) throws ApperyException {
        return traceMeta(title, url, null, null, body, "put");
    }

    String traceMeta(String title, String url, Map<String, String> params, Map<String, String> headers, String body,
            String method) throws ApperyException {
        String result = null;
        switch (method) {
        case "get":
            result = makeGet(url, params, headers);
            break;
        case "post":
            result = makePost(url, body);
            break;
        case "put":
            result = makePut(url, body);
            break;
        }
        return result;
    }

    void delay(int ms) throws InterruptedException {
        Thread.sleep(ms);
    }

}
