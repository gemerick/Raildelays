package be.raildelays.batch;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.file.ExcelSheetItemWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@ContextConfiguration(locations = {"/jobs/steps/handle-max-months-job-context.xml"})
public class HandleMaxMonthsIT extends AbstractContextIT {

    private static List<String> FILES_NAMES = Arrays.asList("20140107", "20131128", "20130905");

    private static String TEMPLATE_PATH;

    private static String TARGET_PATH; // Should be ./target/

    private static String SOURCE_PATH;

    private static String ARCHIVE_PATH;

    private static FileFilter FILTER_EXCEL_FILES = pathname ->
            pathname.getName().endsWith(ExcelSheetItemWriter.Format.OLE2.getFileExtension()) ||
                    pathname.getName().endsWith(ExcelSheetItemWriter.Format.OOXML.getFileExtension());

    @BeforeClass
    public static void setUp() throws IOException {
        File templateFile = new ClassPathResource("template.xls").getFile();

        TARGET_PATH = templateFile.getParentFile().getParentFile().getAbsolutePath() + File.separator;
        SOURCE_PATH = TARGET_PATH + "test-classes" + File.separator + "6monthsDelays" + File.separator;
        ARCHIVE_PATH = TARGET_PATH + LocalDate.now().toString() + File.separator;
        TEMPLATE_PATH = templateFile.getAbsolutePath();

        cleanUp();
        copyFiles();
    }


    @Test
    public void testCompleted() throws Exception {
        final Map<String, JobParameter> parameters = new HashMap<>();

        parameters.put("excel.output.path", new JobParameter(TARGET_PATH));
        parameters.put("excel.file.name", new JobParameter("retard_sncb"));
        parameters.put("excel.file.extension", new JobParameter("xls"));
        parameters.put("excel.archive.path", new JobParameter(TARGET_PATH));
        parameters.put("excel.template.path", new JobParameter(TEMPLATE_PATH));
        parameters.put("language", new JobParameter("en"));
        parameters.put("threshold.date", new JobParameter(Date.from(LocalDate.of(2014, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))));
        // We don't test the last step sendEmail
        parameters.put("mail.server.host", new JobParameter(""));
        parameters.put("mail.server.port", new JobParameter(25L));
        parameters.put("mail.account.username", new JobParameter(""));
        parameters.put("mail.account.password", new JobParameter(""));
        parameters.put("mail.account.address", new JobParameter(""));

        JobExecution jobExecution = getJobLauncherTestUtils().launchJob(new JobParameters(parameters));

        jobExecution.getStepExecutions().forEach(stepExecution -> {
            if (!stepExecution.getStepName().equals("sendEmail")) {
                Assert.assertFalse(stepExecution.getStatus().isUnsuccessful());
            }
        });
        Assert.assertEquals(3, getExcelFiles().size());

        getExcelFiles().forEach(file ->
                Assert.assertTrue(
                        "The file name is not one of those expectedTime",
                        FILES_NAMES.stream().anyMatch(s -> file.getName().contains(s))
                )
        );
    }

    @AfterClass
    public static void tearDown() {
        //cleanUp();
    }


    protected static void copyFiles() throws IOException {
        Files.list(Paths.get(SOURCE_PATH)).forEach(path -> {
            try {
                Files.copy(path, Paths.get(TARGET_PATH + "/" + path.getFileName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    protected static List<File> getExcelFiles() {
        final List<File> result = new ArrayList<>();
        final File firstDirectory = new File(TARGET_PATH);
        final File secondDirectory = new File(ARCHIVE_PATH);

        result.addAll(asList(firstDirectory.listFiles(FILTER_EXCEL_FILES)));
        result.addAll(asList(secondDirectory.listFiles(FILTER_EXCEL_FILES)));

        return result;
    }

    protected static List<File> asList(File[] array) {
        return array != null ? Arrays.asList(array) : Collections.EMPTY_LIST;
    }

    protected static void cleanUp() {
        //-- We remove any result from the test
        getExcelFiles()
                .stream()
                .filter(file -> !file.delete())
                .forEach(java.io.File::deleteOnExit);
    }

}
