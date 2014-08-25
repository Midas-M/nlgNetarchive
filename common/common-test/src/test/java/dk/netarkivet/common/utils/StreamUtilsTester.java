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
package dk.netarkivet.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mockobjects.servlet.MockJspWriter;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.testutils.TestFileUtils;
import dk.netarkivet.testutils.preconfigured.ReloadSettings;
import dk.netarkivet.testutils.preconfigured.UseTestRemoteFile;

/**
 * 
 * Unit tests for the StreamUtils class.
 *
 */
public class StreamUtilsTester {

    private UseTestRemoteFile rf = new UseTestRemoteFile();
    ReloadSettings rs = new ReloadSettings();

    private static final File BASE_DIR = new File("tests/dk/netarkivet/common/utils");
    private static final File ORIGINALS = new File(BASE_DIR, "fileutils_data");
    private static final File WORKING = new File(BASE_DIR, "working");
    private static final File TESTFILE = new File(WORKING, "streamutilstestfile.txt");

    @Before
    public void setUp() {
        rs.setUp();
        FileUtils.removeRecursively(WORKING);
        TestFileUtils.copyDirectoryNonCVS(ORIGINALS, WORKING);
        rf.setUp();
    }

    @After
    public void tearDown() {
        FileUtils.removeRecursively(WORKING);
        rf.tearDown();
        rs.tearDown();
    }

    @Test
    public void testCopyInputStreamToOutputStream() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("foobar\n".getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copyInputStreamToOutputStream(in, out);
        assertEquals("Input should have been transferred", "foobar\n", out.toString());
        assertEquals("In stream should be at EOF", -1, in.read());

        in = new ByteArrayInputStream("hash".getBytes());
        StreamUtils.copyInputStreamToOutputStream(in, out);
        assertEquals("Extra input should have been transferred", "foobar\nhash", out.toString());

        StreamUtils.copyInputStreamToOutputStream(in, out);
        assertEquals("Re-reading input should give no error and no change", "foobar\nhash", out.toString());

        in = new ByteArrayInputStream("".getBytes());
        StreamUtils.copyInputStreamToOutputStream(in, out);
        assertEquals("Zero input should have been transferred", "foobar\nhash", out.toString());

        try {
            StreamUtils.copyInputStreamToOutputStream(null, out);
            fail("Null in should cause error");
        } catch (ArgumentNotValid e) {
            // expected
        }

        try {
            StreamUtils.copyInputStreamToOutputStream(in, null);
            fail("Null out should cause error");
        } catch (ArgumentNotValid e) {
            // expected
        }

        // TODO: Test with streams that cause errors if closed.
    }

    /** test that method copyInputStreamToJspWriter works. */
    @Test
    public void testCopyInputStreamToJspWriter() throws Exception {
        StringBuffer buf = new StringBuffer();
        MyMockJspWriter writer = new MyMockJspWriter(buf);
        String testfileAsString = FileUtils.readFile(TESTFILE);
        StreamUtils.copyInputStreamToJspWriter(new FileInputStream(TESTFILE), writer);

        assertEquals(testfileAsString, buf.toString());
    }

    @Test
    public void testGetInputStreamAsString() throws IOException {
        String testfileAsString = FileUtils.readFile(TESTFILE);

        assertEquals(testfileAsString, StreamUtils.getInputStreamAsString(new FileInputStream(TESTFILE)));
    }

    private class MyMockJspWriter extends MockJspWriter {
        private StringBuffer buf;

        public MyMockJspWriter(StringBuffer buf) {
            this.buf = buf;
        }

        public void write(String str, int off, int len) {
            buf.append(str.substring(off, len));
        }
    }
}