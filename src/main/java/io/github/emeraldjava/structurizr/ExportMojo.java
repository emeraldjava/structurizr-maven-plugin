package io.github.emeraldjava.structurizr;

import com.structurizr.Workspace;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "export", requiresProject = true)
public class ExportMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}/generated", required = true, readonly = true)
    File outputFileBaseDir;

    @Parameter(defaultValue = "${project.build.resources[0].directory}/workspace", required = true, readonly = true)
    File productConfigRootFolder;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("productConfigRootFolder::" + productConfigRootFolder.getAbsolutePath());
        Workspace workspace;
    }
}
