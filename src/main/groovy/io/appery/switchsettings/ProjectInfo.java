package io.appery.switchsettings;

import java.util.List;
import java.util.Map;

public class ProjectInfo {

    private String name;
    private String description;
	private Map<String, List<AssetInfo>> assets;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the assets
     */
    public Map<String, List<AssetInfo>> getAssets() {
        return assets;
    }

    /**
     * @param assets the assets to set
     */
    public void setAssets(Map<String, List<AssetInfo>> assets) {
        this.assets = assets;
    }

}