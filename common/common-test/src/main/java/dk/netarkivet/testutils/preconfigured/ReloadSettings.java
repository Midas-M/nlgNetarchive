/*
 * #%L
 * Netarchivesuite - common - test
 * %%
 * Copyright (C) 2005 - 2014 The Royal Danish Library, the Danish State and University Library,
 *             the National Library of France and the Austrian National Library.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package dk.netarkivet.testutils.preconfigured;

import java.io.File;

import dk.netarkivet.common.utils.Settings;

public class ReloadSettings implements TestConfigurationIF {
    private File f;
    private String oldSettingsFilenames;
    private String settingsFileProperty = Settings.SETTINGS_FILE_PROPERTY;

    public ReloadSettings() {

    }

    public ReloadSettings(File f) {
        this.f = f;
    }

    public void setUp() {
        oldSettingsFilenames = System.getProperty(settingsFileProperty);
        if (f != null) {
            System.setProperty(settingsFileProperty, f.getAbsolutePath());
        }
        Settings.reload();
    }

    public void tearDown() {
        if (oldSettingsFilenames != null) {
            System.setProperty(settingsFileProperty, oldSettingsFilenames);
        } else {
            System.clearProperty(settingsFileProperty);
        }
        Settings.reload();
    }

}