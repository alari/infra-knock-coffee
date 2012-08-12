package ru.mirari.infra.coffee

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

/**
 * @author alari
 * @since 8/12/12 4:50 PM
 */
class DomainCoffeeBuilder {
    static public DomainCoffeeBuilder getBuilder(final Class domainClass, final DefaultGrailsDomainClass grailsDomainClass = null) {
        new DomainCoffeeBuilder(CoffeeDomain.getConfig(domainClass, grailsDomainClass))
    }

    private final CoffeeDomain config

    private DomainCoffeeBuilder(final CoffeeDomain config) {
        this.config = config
    }

    private CoffeeProperty property(final String name) {
        config.property(name)
    }

    String getExtendCoffee() {
        config.extend ? " extends ${config.extend}" : ""
    }

    String getPropertyInitCoffee(final String name) {
        String propCoffee = "@$name = "
        final CoffeeProperty prop = property(name)
        if (prop.init) {
            propCoffee += prop.init
        } else if (prop.isToMany() || prop.isBasicCollection()) {
            propCoffee += "ko.observableArray []"
        } else {
            propCoffee += "ko.observable null"
        }
        propCoffee
    }

    List<String> getFromJsonCoffee(final String name) {
        List<String> coffee = []
        final CoffeeProperty prop = property(name)
        if (prop.mapping) {
            Map mapping = prop.mapping
            coffee.add "      ${name}:"
            mapping.keySet().each {
                coffee.add "        ${it}: (o)-> ${mapping.get(it)}"
            }
        } else if (prop.association) {
            coffee.add "      ${name}:"
            coffee.add "        create: (o)-> if o.data then new ${prop.association.name}VM().fromJson(o.data) else null"
            coffee.add "        key: (o)-> ko.utils.unwrapObservable o.id"
        }
        coffee
    }

    List<String> buildConstructor() {
        List<String> coffee = []
        // Constructor
        coffee.add "  constructor: ->"
        if (config.extend) coffee.add "    super()"
        coffee.add "    @id = ko.observable null"
        config.watchedProperties.each {
            final String propCoffee = getPropertyInitCoffee(it)
            if (propCoffee) coffee.add "    " + propCoffee
        }
        coffee
    }

    List<String> buildFromJson() {
        List<String> coffee = []
        // Loader
        coffee.add "  fromJson: (json)=>"
        coffee.add "    mapping ="

        config.watchedProperties.each {
            coffee.addAll(getFromJsonCoffee(it))
        }

        coffee.add "    ko.mapping.fromJS json, mapping, this"
        coffee.add "    this"
        coffee
    }

    List<String> build() {
        List<String> coffee = []

        if(!config.clazz) return;

        coffee.add "exports = this"
        coffee.add("class exports.${config.clazz.name}VM" + extendCoffee)

        coffee.addAll(buildConstructor())
        coffee.addAll(buildFromJson())

        coffee
    }

    void build(File toFile) {
        if(!config.clazz) return;
        toFile.write(build().join("\n"))
    }
}
