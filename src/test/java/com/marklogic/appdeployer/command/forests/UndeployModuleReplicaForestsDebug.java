package com.marklogic.appdeployer.command.forests;

import com.marklogic.appdeployer.AppConfig;
import com.marklogic.appdeployer.command.restapis.DeployRestApiServersCommand;
import com.marklogic.appdeployer.impl.SimpleAppDeployer;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.ManageConfig;
import com.marklogic.mgmt.admin.AdminConfig;
import com.marklogic.mgmt.admin.AdminManager;

/**
 * This program is used to verify that when a REST API server is deleted, the replica forests for both the content
 * and modules databases are deleted first if the command is configured to delete the content/modules databases. It
 * relies on replicas being created, so it's currently just a debug program instead of a test, as the test environment
 * is assumed to be a single host.
 */
public class UndeployModuleReplicaForestsDebug {

	public static void main(String[] args) {
		final String host = args[0];
		final String password = args[1];

		ManageConfig config = new ManageConfig(host, 8002, "admin", password);
		ManageClient manageClient = new ManageClient(config);

		AdminConfig adminConfig = new AdminConfig(host, 8001, "admin", password);
		AdminManager adminManager = new AdminManager(adminConfig);

		AppConfig appConfig = new AppConfig();
		appConfig.setName("testapp");
		appConfig.setDatabaseNamesAndReplicaCounts("testapp-modules,1,testapp-content,1");

		DeployRestApiServersCommand restApiCommand = new DeployRestApiServersCommand();
		// Command is assumed to delete the modules database by default
		restApiCommand.setDeleteContentDatabase(true);

		ConfigureForestReplicasCommand replicasCommand = new ConfigureForestReplicasCommand();

		SimpleAppDeployer deployer = new SimpleAppDeployer(manageClient, adminManager, restApiCommand, replicasCommand);

		deployer.deploy(appConfig);

		/**
		 * This should delete all of the replica forests for the content/modules databases first, and then the DELETE
		 * call to /v1/rest-apis will delete the content and modules databases.
		 */
		deployer.undeploy(appConfig);
	}

}
