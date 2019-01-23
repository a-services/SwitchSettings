package io.appery.switchsettings;

import io.appery.rest.ApperyException;

/**
 * Main class to run the utility.
 */
public class aset {

	public static void main(String[] args) {
        System.out.println("--------------------------------------------------------------------------------");
        if (args.length == 0) {
            System.out.println("Parameters: env [env2]");
            System.out.println("- Single parameter mode will switch Appery.io jQM project `Settings` item to `env`");
            System.out.println("- Two parameters mode will compare `env` and `env2` environments");
            return;
        }
        try {
            ApperySettingsSwitcher apperySettingsSwitcher = new ApperySettingsSwitcher();
            if (args.length == 1) {
                apperySettingsSwitcher.switchSettings(args[0]);
            } else {
                apperySettingsSwitcher.compareEnvironments(args[0], args[1]);
            }
        } catch(ApperyException e) {
            System.out.println("[ERROR] " + e.getReason());
        }
	}
}