package be.raildelays.batch;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@DirtiesContext
// Because of issue [SPR-8849] (https://jira.springsource.org/browse/SPR-8849)
@ContextConfiguration(locations = {"/jobs/main-job-context.xml"})
@DataSet(value = "classpath:HandleDelayMoreThanOneHourIT.xml", tearDownOperation = DBOperation.DELETE_ALL)
public class HandleDelayMoreThanOneHourIT extends AbstractContextIT {

    @Test
    @Ignore
    public void testStep3() throws ParseException, IOException {
        final Map<String, JobParameter> parameters = new HashMap<>();
        final ExecutionContext executionContext = MetaDataInstanceFactory.createJobExecution().getExecutionContext();

        executionContext.putLong("moreThanOneHourDelay", 466);

        parameters.put("input.file.path", new JobParameter("train-list.properties"));
        parameters.put("date", new JobParameter(new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2000")));
        parameters.put("station.a.name", new JobParameter("Liège-Guillemins"));
        parameters.put("station.b.name", new JobParameter("Brussels (Bruxelles)-Central"));
        parameters.put("excel.output.path", new JobParameter("./output.xls"));
        parameters.put("excel.input.template", new JobParameter(new ClassPathResource("template.xls").getFile().getAbsolutePath()));
        parameters.put("mail.account.username", new JobParameter("alexis.soumagne@gmail.com"));
        parameters.put("mail.account.password", new JobParameter("BRU338 tcpqriow"));
        parameters.put("mail.account.address", new JobParameter("alexis.soumagne@gmail.com"));
        parameters.put("mail.server.host", new JobParameter("smtp.gmail.com"));
        parameters.put("mail.server.port", new JobParameter("465"));

        JobExecution jobExecution = getJobLauncherTestUtils().launchStep("handleDelayMoreThanOneHour", new JobParameters(parameters), executionContext);

        Assert.assertFalse(jobExecution.getStatus().isUnsuccessful());
        Assert.assertEquals(1, jobExecution.getStepExecutions().toArray(new StepExecution[1])[0].getWriteCount());
    }

}
