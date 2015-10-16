package rundeck.services

import com.dtolabs.rundeck.core.plugins.configuration.PropertyResolver
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope
import com.dtolabs.rundeck.core.plugins.configuration.Validator
import com.dtolabs.rundeck.plugins.jobs.JobChangeListener
import com.dtolabs.rundeck.plugins.scm.ScmExportPlugin
import com.dtolabs.rundeck.plugins.scm.ScmExportPluginFactory
import com.dtolabs.rundeck.plugins.scm.ScmImportPlugin
import com.dtolabs.rundeck.plugins.scm.ScmImportPluginFactory
import com.dtolabs.rundeck.plugins.scm.ScmOperationContext
import com.dtolabs.rundeck.plugins.scm.ScmPluginInvalidInput
import com.dtolabs.rundeck.server.plugins.ValidatedPlugin
import com.dtolabs.rundeck.server.plugins.services.ScmExportPluginProviderService
import com.dtolabs.rundeck.server.plugins.services.ScmImportPluginProviderService
import grails.test.mixin.TestFor
import rundeck.services.scm.ScmPluginConfigData
import spock.lang.Specification

/**
 * Created by greg on 10/15/15.
 */
@TestFor(ScmService)
class ScmServiceSpec extends Specification {


    def "disablePlugin"() {
        given:
        service.frameworkService = Mock(FrameworkService)
        service.pluginConfigService = Mock(PluginConfigService)
        service.jobEventsService = Mock(JobEventsService)
        ScmPluginConfigData config = Mock(ScmPluginConfigData)
        ScmExportPlugin exportPlugin = Mock(ScmExportPlugin)
        ScmImportPlugin importPlugin = Mock(ScmImportPlugin)
        service.loadedExportPlugins['test1']= exportPlugin
        service.loadedImportPlugins['test1']= importPlugin
        def dummyListener = Mock(JobChangeListener)
        service.loadedExportListeners['test1'] = dummyListener
        service.loadedImportListeners['test1'] = dummyListener
        service.renamedJobsCache['test1'] = [:]
        service.deletedJobsCache['test1'] = [:]

        when:
        service.disablePlugin(integration, 'test1', null)

        then:
        1 * service.pluginConfigService.loadScmConfig(
                'test1',
                "etc/scm-${integration}.properties",
                'scm.' + integration
        ) >> config
        1 * config.setEnabled(false)
        1 * service.pluginConfigService.storeConfig(config, 'test1', "etc/scm-${integration}.properties")
        1 * service.jobEventsService.removeListener(dummyListener)

        if (integration == ScmService.EXPORT) {
            service.loadedExportPlugins['test1'] == null
            service.loadedExportListeners['test1'] == null
            service.renamedJobsCache['test1'] == null
            service.deletedJobsCache['test1'] == null
            1 * exportPlugin.cleanup()
        } else {
            service.loadedImportPlugins['test1'] == null
            service.loadedImportListeners['test1'] == null
            1 * importPlugin.cleanup()
        }


        where:
        integration       | _
        ScmService.EXPORT | _
        ScmService.IMPORT | _
    }
    def "disablePlugin not enabled"() {
        given:
        service.frameworkService = Mock(FrameworkService)
        service.pluginConfigService = Mock(PluginConfigService)
        service.jobEventsService = Mock(JobEventsService)
        ScmExportPlugin exportPlugin = Mock(ScmExportPlugin)
        ScmImportPlugin importPlugin = Mock(ScmImportPlugin)
        service.loadedExportPlugins['test1']= exportPlugin
        service.loadedImportPlugins['test1']= importPlugin
        def dummyListener = Mock(JobChangeListener)
        service.loadedExportListeners['test1'] = dummyListener
        service.loadedImportListeners['test1'] = dummyListener
        service.renamedJobsCache['test1'] = [:]
        service.deletedJobsCache['test1'] = [:]

        when:
        service.disablePlugin(integration, 'test1', null)

        then:
        1 * service.pluginConfigService.loadScmConfig(
                'test1',
                "etc/scm-${integration}.properties",
                'scm.' + integration
        ) >> null
        1 * service.jobEventsService.removeListener(dummyListener)

        if (integration == ScmService.EXPORT) {
            service.loadedExportPlugins['test1'] == null
            service.loadedExportListeners['test1'] == null
            service.renamedJobsCache['test1'] == null
            service.deletedJobsCache['test1'] == null
            1 * exportPlugin.cleanup()
        } else {
            service.loadedImportPlugins['test1'] == null
            service.loadedImportListeners['test1'] == null
            1 * importPlugin.cleanup()
        }


        where:
        integration       | _
        ScmService.EXPORT | _
        ScmService.IMPORT | _
    }

    def "removePluginConfiguration"() {
        given:
        service.frameworkService = Mock(FrameworkService)
        service.pluginConfigService = Mock(PluginConfigService)
        service.jobEventsService = Mock(JobEventsService)
        ScmPluginConfigData config = Mock(ScmPluginConfigData)

        when:
        service.removePluginConfiguration(integration, 'test1', null)
        then:
        1 * service.pluginConfigService.loadScmConfig(
                'test1',
                "etc/scm-${integration}.properties",
                'scm.' + integration
        ) >> config
        1 * config.setEnabled(false)
        1 * service.pluginConfigService.storeConfig(config, 'test1', "etc/scm-${integration}.properties")
        1 * service.jobEventsService.removeListener(_)
        1 * service.pluginConfigService.removePluginConfiguration('test1', "etc/scm-${integration}.properties")

        where:
        integration       | _
        ScmService.EXPORT | _
        ScmService.IMPORT | _
    }

