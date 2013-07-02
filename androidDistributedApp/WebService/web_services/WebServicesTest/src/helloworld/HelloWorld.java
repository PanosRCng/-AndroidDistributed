package helloworld;

import javax.jws.WebMethod;
import javax.jws.WebService;
import com.google.gson.Gson;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import org.hibernate.Query;
import java.util.*;

@WebService
public class HelloWorld {

    @WebMethod
    public String registerSmartphone(String smartphoneJson)
    {
        Gson gson = new Gson();
        Smartphone smartphone = gson.fromJson(smartphoneJson, Smartphone.class);
        int phoneId = smartphone.getPhoneId();
        String sensors_rules = smartphone.getSensorsRules();
        String time_rules = smartphone.getTimeRules();

        Configuration conf = new Configuration().configure();
        SessionFactory factory = conf.buildSessionFactory();
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();

        String hql = "from SmartphonesEntity where phoneId=?";
        List<SmartphonesEntity> recordList= session.createQuery(hql).setInteger(0, phoneId).list();

        String response = "-1";

        if(recordList!=null && recordList.size()>0)
        {
            SmartphonesEntity smartphoneEntity2 = recordList.get(0);
            smartphoneEntity2.setSensorsRules(sensors_rules);
            smartphoneEntity2.setTimeRules(time_rules);

            session.update(smartphoneEntity2);

            response = Integer.toString(phoneId);
        }
        else
        {
            hql = "from SmartphonesEntity where 1=1";
            List<SmartphonesEntity> smartphonesList= session.createQuery(hql).list();

            int smartphoneId;

            if(smartphonesList.size() > 0)
            {
                smartphoneId = smartphonesList.get(smartphonesList.size()-1).getId() + 1;
            }
            else
            {
                smartphoneId = 1;
            }

            SmartphonesEntity smartphoneEntity = new SmartphonesEntity();
            smartphoneEntity.setPhoneId(smartphoneId);
            smartphoneEntity.setSensorsRules(sensors_rules);
            smartphoneEntity.setTimeRules(time_rules);

            session.save(smartphoneEntity);

            response = Integer.toString(smartphoneId);
        }

        tx.commit();

        return response;
    }

    @WebMethod
    public String getExperiment(String smartphoneJson)
    {
        Gson gson = new Gson();
        Smartphone smartphone = gson.fromJson(smartphoneJson, Smartphone.class);
        int phoneId = smartphone.getPhoneId();
        String smartphoneSensorRules = smartphone.getSensorsRules();

        Configuration conf = new Configuration().configure();
        SessionFactory factory = conf.buildSessionFactory();
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();

//        SmartphonesEntity smartphoneEntity = new SmartphonesEntity();
//        smartphoneEntity.setPhoneId(phoneId);

        String hql = "from SmartphonesEntity where phoneId=?";
        List<SmartphonesEntity> recordList= session.createQuery(hql).setInteger(0, phoneId).list();

        String jsonExperiment="";

        if( recordList.size() > 0 )
        {
            SmartphonesEntity my_smartphone = recordList.get(0);
            my_smartphone.setSensorsRules(smartphoneSensorRules);
            session.update(my_smartphone);

            //         String smartphoneSensorRules = my_smartphone.getSensorsRules();
            String smarDep = smartphoneSensorRules;
            String[] smarDeps = smartphoneSensorRules.split("|");

            if(recordList.size()==1)
            {
                hql = "from ExperimentsEntity where 1=1";
                List<ExperimentsEntity> experimentsList= session.createQuery(hql).list();

                for(ExperimentsEntity experimentsEntity : experimentsList)
                {
                    if(experimentsEntity.getStatus().equals("finished"))
                    {
                        continue;
                    }

                    String expDep = experimentsEntity.getSensorDependencies();
                    String[] expDeps = expDep.split("|");

                    Set<String> smarSet = new HashSet<String>(Arrays.asList(smarDeps));
                    Set<String> expSet = new HashSet<String>(Arrays.asList(expDeps));

                    if( smarSet.equals(expSet) )
                    {
                        Experiment experiment = new Experiment(experimentsEntity.getContextType(),
                                        experimentsEntity.getUserEmail(), experimentsEntity.getName(),
                            experimentsEntity.getSensorDependencies(), experimentsEntity.getTimeDependencies(),
                            experimentsEntity.getExpires(), experimentsEntity.getUrl());

                        jsonExperiment = gson.toJson(experiment);
                        System.out.println(jsonExperiment);
                        break;
                    }
                    else
                    {
                        System.out.println("this experiment violates phone's sensors rules");
                    }
                }
            }

            tx.commit();
        }
        else
        {
            jsonExperiment="0";
        }

        return jsonExperiment;
    }

    @WebMethod
    public String getPluginList(String pingJson)
    {
        Gson gson = new Gson();

        Configuration conf = new Configuration().configure();
        SessionFactory factory = conf.buildSessionFactory();
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();

        String hql = "from PluginsEntity where 1=1";
        List<PluginsEntity> pluginsList= session.createQuery(hql).list();

        String jsonPluginList = "";
        PluginList pluginList = new PluginList();
        ArrayList<MyPlugInfo> plugList = new ArrayList<MyPlugInfo>();

        for( PluginsEntity plugin : pluginsList )
        {
            String id = plugin.getPluginId();
            String description = plugin.getDescription();
            String name = plugin.getName();
            String installUrl = plugin.getInstallUrl();
            String runtimeFactoryClass = plugin.getRuntimeFactoryClass();

            MyPlugInfo myPlugInfo = new MyPlugInfo(id, runtimeFactoryClass, name, description, installUrl);

            plugList.add(myPlugInfo);
        }

        pluginList.setPluginList(plugList);

        jsonPluginList = gson.toJson(pluginList);
        System.out.println(jsonPluginList);

        return jsonPluginList;
    }



    @WebMethod
    public String reportResults(String reportJson)
    {
        Gson gson = new Gson();
        Report report = gson.fromJson(reportJson, Report.class);
        String contextType = report.getName();
        ArrayList<String> experimentResults = report.getResults();
        System.out.println("contextType: " + contextType );

        Configuration conf = new Configuration().configure();
        SessionFactory factory = conf.buildSessionFactory();

        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();

        Query q = session.createQuery("from ExperimentsEntity where contextType = :contextType ");
        q.setParameter("contextType", contextType);
        ExperimentsEntity experiment = (ExperimentsEntity)q.list().get(0);
        int experimentId = experiment.getId();
        int executedBy = experiment.getExecutedBy();
        executedBy++;
        experiment.setExecutedBy(executedBy);
        experiment.setStatus("running");
        session.update(experiment);

        for(String result : experimentResults)
        {
            ResultsEntity resultsEntity = new ResultsEntity();
            resultsEntity.setExperimentId(experimentId);
            resultsEntity.setSourceId(executedBy);

            if(result != null)
            {
                resultsEntity.setValue(result);
            }
            else
            {
                resultsEntity.setValue("");
            }


            session.save(resultsEntity);

            session.flush();
            session.clear();
        }

        tx.commit();

        session.close();

        return "1";
    }

    @WebMethod
    public String Ping(String pingJson)
    {
        Gson gson = new Gson();
        Ping ping = gson.fromJson(pingJson, Ping.class);

        if(ping.getValue() == 1)
        {
            return "1";
        }

        return "0";
    }
}
