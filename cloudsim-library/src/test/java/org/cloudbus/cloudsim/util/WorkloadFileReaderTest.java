package org.cloudbus.cloudsim.util;

import org.cloudbus.cloudsim.Cloudlet;
import org.junit.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WorkloadFileReaderTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void read() throws FileNotFoundException {

	WorkloadModel r = new WorkloadFileReader("src"
		+ File.separator
		+ "test"
		+ File.separator
		+ "LCG.swf.gz", 1);
	List<Cloudlet> cloudletlist = r.generateWorkload();
	assertEquals(188041, cloudletlist.size());

	for (Cloudlet cloudlet : cloudletlist) {
	    assertTrue(cloudlet.getCloudletLength() > 0);
	}
    }
}
