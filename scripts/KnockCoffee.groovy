import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("Compile")

target(main: "Builds or updates .coffee ViewModels for domain classes") {
    loadApp()

    Class CoffeeDomainConfig =  classLoader.loadClass("ru.mirari.infra.CoffeeDomainConfig", true)

    println "Building Coffee ViewModels..."
    for (DefaultGrailsDomainClass domainClass in grailsApp.domainClasses) {
        println "Building ${domainClass.name}VM..."

        def config = CoffeeDomainConfig.getConfig(domainClass)

        // Coffee file building begins
        List<String> coffee = []
        coffee.add "exports = this"
        coffee.add("class exports.${domainClass.name}VM" + config.extendCoffee)

        // Constructor
        coffee.add "  constructor: ->"
        if(config.extend) coffee.add "    super()"
        coffee.add "    @id = ko.observable null"

        domainClass.persistentProperties.each {GrailsDomainClassProperty prop ->
            final String propCoffee = config.getPropertyInitCoffee(prop.name)
            if(propCoffee) coffee.add "    "+propCoffee
        }

        // Loader
        coffee.add "  fromJson: (json)=>"
        coffee.add "    mapping ="

        domainClass.persistentProperties.each {GrailsDomainClassProperty prop ->
            if (prop.name in config.ignore) return;
            if (config.mapping.get(prop.name)) {
                Map mapping = config.mapping.get(prop.name)
                coffee.add "      ${prop.name}:"
                mapping.keySet().each {
                    coffee.add "        ${it}: (o)-> ${mapping.get(it)}"
                }
            } else if (prop.isAssociation()) {
                coffee.add "      ${prop.name}:"
                coffee.add "        create: (o)-> if o.data then new ${prop.referencedDomainClass.name}VM().fromJson(o.data) else null"
                coffee.add "        key: (o)-> ko.utils.unwrapObservable o.id"
            }
        }
        coffee.add "    ko.mapping.fromJS json, mapping, this"
        coffee.add "    this"

        new File("web-app/coffee/domain/${domainClass.name}VM.coffee").write(coffee.join("\n"))

        println()
        println coffee.join("\n")
        println("---------------------")
    }
    "coffee -o web-app/js/ --compile web-app/coffee/".execute()
    println "Built"
}

setDefaultTarget(main)
