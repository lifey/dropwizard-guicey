package ru.vyarus.dropwizard.guice.diagnostic

import io.dropwizard.Application
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.diagnostic.support.AutoScanAppWithBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.*
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooInstaller
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.GuiceSupportModule
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.InstallerItemInfo
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.TaskInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.admin.AdminFilterInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.admin.AdminServletInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.health.HealthCheckInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyFeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
@UseGuiceyApp(AutoScanAppWithBundle)
class AutoScanModeWithBundleDiagnosticTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check diagnostic info correctness"() {

        expect: "correct bundles info"
        info.bundles as Set == [FooBundle, FooBundleRelativeBundle, CoreInstallersBundle] as Set
        info.bundlesFromLookup.isEmpty()
        info.bundlesFromDw.isEmpty()

        and: "correct installers info"
        def classes = [FooInstaller, FooBundleInstaller,
                       JerseyFeatureInstaller.class,
                       JerseyProviderInstaller.class,
                       ResourceInstaller.class,
                       EagerSingletonInstaller.class,
                       HealthCheckInstaller.class,
                       TaskInstaller.class,
                       PluginInstaller.class,
                       AdminFilterInstaller.class,
                       AdminServletInstaller.class]
        info.installers as Set == classes as Set
        info.installersDisabled as Set == [LifeCycleInstaller, ManagedInstaller] as Set
        info.installersFromScan == [FooInstaller]

        and: "correct extensions info"
        info.extensions as Set == [FooResource, FooBundleResource] as Set
        info.extensionsFromScan == [FooResource]
        info.getExtensions(ResourceInstaller) as Set == [FooResource, FooBundleResource] as Set
        info.getExtensions(FooBundleInstaller).isEmpty()

        and: "correct modules"
        info.modules as Set == [FooModule, GuiceSupportModule, FooBundleModule] as Set

        and: "correct scopes"
        info.getActiveScopes() == [Application, ClasspathScanner, CoreInstallersBundle, FooBundle] as Set
        info.getItemsByScope(Application) as Set == [CoreInstallersBundle, FooBundle, FooModule, GuiceSupportModule] as Set
        info.getItemsByScope(ClasspathScanner) as Set == [FooInstaller, FooResource] as Set
        info.getItemsByScope(FooBundle) as Set == [FooBundleInstaller, FooBundleResource, FooBundleModule, FooBundleRelativeBundle] as Set

        and: "lifecycle installer was disabled"
        !info.getItemsByScope(CoreInstallersBundle).contains(LifeCycleInstaller)
        InstallerItemInfo li = info.data.getInfo(LifeCycleInstaller)
        !li.enabled
        li.disabledBy == [Application] as Set
        li.registered

        and: "managed installer was disabled"
        !info.getItemsByScope(CoreInstallersBundle).contains(ManagedInstaller)
        InstallerItemInfo mi = info.data.getInfo(ManagedInstaller)
        !mi.enabled
        mi.disabledBy == [FooBundle] as Set
        mi.registered
    }
}
