/* File:    $Id$
 * Version: $Revision$
 * Date:    $Date$
 * Author:  $Author$
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
package dk.netarkivet.archive.checksum;

import dk.netarkivet.archive.checksum.distribute.FileChecksumServerTester;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Suite of unittests for the classes in the
 * dk.netarkivet.archive.checksum and dk.netarkivet.archive.checksum.distribute
 * package.
 */
public class ArchiveChecksumTesterSuite {
    public static Test suite() {
        TestSuite suite;
        suite = new TestSuite(ArchiveChecksumTesterSuite.class.getName());

        addToSuite(suite);

        return suite;
    }

    public static void addToSuite(TestSuite suite) {
        suite.addTestSuite(FileChecksumArchiveTester.class);
        suite.addTestSuite(FileChecksumServerTester.class);
    }

    public static void main(String args[]) {
        String args2[] = { "-noloading",
                ArchiveChecksumTesterSuite.class.getName() };
        TestRunner.main(args2);
    }
}