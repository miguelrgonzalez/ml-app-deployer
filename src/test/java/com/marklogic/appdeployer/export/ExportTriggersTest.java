package com.marklogic.appdeployer.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.appdeployer.command.databases.DeployOtherDatabasesCommand;
import com.marklogic.appdeployer.command.triggers.DeployTriggersCommand;
import com.marklogic.mgmt.selector.PrefixResourceSelector;
import com.marklogic.mgmt.selector.PropertiesResourceSelector;
import com.marklogic.mgmt.selector.RegexResourceSelector;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

public class ExportTriggersTest extends AbstractExportTest {

	@After
	public void teardown() {
		undeploySampleApp();
	}

	@Test
	public void test() throws Exception {
		initializeAppDeployer(new DeployOtherDatabasesCommand(1), new DeployTriggersCommand());
		appDeployer.deploy(appConfig);

		Properties props = new Properties();
		props.setProperty("triggers", "my-trigger");

		ExportedResources resources = new Exporter(manageClient)
			.withTriggersDatabase(appConfig.getTriggersDatabaseName())
			.select(new RegexResourceSelector("my-trigger"))
			.export(exportDir);
		verifyExportedResources(resources);

		initializeExportDir();
		resources = new Exporter(manageClient)
			.withTriggersDatabase(appConfig.getTriggersDatabaseName())
			.select(new PrefixResourceSelector("my-trig"))
			.export(exportDir);
		verifyExportedResources(resources);

		initializeExportDir();
		resources = new Exporter(manageClient)
			.withTriggersDatabase(appConfig.getTriggersDatabaseName())
			.select(new PropertiesResourceSelector(props))
			.export(exportDir);
		verifyExportedResources(resources);
	}

	private void verifyExportedResources(ExportedResources resources) throws Exception {
		assertEquals(1, resources.getMessages().size());
		assertEquals("Each exported trigger has the 'id' field removed from it, as that field should be generated by MarkLogic.",
			resources.getMessages().get(0));

		assertEquals(1, resources.getFiles().size());

		File triggerFile = new File(exportDir, "triggers/my-trigger.json");
		JsonNode json = objectMapper.readTree(triggerFile);
		assertNull(json.get("id"));
		assertEquals("my-trigger", json.get("name").asText());
	}
}
