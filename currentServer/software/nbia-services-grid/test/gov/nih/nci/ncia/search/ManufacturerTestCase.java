package gov.nih.nci.ncia.search;

import junit.framework.TestCase;

public class ManufacturerTestCase extends TestCase {

	public void testManufacturer() {
		Model m1 = new Model();
		Model m2 = new Model();
		Model[] models = new Model[2];
		models[0] = m1;
		models[1] = m2;
		
		Manufacturer manu = new Manufacturer();
		manu.setName("foo1");
		manu.setModels(models);
		
		assertTrue(manu.getName().equals("foo1"));
		assertTrue(manu.getModels().length==2);
		
		Model newModel = new Model();
		newModel.setName("eek");
		manu.setModels(0, newModel);
		assertTrue(manu.getModels(0).getName().equals("eek"));
	}

}
