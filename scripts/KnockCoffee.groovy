import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("Compile")

target(main: "Builds or updates .coffee ViewModels for domain classes") {
    loadApp()

    Class DomainCoffeeBuilder = classLoader.loadClass("ru.mirari.infra.coffee.DomainCoffeeBuilder", true)

    println "Building Coffee ViewModels..."
    for (DefaultGrailsDomainClass domainClass in grailsApp.domainClasses) {
        println "Building ${domainClass.name}VM..."

        DomainCoffeeBuilder.getBuilder(domainClass.clazz, domainClass).build(new File("web-app/coffee/domain/${domainClass.name}VM.coffee"))
    }
    "coffee -o web-app/js/ --compile web-app/coffee/".execute()
    println "Built"
}

setDefaultTarget(main)
