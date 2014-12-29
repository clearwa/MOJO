package com.objectforge.mascot.ACPFormEditor.internal;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class ACPFormEditorPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ACPFormEditorPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
    private static String [] BUNDLPLACES = new String [] 
               {  "plugin" , "com.objectforge.mascot.ACPFormEditor.internal.ACPFormEditorPluginResources"};
	
	/**
	 * The constructor.
	 */
	public ACPFormEditorPlugin() {
        super();
        plugin = this;
    }

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static ACPFormEditorPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ACPFormEditorPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
        resourceBundle = null;
        for (int i=0; i < BUNDLPLACES.length; i++) {
            try {
                resourceBundle = ResourceBundle.getBundle( BUNDLPLACES[i] );
                getLog().log( new Status( IStatus.INFO, Platform.PI_RUNTIME, 0,"Found " + BUNDLPLACES[i], null ));
                return resourceBundle;
            } catch (MissingResourceException x) {
                getLog().log( new Status( IStatus.ERROR, Platform.PI_RUNTIME, 1,"Cannot find resource bundle", null ));
            }
        }
		return null;
	}
}
