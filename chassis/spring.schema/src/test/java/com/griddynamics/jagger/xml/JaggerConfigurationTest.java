package com.griddynamics.jagger.xml;

import com.griddynamics.jagger.JaggerLauncher;
import com.griddynamics.jagger.engine.e1.aggregator.workload.DurationLogProcessor;
import com.griddynamics.jagger.engine.e1.scenario.WorkloadTask;
import com.griddynamics.jagger.invoker.QueryPoolScenarioFactory;
import com.griddynamics.jagger.invoker.ScenarioFactory;
import com.griddynamics.jagger.master.CompositeTask;
import com.griddynamics.jagger.master.configuration.Configuration;
import com.griddynamics.jagger.reporting.ReportingService;
import junit.framework.Assert;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: kgribov
 * Date: 12/14/12
 * Time: 11:00 AM
 * To change this template use File | Settings | File Templates.
 */

/*
*   Launch only as maven test
*/
public class JaggerConfigurationTest {

    private ApplicationContext ctx;

    @BeforeClass
    public void testInit() throws Exception{
        URL directory = new URL("file:" + "../configuration/");
        Properties environmentProperties = new Properties();
        JaggerLauncher.loadBootProperties(directory, "profiles/local/environment.properties", environmentProperties);
        environmentProperties.put("chassis.master.configuration.include",environmentProperties.get("chassis.master.configuration.include")+", ../spring.schema/src/test/resources/example-configuration.conf.xml1");
        ctx = JaggerLauncher.loadContext(directory,"chassis.master.configuration",environmentProperties);
    }

    @Test
    public void conf1Test(){
        Configuration config1 = (Configuration) ctx.getBean("config1");
        Assert.assertNotNull(config1);
    }

    @Test
    public void conf2Test(){
        Configuration config2 = (Configuration) ctx.getBean("config2");
        Assert.assertNotNull(config2);
    }


    @Test
    public void outerTestPlanTest(){
        Configuration config1 = (Configuration) ctx.getBean("config1");
        Assert.assertEquals(config1.getTasks().size(), 2);
        checkListOnNull(config1.getTasks());
    }


    @Test
    public void conf1ListTest(){
        Configuration config1 = (Configuration) ctx.getBean("config1");

        Assert.assertEquals(config1.getSessionExecutionListeners().size(), 3);
        checkListOnNull(config1.getSessionExecutionListeners());

        Assert.assertEquals(config1.getDistributionListeners().size(), 8);
        checkListOnNull(config1.getDistributionListeners());
    }


    @Test
    public void conf2ListTest(){
        Configuration config2 = (Configuration) ctx.getBean("config2");

        Assert.assertEquals(config2.getSessionExecutionListeners().size(), 2);
        checkListOnNull(config2.getSessionExecutionListeners());

        Assert.assertEquals(7, config2.getDistributionListeners().size());
        checkListOnNull(config2.getDistributionListeners());
    }

    @Test
    public void conf1LatencyTest(){
        Configuration config1 = (Configuration) ctx.getBean("config1");
        ExampleTestListener exampleTestListener = (ExampleTestListener)config1.getDistributionListeners().get(config1.getDistributionListeners().size()-1);
        Assert.assertNotNull(exampleTestListener);
    }

    @Test
    public void conf1CalibrationSamplesCountTest(){
        Configuration config1 = (Configuration) ctx.getBean("config1");
        // DANGER! CLASS CAST MAGIC!!!
        ScenarioFactory scenarioFactory =
                ((WorkloadTask)((CompositeTask) config1.getTasks().get(0)).getAttendant().get(0)).getScenarioFactory();
        int calibrationSamplesCount = scenarioFactory.getCalibrationSamplesCount();
        Assert.assertEquals(1101, calibrationSamplesCount);
    }

    @Test
    public void conf1ReportTest(){
        Configuration config1 = (Configuration) ctx.getBean("config1");

        ReportingService reportingService = config1.getReport();
        Assert.assertNotNull(reportingService);
    }

    @Test
    public void conf1ProviderTest() throws Exception {
        Configuration config1 = (Configuration) ctx.getBean("config1");
        // DANGER! CLASS CAST MAGIC!!!
        Iterator it=((QueryPoolScenarioFactory)((WorkloadTask)((CompositeTask) config1.getTasks().get(0)).getAttendant().get(0)).getScenarioFactory()).getEndpointProvider().iterator();
        Assert.assertEquals(RequestPath.class, it.next().getClass());
    }


    private void checkListOnNull(List list){
        for (Object o : list){
            Assert.assertNotNull(o);
        }
    }
}
