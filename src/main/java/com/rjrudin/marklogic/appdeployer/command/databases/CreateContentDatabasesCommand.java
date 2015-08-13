package com.rjrudin.marklogic.appdeployer.command.databases;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.rjrudin.marklogic.appdeployer.AppConfig;
import com.rjrudin.marklogic.appdeployer.command.AbstractCommand;
import com.rjrudin.marklogic.appdeployer.command.CommandContext;
import com.rjrudin.marklogic.appdeployer.command.SortOrderConstants;
import com.rjrudin.marklogic.appdeployer.command.UndoableCommand;
import com.rjrudin.marklogic.mgmt.databases.DatabaseManager;
import com.rjrudin.marklogic.rest.util.JsonNodeUtil;

public class CreateContentDatabasesCommand extends AbstractCommand implements UndoableCommand {

    private String forestDelete = "data";

    public CreateContentDatabasesCommand() {
        setExecuteSortOrder(SortOrderConstants.CREATE_CONTENT_DATABASES);
    }

    /**
     * Must be deleted before triggers/schemas databases are deleted, as the content database may refer to a
     * triggers/schemas database that must be deleted.
     */
    @Override
    public Integer getUndoSortOrder() {
        return SortOrderConstants.DELETE_CONTENT_DATABASES;
    }

    @Override
    public void execute(CommandContext context) {
        AppConfig appConfig = context.getAppConfig();

        JsonNode node = mergeContentDatabaseFiles(appConfig);
        if (node == null) {
            logger.info("No content database files found, so no creating or updating content databases");
            return;
        }

        String payload = node.toString();
        String json = tokenReplacer.replaceTokens(payload, appConfig, false);

        DatabaseManager dbMgr = new DatabaseManager(context.getManageClient());
        dbMgr.save(json);

        if (appConfig.isTestPortSet()) {
            json = tokenReplacer.replaceTokens(payload, appConfig, true);
            dbMgr.save(json);
        }
    }

    /**
     * Just because there's not a content database file doesn't mean that one wasn't created via the command for
     * creating a REST API server. If the REST API server command didn't delete the content database, we'd still want
     * this command to attempt to do so in the event that no content database files exist.
     */
    @Override
    public void undo(CommandContext context) {
        AppConfig appConfig = context.getAppConfig();
        JsonNode node = mergeContentDatabaseFiles(appConfig);
        if (node != null) {
            logger.info("No content database files found, so not deleting content databases");
            String payload = node.toString();
            String json = tokenReplacer.replaceTokens(payload, appConfig, false);

            DatabaseManager dbMgr = new DatabaseManager(context.getManageClient());
            dbMgr.setForestDelete(forestDelete);
            dbMgr.delete(json);

            if (appConfig.isTestPortSet()) {
                json = tokenReplacer.replaceTokens(payload, appConfig, true);
                dbMgr.delete(json);
            }
        } else {
            // Try to delete the content database if it exists
            DatabaseManager dbMgr = new DatabaseManager(context.getManageClient());
            dbMgr.setForestDelete(forestDelete);
            dbMgr.deleteByName(appConfig.getContentDatabaseName());

            if (appConfig.isTestPortSet()) {
                dbMgr.deleteByName(appConfig.getContentDatabaseName());
            }
        }
    }

    protected JsonNode mergeContentDatabaseFiles(AppConfig appConfig) {
        List<File> files = appConfig.getConfigDir().getContentDatabaseFiles();
        if (logger.isInfoEnabled()) {
            logger.info("Merging JSON files at locations: " + files);
        }
        return JsonNodeUtil.mergeJsonFiles(files);
    }

    public String getForestDelete() {
        return forestDelete;
    }

    public void setForestDelete(String forestDelete) {
        this.forestDelete = forestDelete;
    }
}
