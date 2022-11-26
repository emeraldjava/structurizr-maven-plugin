package io.github.emeraldjava.structurizr;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.export.*;
import com.structurizr.export.dot.DOTExporter;
import com.structurizr.export.ilograph.IlographExporter;
import com.structurizr.export.mermaid.MermaidDiagramExporter;
import com.structurizr.export.plantuml.C4PlantUMLExporter;
import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;
import com.structurizr.export.websequencediagrams.WebSequenceDiagramsExporter;
import io.github.emeraldjava.structurizr.export.DslWorkspaceExporter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mojo(name = "export", requiresProject = true)
public class ExportMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}/generated", required = true, readonly = true)
    File outputFileBaseDir;

    @Parameter(defaultValue = "${project.build.resources[0].directory}/workspace/example.dsl", required = true, readonly = true)
    File productConfigRootFile;


    private static final String JSON_FORMAT = "json";
    private static final String THEME_FORMAT = "theme";
    private static final String DSL_FORMAT = "dsl";
    private static final String PLANTUML_FORMAT = "plantuml";
    private static final String PLANTUML_C4PLANTUML_SUBFORMAT = "plantuml/c4plantuml";
    private static final String PLANTUML_STRUCTURIZR_SUBFORMAT = "structurizr";
    private static final String WEBSEQUENCEDIAGRAMS_FORMAT = "websequencediagrams";
    private static final String MERMAID_FORMAT = "mermaid";
    private static final String DOT_FORMAT = "dot";
    private static final String ILOGRAPH_FORMAT = "ilograph";
    private static final String CUSTOM_FORMAT = "fqcn";

    private static final Map<String, Exporter> EXPORTERS = new HashMap<>();

    static {
//        EXPORTERS.put(JSON_FORMAT, new JsonWorkspaceExporter());
  //      EXPORTERS.put(THEME_FORMAT, new JsonWorkspaceThemeExporter());
        EXPORTERS.put(DSL_FORMAT, new DslWorkspaceExporter());
        EXPORTERS.put(PLANTUML_FORMAT, new StructurizrPlantUMLExporter());
        EXPORTERS.put(PLANTUML_FORMAT + "/" + PLANTUML_STRUCTURIZR_SUBFORMAT, new StructurizrPlantUMLExporter());
        EXPORTERS.put(PLANTUML_FORMAT + "/" + PLANTUML_C4PLANTUML_SUBFORMAT, new C4PlantUMLExporter());
        EXPORTERS.put(MERMAID_FORMAT, new MermaidDiagramExporter());
        EXPORTERS.put(DOT_FORMAT, new DOTExporter());
        EXPORTERS.put(WEBSEQUENCEDIAGRAMS_FORMAT, new WebSequenceDiagramsExporter());
        EXPORTERS.put(ILOGRAPH_FORMAT, new IlographExporter());
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("productConfigRootFile::" + productConfigRootFile.getAbsolutePath());

        Workspace workspace;
        try {
            getLog().info(" - loading workspace from DSL");
            StructurizrDslParser structurizrDslParser = new StructurizrDslParser();
            structurizrDslParser.parse(productConfigRootFile);
            workspace = structurizrDslParser.getWorkspace();
            getLog().info("workspace Id:" + workspace.getId());

            // output dir
//            File outputDir = new File(outputFileBaseDir);
            outputFileBaseDir.mkdirs();

            DslWorkspaceExporter exporter = new DslWorkspaceExporter();
            WorkspaceExport export = exporter.export(workspace);
            getLog().info("export " +export.getDefinition());

            export(workspace,PLANTUML_FORMAT);
            //export(workspace,DSL_FORMAT);
            //export(workspace,MERMAID_FORMAT);
            //export(workspace,PLANTUML_C4PLANTUML_SUBFORMAT);
            //export(workspace,PLANTUML_STRUCTURIZR_SUBFORMAT);
            //WorkspaceExporter workspaceExporter = (WorkspaceExporter) exporter;
            //WorkspaceExport export = workspaceExporter.export(workspace);

        }catch(Exception e) {
            e.printStackTrace();
            getLog().error(e);
        }
        getLog().info("finished.");
    }

    private void export(Workspace workspace,String format) throws Exception {
//        String format = "dsl";
        long workspaceId = workspace.getId();
        String outputPath = outputFileBaseDir.getAbsolutePath();

        Exporter exporter = findExporter(format);
        if (exporter == null) {
            getLog().info(" - unknown export format: " + format);
        } else {
            getLog().info(" - exporting with " + exporter.getClass().getSimpleName());

            if (exporter instanceof DiagramExporter) {
                DiagramExporter diagramExporter = (DiagramExporter) exporter;

                if (workspace.getViews().isEmpty()) {
                    getLog().info(" - the workspace contains no views");
                } else {
                    Collection<Diagram> diagrams = diagramExporter.export(workspace);

                    for (Diagram diagram : diagrams) {
                        File file = new File(outputPath, String.format("%s-%s.%s", prefix(workspaceId), diagram.getKey(), diagram.getFileExtension()));
                        writeToFile(file, diagram.getDefinition());

                        if (diagram.getLegend() != null) {
                            file = new File(outputPath, String.format("%s-%s-key.%s", prefix(workspaceId), diagram.getKey(), diagram.getFileExtension()));
                            writeToFile(file, diagram.getLegend().getDefinition());
                        }

                        if (!diagram.getFrames().isEmpty()) {
                            int index = 1;
                            for (Diagram frame : diagram.getFrames()) {
                                file = new File(outputPath, String.format("%s-%s-%s.%s", prefix(workspaceId), diagram.getKey(), index, diagram.getFileExtension()));
                                writeToFile(file, frame.getDefinition());
                                index++;
                            }
                        }
                    }
                }
            } else if (exporter instanceof WorkspaceExporter) {
                WorkspaceExporter workspaceExporter = (WorkspaceExporter) exporter;
                WorkspaceExport export = workspaceExporter.export(workspace);

                String filename = "a-file-name";

//                if (THEME_FORMAT.equalsIgnoreCase(format)) {
  //                  filename = outputFileBaseDir.getName().substring(0, outputFileBaseDir.getName().lastIndexOf('.')) + "-theme";
    //            } else {
      //              filename = outputFileBaseDir.getName().substring(0, outputFileBaseDir.getName().lastIndexOf('.'));
        //        }

                File file = new File(outputPath, String.format("%s.%s", filename, export.getFileExtension()));
                writeToFile(file, export.getDefinition());
            }
        }
    }


    private Exporter findExporter(String format) {
        if (EXPORTERS.containsKey(format.toLowerCase())) {
            return EXPORTERS.get(format.toLowerCase());
        }

        try {
            Class<?> clazz = Class.forName(format);
            if (Exporter.class.isAssignableFrom(clazz)) {
                return (Exporter) clazz.getDeclaredConstructor().newInstance();
            }
        } catch (ClassNotFoundException e) {
            getLog().error(" - unknown export format: " + format);
        } catch (Exception e) {
            getLog().error(" - error creating instance of " + format, e);
        }

        return null;
    }

    private String prefix(long workspaceId) {
        if (workspaceId > 0) {
            return "structurizr-" + workspaceId;
        } else {
            return "structurizr";
        }
    }

    private void writeToFile(File file, String content) throws Exception {
        getLog().info(" - writing " + file.getCanonicalPath());

        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        writer.write(content);
        writer.flush();
        writer.close();
    }
}