    def "removeAllPluginConfiguration"() {
        given:
        service.frameworkService = Mock(FrameworkService)
        service.pluginConfigService = Mock(PluginConfigService)
        service.jobEventsService = Mock(JobEventsService)
        ScmPluginConfigData config = Mock(ScmPluginConfigData)
        ScmPluginConfigData config2 = Mock(ScmPluginConfigData)

        when:
        service.removeAllPluginConfiguration('test1', null)
        then:
        1 * service.pluginConfigService.loadScmConfig(
                'test1',
                "etc/scm-import.properties",
                'scm.import'
        ) >> config
        1 * service.pluginConfigService.loadScmConfig(
                'test1',
                "etc/scm-export.properties",
                'scm.export'
        ) >> config2
        1 * config.setEnabled(false)
        1 * config2.setEnabled(false)
        1 * service.pluginConfigService.storeConfig(config, 'test1', "etc/scm-import.properties")
        1 * service.pluginConfigService.storeConfig(config2, 'test1', "etc/scm-export.properties")
        2 * service.jobEventsService.removeListener(_)
        1 * service.pluginConfigService.removePluginConfiguration('test1', "etc/scm-import.properties")
        1 * service.pluginConfigService.removePluginConfiguration('test1', "etc/scm-export.properties")

    }

    def "validatePluginSetup"() {
        given:
        def config = [:]
        service.pluginService = Mock(PluginService)
        service.frameworkService = Mock(FrameworkService)
        def resolver = Mock(PropertyResolver)
        service.scmExportPluginProviderService = Mock(ScmExportPluginProviderService)
        service.scmImportPluginProviderService = Mock(ScmImportPluginProviderService)

        def validated = new ValidatedPlugin(valid: false)

        when:
        def result = service.validatePluginSetup(integration, 'test', 'type', config)


        then:

        1 * service.frameworkService.getFrameworkPropertyResolver('test', config) >> resolver
        1 * service.pluginService.validatePlugin(
                'type',
                integration == ScmService.EXPORT ? service.scmExportPluginProviderService :
                        service.scmImportPluginProviderService,
                resolver,
                PropertyScope.Instance,
                PropertyScope.Project
        ) >>
                validated
        result == validated

        where:
        integration       | _
        ScmService.EXPORT | _
        ScmService.IMPORT | _

    }

    def "init plugin invalid"() {
        given:
        def ctx = Mock(ScmOperationContext)
        def config = [:]
        service.pluginService = Mock(PluginService)
        service.frameworkService = Mock(FrameworkService)
        def resolver = Mock(PropertyResolver)


        def report = Validator.errorReport('a', 'b')
        def validated = new ValidatedPlugin(valid: false, report: report)
        when:
        def result = service.initPlugin(integration, ctx, 'atype', config)

        then:
        1 * service.frameworkService.getFrameworkPropertyResolver(*_)
        1 * service.pluginService.validatePlugin(*_) >> validated

        ScmPluginInvalidInput err = thrown()
        err.report == report


        where:
        integration       | _
        ScmService.EXPORT | _
        ScmService.IMPORT | _
    }
    def "init import plugin valid"() {
        given:
        def ctx = Mock(ScmOperationContext){
            getFrameworkProject()>>'testProject'
        }
        def config = [:]
        def configobj = Mock(ScmPluginConfigData)

        ScmImportPluginFactory importFactory = Mock(ScmImportPluginFactory)
        ScmImportPlugin plugin = Mock(ScmImportPlugin)

        service.pluginService = Mock(PluginService)
        service.pluginConfigService = Mock(PluginConfigService)
        service.jobEventsService = Mock(JobEventsService)
        service.frameworkService = Mock(FrameworkService)

        def validated = new ValidatedPlugin(valid: true)

        when:
        def result = service.initPlugin(integration, ctx, 'atype', config)

        then:
        1 * service.frameworkService.getFrameworkPropertyResolver(*_)
        1 * service.pluginService.validatePlugin(*_) >> validated
        1 * service.pluginService.getPlugin('atype',_) >> importFactory
        1 * service.pluginConfigService.loadScmConfig(*_) >> configobj
        1 * configobj.getSettingList('trackedItems') >> ['a','b']
        1 * importFactory.createPlugin(ctx,config,['a','b']) >> plugin
        1 * service.jobEventsService.addListenerForProject(_, 'testProject')

        result == plugin



        where:
        integration       | _
        ScmService.IMPORT | _
    }
    def "init export plugin valid"() {
        given:
        def ctx = Mock(ScmOperationContext){
            getFrameworkProject()>>'testProject'
        }
        def config = [:]

        ScmExportPluginFactory exportFactory = Mock(ScmExportPluginFactory)
        ScmExportPlugin plugin = Mock(ScmExportPlugin)

        service.pluginService = Mock(PluginService)
        service.pluginConfigService = Mock(PluginConfigService)
        service.jobEventsService = Mock(JobEventsService)
        service.frameworkService = Mock(FrameworkService)

        def validated = new ValidatedPlugin(valid: true)

        when:
        def result = service.initPlugin(integration, ctx, 'atype', config)

        then:
        1 * service.frameworkService.getFrameworkPropertyResolver(*_)
        1 * service.pluginService.validatePlugin(*_) >> validated
        1 * service.pluginService.getPlugin('atype',_) >> exportFactory
        1 * exportFactory.createPlugin(ctx,config) >> plugin
        1 * service.jobEventsService.addListenerForProject(_, 'testProject')

        result == plugin



        where:
        integration       | _
        ScmService.EXPORT | _
    }
}
