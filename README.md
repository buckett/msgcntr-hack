Message Center
==============

This part of the Sakai project provides the Messages tool and the Forums tool. It also used to provide the MessageCenter
tool but this hasn't been part of Sakai since 2.6.

The context ID seen in the code is often the site ID. The context Site ID seen in the code is often the site
reference.

Hibernate named queries are good because they are parsed at startup so you don't have a blowup at runtime.
Goals
-----

To make immutable set of objects that are highly cached and don't create much GC overhead.
Don't require open sessions in view as this makes it difficult for other services to call the API without also
holding a session open in the view.
There's no benefit to having pages being cachable as we generate unique pages for each user (authz, etc).
Not sure how to deal with loading summary (eg topic without any attachments vs loading topic and all bits).
Need to design for AJAX so it's easy to create a REST API.

Dependencies
------------

It depends on the Type API for storing little bits of data. In the DB schema you will often see a type_uuid column
which is a reference to a entry in the TypeManager. It seems that it's only msgcntr that uses the type API so it could
probably be move in here.

Running outside Sakai
---------------------

To aim development I'm hoping to allow this to be run outside the Sakai kernel. Todo this I'll need.

SakaiProxy - 
RequestFilter - Current placement (site), current user, current session, threadlocal management.
UserService


Problems
--------

The services internally use lots of threadlocals that are only setup when requests come through the portal.
 - current user is ok to get through threadlocal.
 - everything else not.
The services also don't have good tests.
Need to contain Sakai dependencies inside one class so it's easy to test the code (not really sure).
Error handling, not really any use of exceptions.
Permissions on services?
Type API doesn't handle i18n.
Should use placement IDs rather than site IDs to allow multiple forums per site.
Should use the standard Sakai authz API rather than re-implementing permissions.
Doesn't have clean responsibilities between services.
Too much mutability on entities exposed through the API.

Should things like forum titles be i18ned?

Are we going to have an open session in the view?

Area is known as "Template Settings" in the interface.

Lots of the classes are far too large.
DuscussionForumManagerImpl has it's own permissions cache because it didn't end up using Sakai security.
Should have strict DAO?

RankManager stores Ranks against User EIDs rather than IDs, should have a quartz jobs to fix this up properly, but 
at the moment it just asks the UDS to translate.

DB Changes
----------
These columns can all go as  it's a set rather than an list now.
MFR_AP_ACCESSORS_T.accessors_index_col 
MFR_AP_MODERATORS_T.moderators_index_col
MFR_AP_CONTRIBUTORS_T.contributors_index_col

This looks unused

DROP TABLE MFR_CONTROL_PERMISSIONS_T;

-- It's an non-unique index on a display name thats never needed.
DROP INDEX CMN_TYPE_T_DISPLAY_NAME_I; 

MFR_ATTACHMENT_T.url

-- This table is for the Label class which is unused and hasn't been touched since 2006 really.
DROP TABLE MFR_LABEL_T;

Blowup because of classloaders
------------------------------

        at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveReference(BeanDefinitionValueResolver.java:329)
        at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveValueIfNecessary(BeanDefinitionValueResolver.java:107)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.applyPropertyValues(AbstractAutowireCapableBeanFactory.java:1387)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.populateBean(AbstractAutowireCapableBeanFactory.java:1128)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:519)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:458)
        at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:295)
        at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:223)
        at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:292)
        at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:194)
        at org.sakaiproject.util.NoisierDefaultListableBeanFactory.preInstantiateSingletons(NoisierDefaultListableBeanFactory.java:73)
        at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:932)
        at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:479)
        at org.sakaiproject.component.impl.SpringCompMgr.init(SpringCompMgr.java:160)
        at org.sakaiproject.component.cover.ComponentManager.getInstance(ComponentManager.java:106)
        at org.sakaiproject.component.cover.ComponentManager.get(ComponentManager.java:117)
        at org.sakaiproject.component.cover.ServerConfigurationService.getInstance(ServerConfigurationService.java:52)
        at org.sakaiproject.util.RequestFilter.init(RequestFilter.java:564)
        at org.apache.catalina.core.ApplicationFilterConfig.initFilter(ApplicationFilterConfig.java:281)
        at org.apache.catalina.core.ApplicationFilterConfig.getFilter(ApplicationFilterConfig.java:262)
        at org.apache.catalina.core.ApplicationFilterConfig.<init>(ApplicationFilterConfig.java:107)
        at org.apache.catalina.core.StandardContext.filterStart(StandardContext.java:4797)
        at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:5473)
        at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:150)
        at org.apache.catalina.core.ContainerBase.addChildInternal(ContainerBase.java:901)
        at org.apache.catalina.core.ContainerBase.addChild(ContainerBase.java:877)
        at org.apache.catalina.core.StandardHost.addChild(StandardHost.java:634)
        at org.apache.catalina.startup.HostConfig.deployWAR(HostConfig.java:1074)
        at org.apache.catalina.startup.HostConfig$DeployWar.run(HostConfig.java:1858)
        at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
        at java.util.concurrent.FutureTask.run(FutureTask.java:266)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
        at java.lang.Thread.run(Thread.java:745)