/*
 * Hibernate Tools, Tooling for your Hibernate Projects
 * 
 * Copyright 2004-2020 Red Hat, Inc.
 *
 * Licensed under the GNU Lesser General Public License (LGPL), 
 * version 2.1 or later (the "License").
 * You may not use this file except in compliance with the License.
 * You may read the licence in the 'lgpl.txt' file in the root folder of 
 * project or obtain a copy at
 *
 *     http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hibernate.tool.hbm2x.hbm2hbmxml.OneToOneTest;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.api.export.ExporterConstants;
import org.hibernate.tool.api.metadata.MetadataDescriptor;
import org.hibernate.tool.api.metadata.MetadataDescriptorFactory;
import org.hibernate.tool.internal.export.hbm.HbmExporter;
import org.hibernate.tools.test.util.HibernateUtil;
import org.hibernate.tools.test.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;



public class TestCase {

	private static final String[] HBM_XML_FILES = new String[] {
			"PersonAddressOneToOnePrimaryKey.hbm.xml"
	};
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	private File outputDir = null;
	private File resourcesDir = null;
	private HbmExporter hbmexporter = null;
	
	@Before
	public void setUp() throws Exception {
		outputDir = new File(temporaryFolder.getRoot(), "output");
		outputDir.mkdir();
		resourcesDir = new File(temporaryFolder.getRoot(), "resources");
		resourcesDir.mkdir();
		MetadataDescriptor metadataDescriptor = HibernateUtil
				.initializeMetadataDescriptor(this, HBM_XML_FILES, resourcesDir);
		hbmexporter = new HbmExporter();
		hbmexporter.getProperties().put(ExporterConstants.METADATA_DESCRIPTOR, metadataDescriptor);
		hbmexporter.getProperties().put(ExporterConstants.DESTINATION_FOLDER, outputDir);
		hbmexporter.start();		
	}
	
	@Test
	public void testAllFilesExistence() {
		Assert.assertFalse(new File(
				outputDir, "GeneralHbmSettings.hbm.xml")
			.exists());
		JUnitUtil.assertIsNonEmptyFile(
				new File(
						outputDir,
						"org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Person.hbm.xml"));
		JUnitUtil.assertIsNonEmptyFile(
				new File(
						outputDir, 
						"/org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Address.hbm.xml"));		
	}
	
	@Test
	public void testArtifactCollection() {
		Assert.assertEquals(
				2,
				hbmexporter.getArtifactCollector().getFileCount("hbm.xml"));
	}
	
	@Test
	public void testReadable() {
        File personHbmXml = new File(
        		outputDir, 
        		"org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Person.hbm.xml");
        File addressHbmXml = new File(
        		outputDir, 
        		"org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Address.hbm.xml");
		Properties properties = new Properties();
		properties.setProperty(AvailableSettings.DIALECT, HibernateUtil.Dialect.class.getName());
		File[] files = new File[] { personHbmXml, addressHbmXml };
		MetadataDescriptor metadataDescriptor = MetadataDescriptorFactory
				.createNativeDescriptor(null, files, properties);
        Assert.assertNotNull(metadataDescriptor.createMetadata());
    }
	
	public void testOneToOne() throws DocumentException {
		SAXReader xmlReader = new SAXReader();
		xmlReader.setValidation(true);
		File xmlFile = new File(
        		outputDir, 
        		"org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Person.hbm.xml");
		Document document = xmlReader.read(xmlFile);
		XPath xpath = DocumentHelper.createXPath("//hibernate-mapping/class/one-to-one");
		List<?> list = xpath.selectNodes(document);
		Assert.assertEquals("Expected to get one-to-one element", 1, list.size());
		Element node = (Element) list.get(0);
		Assert.assertEquals(node.attribute( "name" ).getText(),"address");
		Assert.assertEquals(node.attribute( "constrained" ).getText(),"false");
		xmlFile = new File(
        		outputDir, 
        		"org/hibernate/tool/hbm2x/hbm2hbmxml/OneToOneTest/Address.hbm.xml");
		document = xmlReader.read(xmlFile);
		xpath = DocumentHelper.createXPath("//hibernate-mapping/class/one-to-one");
		list = xpath.selectNodes(document);
		Assert.assertEquals("Expected to get one set element", 1, list.size());
		node = (Element) list.get(0);
		Assert.assertEquals(node.attribute( "name" ).getText(),"person");
		Assert.assertEquals(node.attribute( "constrained" ).getText(),"true");
		Assert.assertEquals(node.attribute( "access" ).getText(), "field");
	}

}
