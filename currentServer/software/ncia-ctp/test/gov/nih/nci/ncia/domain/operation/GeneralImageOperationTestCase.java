package gov.nih.nci.ncia.domain.operation;

import gov.nih.nci.ncia.AbstractDbUnitTestForJunit4;
import gov.nih.nci.ncia.internaldomain.GeneralSeries;
import gov.nih.nci.ncia.internaldomain.Patient;
import gov.nih.nci.ncia.internaldomain.Study;
import gov.nih.nci.ncia.internaldomain.TrialDataProvenance;

import java.util.Hashtable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import junit.framework.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class })

public class GeneralImageOperationTestCase extends AbstractDbUnitTestForJunit4 {

	@Test
	public void testValidateNoSopInstanceUid() throws Exception {
		try {
			Hashtable<String, String> numbers = new Hashtable<String, String> ();
			setUp();
			gio.validate(numbers);
			Assert.fail("without a sop uid, the image shouldnt validate");
		}
		catch(Exception ex) {
			//want to be here
			Assert.assertTrue(true);
		}
	}
    //////////////////////////////PROTECTED/////////////////////////////////

    protected String getDataSetResourceSpec() {
    	return TEST_DB_FLAT_FILE;
    }

    protected void setUp() throws Exception {

		gio.setDataProvenance(tdp);
		gio.setPatient(patient);
		gio.setSeries(series);
		gio.setStudy(study);
    }

    ////////////////////////////////////PRIVATE/////////////////////////////////

    private static final String TEST_DB_FLAT_FILE = "dbunitscripts/patient_353337344.xml";

    @Autowired
    private GeneralImageOperationInterface gio;
    @Autowired
    TrialDataProvenance tdp;
    @Autowired
    Patient patient;
    @Autowired
    Study study;
    @Autowired
    GeneralSeries series;
}
