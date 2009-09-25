/* $Id: MetadataFile.java 1012 2009-09-21 17:26:49Z ngiraud $
 * $Revision: 1012 $
 * $Date: 2009-09-16 14:39:49 +0200 (Wed, 16 Sep 2009) $
 * $Author: ngiraud $
 *
 * The Netarchive Suite - Software to harvest and preserve websites
 * Copyright 2004-2007 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dk.netarkivet.harvester.harvesting;

import java.io.File;

import dk.netarkivet.common.utils.Settings;
import dk.netarkivet.harvester.HarvesterSettings;

/**
 * Wraps information for an Heritrix file that should be stored in the metadata
 * ARC.
 *
 * Defines a natural order to sort them.
 *
 * @author ngiraud
 */
class MetadataFile implements Comparable<MetadataFile> {

    /**
     * The available type of metadata records.
     *
     * @author ngiraud
     */
    private enum MetadataType {
        setup,
        reports,
        logs
    }

    /**
     * A string format that is used to build metadata URLs. Parameters are, in
     * order : <ol> <li>the file type @see {@link MetadataType}</li> <li>the
     * file name</li> <li>the Heritrix version</li> <li>the harvest id</li>
     * <li>the job id</li> </ol>
     */
    private static final String URL_FORMAT =
            "metadata://netarkivet.dk/crawl/%s/%s"
            + "?heritrixVersion=%s&harvestid=%s&jobid=%s";

    /**
     * The pattern controlling which files in the crawl directory root should be
     * stored in the metadata ARC.
     */
    public static final String HERITRIX_FILE_PATTERN =
            Settings.get(HarvesterSettings.METADATA_HERITRIX_FILE_PATTERN);

    /**
     * The pattern controlling which files in the crawl directory root should be
     * stored in the metadata ARC as reports.
     */
    public static final String REPORT_FILE_PATTERN =
            Settings.get(HarvesterSettings.METADATA_REPORT_FILE_PATTERN);

    /**
     * The pattern controlling which files in the logs subdirectory of the crawl
     * directory root should be stored in the metadata ARC as log files.
     */
    public static final String LOG_FILE_PATTERN =
            Settings.get(HarvesterSettings.METADATA_LOG_FILE_PATTERN);

    private String url;
    private File heritrixFile;
    private MetadataType type;

    MetadataFile(
            File heritrixFile,
            Long harvestId,
            Long jobId,
            String heritrixVersion) {

        this.heritrixFile = heritrixFile;

        this.type = MetadataType.setup;
        String name = heritrixFile.getName();
        if (name.matches(REPORT_FILE_PATTERN)) {
            this.type = MetadataType.reports;
            this.url = makeMetadataURL(
                    MetadataType.reports,
                    heritrixFile.getName(),
                    harvestId, jobId, heritrixVersion);
        } else if (name.matches(LOG_FILE_PATTERN)) {
            this.type = MetadataType.logs;
            this.url = makeMetadataURL(
                    MetadataType.logs,
                    heritrixFile.getName(),
                    harvestId, jobId, heritrixVersion);
        } else {
            this.url = makeMetadataURL(
                    MetadataType.setup,
                    heritrixFile.getName(),
                    harvestId, jobId, heritrixVersion);
        }
    }

    MetadataFile(
            File heritrixFile,
            Long harvestId,
            Long jobId,
            String heritrixVersion,
            String domain) {
        this(heritrixFile, harvestId, jobId, heritrixVersion);
        this.url += "&domain=" + domain;
    }

    public String getUrl() {
        return url;
    }

    public File getHeritrixFile() {
        return heritrixFile;
    }

    /** First we compare the type ordinals, then the URLS. */
    public int compareTo(MetadataFile other) {
        Integer thisOrdinal = this.type.ordinal();
        Integer otherOrdinal = other.type.ordinal();

        int ordinalCompare = thisOrdinal.compareTo(otherOrdinal);
        if (ordinalCompare != 0) {
            return ordinalCompare;
        }
        return this.url.compareTo(other.url);
    }

    private String makeMetadataURL(
            MetadataType type,
            String name,
            long harvestID,
            long jobID,
            String heritrixVersion) {
        return String.format(
                URL_FORMAT,
                type.name(),
                name,
                heritrixVersion,
                Long.toString(harvestID),
                Long.toString(jobID)
        );
    }

}