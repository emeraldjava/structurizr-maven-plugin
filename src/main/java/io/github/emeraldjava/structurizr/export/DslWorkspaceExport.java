package io.github.emeraldjava.structurizr.export;

import com.structurizr.export.WorkspaceExport;

public class DslWorkspaceExport extends WorkspaceExport {

    public DslWorkspaceExport(String definition) {
        super(definition);
    }

    @Override
    public String getFileExtension() {
        return "dsl";
    }
}